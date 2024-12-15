package com.my.tosspaymenttest.web.service.payment;

import com.my.tosspaymenttest.client.TossPaymentClient;
import com.my.tosspaymenttest.client.dto.TossPaymentReqDto;
import com.my.tosspaymenttest.client.dto.TossPaymentRespDto;
import com.my.tosspaymenttest.client.ex.badRequest.TossPaymentBadRequestException;
import com.my.tosspaymenttest.web.api.payment.dto.PaymentReqDto;
import com.my.tosspaymenttest.web.api.payment.dto.PaymentRespDto;
import com.my.tosspaymenttest.web.domain.payment.Payment;
import com.my.tosspaymenttest.web.domain.point.Point;
import com.my.tosspaymenttest.web.domain.pointHistory.PointHistory;
import com.my.tosspaymenttest.web.domain.user.User;
import com.my.tosspaymenttest.web.ex.*;
import com.my.tosspaymenttest.web.service.AlertService;
import com.my.tosspaymenttest.web.service.MetricService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceFacade {

    private final PaymentService paymentService;

    private final TossPaymentClient tossPaymentClient;
    private final AlertService alertService;
    private final MetricService metricService;

    @Transactional(readOnly = true)
    public PaymentRespDto processPayment(PaymentReqDto paymentReqDto){

        try {
            MDC.put("merchantUid", paymentReqDto.getOrderId());
            MDC.put("paymentKey", paymentReqDto.getPaymentKey());
            log.info("결제 처리 시작");

            User user = paymentService.validateUserAndPayment(paymentReqDto);
            TossPaymentRespDto tossPaymentRespDto = requestPaymentConfirm(paymentReqDto, user);
            Payment payment = paymentService.savePaymentInfo(paymentReqDto, tossPaymentRespDto, user);

            try {
                PointHistory pointHistory = paymentService.chargePointWithHistory(user, payment);
                return new PaymentRespDto(pointHistory);
            } catch (Exception e) {
                rollbackPointCharge(payment, paymentReqDto, user, e);
                recordMetric(user, payment);

                if (e instanceof PointChargeException) {
                    throw e;
                }

                log.error("[시스템 오류] 포인트 충전 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
                throw new PaymentSystemException("시스템 오류로 인한 결제 실패", e);
            }
        } catch (PaymentValidationException | PaymentConfirmException | PointChargeException | PaymentSystemException e) {
            throw e;
        } catch (Exception e) {
            log.error("결제 처리 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            throw new PaymentSystemException("결제 처리 실패", e);
        } finally {
            MDC.remove("merchantUid");
            MDC.remove("paymentKey");
        }
    }

    private void recordMetric(User user, Payment payment) {
        try {
            metricService.recordFailedCharge(user.getId(), payment.getId());
        } catch (Exception metricSaveException) {
            log.error("메트릭 수집 실패: {}", metricSaveException.getMessage(), metricSaveException);
        }
    }

    private TossPaymentRespDto requestPaymentConfirm(PaymentReqDto paymentReqDto, User user){
        try {
            return tossPaymentClient.confirmPayment(
                    new TossPaymentReqDto(
                            paymentReqDto.getOrderId(),
                            paymentReqDto.getAmount(),
                            paymentReqDto.getPaymentKey()
                    ));
        } catch (Exception e){
            alertService.sendAlert("결제 승인 실패", String.format("userId=%d, orderId=%s", user.getId(), paymentReqDto.getOrderId()));
            throw new PaymentConfirmException("결제 승인 실패", e);
        }
    }

    private void rollbackPointCharge(Payment payment, PaymentReqDto paymentReqDto, User user, Exception originalError) {
        try {
            handlePaymentCancellation(payment, paymentReqDto, user, originalError);
        } catch (TossPaymentBadRequestException | CanceledPaymentException e) {
            throw e;
        } catch (Exception cancelException) {
            log.error("[긴급] Payment 롤백 실패: {}", cancelException.getMessage(), cancelException);
            alertService.sendEmergencyAlert(
                    "[긴급] Payment 롤백 실패",
                    formatAlertMessage(payment, user, originalError, cancelException)
            );
        } finally {
            recordMetric(user, payment);
        }
    }

    private String formatAlertMessage(Payment payment, User user, Exception originalError, Exception cancelError) {
        return String.format("""
                    시간: %s
                    Payment ID: %d
                    사용자ID: %d
                    최초에러: %s
                    취소실패원인: %s
                    """,
                LocalDateTime.now(),
                payment.getId(),
                user.getId(),
                originalError.getMessage(),
                cancelError.getMessage()
        );
    }

    private void handlePaymentCancellation(Payment payment, PaymentReqDto paymentReqDto, User user, Exception originalError) {
        try {
            TossPaymentRespDto canceledPaymentInfo =
                    paymentService.cancelPaymentAndThrowWhenFailToUpdatePoint(paymentReqDto, user, originalError);
            paymentService.updatePaymentStatusToCanceled(payment, canceledPaymentInfo);
            log.info("결제 취소 처리 완료 - paymentId: {}", payment.getId());
        } catch (TossPaymentBadRequestException e) {
            log.warn("결제 취소 중 예측된 실패 발생: {}", e.getMessage());
            alertService.sendAlert("결제 취소 중 예측된 실패",
                    formatAlertMessage(payment, user, originalError, e));
            throw e;
        } catch (CanceledPaymentException e) {
            log.error("[긴급] 결제 취소 상태 검증 실패: {}", e.getMessage());
            alertService.sendEmergencyAlert(
                    "[긴급] 결제 취소 상태 검증 실패",
                    formatAlertMessage(payment, user, originalError, e));
            throw e;
        } catch (Exception updateFailException) {
            log.error("결제 취소 상태 DB 반영 실패: {}", updateFailException.getMessage(), updateFailException);
            String message = String.format(
                    "수동 확인 필요 - 토스 결제는 취소되었으나 DB 반영 실패. paymentId: %s, userId: %s",
                    payment.getId(), user.getId()
            );
            throw new PaymentCancellationException(message, updateFailException);
        }
    }
}
