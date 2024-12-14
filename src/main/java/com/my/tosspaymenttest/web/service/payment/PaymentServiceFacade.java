package com.my.tosspaymenttest.web.service.payment;

import com.my.tosspaymenttest.client.TossPaymentClient;
import com.my.tosspaymenttest.client.dto.TossPaymentReqDto;
import com.my.tosspaymenttest.client.dto.TossPaymentRespDto;
import com.my.tosspaymenttest.web.api.payment.dto.PaymentReqDto;
import com.my.tosspaymenttest.web.api.payment.dto.PaymentRespDto;
import com.my.tosspaymenttest.web.domain.payment.Payment;
import com.my.tosspaymenttest.web.domain.point.Point;
import com.my.tosspaymenttest.web.domain.pointHistory.PointHistory;
import com.my.tosspaymenttest.web.domain.user.User;
import com.my.tosspaymenttest.web.ex.PaymentCancellationException;
import com.my.tosspaymenttest.web.ex.PaymentConfirmException;
import com.my.tosspaymenttest.web.ex.PointChargeException;
import com.my.tosspaymenttest.web.ex.PointHistoryException;
import com.my.tosspaymenttest.web.service.AlertService;
import com.my.tosspaymenttest.web.service.MetricService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        User user = paymentService.validateUserAndPayment(paymentReqDto);
        TossPaymentRespDto tossPaymentRespDto = requestPaymentConfirm(paymentReqDto, user);

        // 결제 정보 저장 -> 별도 트랜잭션
        Payment payment = paymentService.savePaymentInfo(paymentReqDto, tossPaymentRespDto, user);

        try{
            //포인트 충전 -> 별도 트랜잭션
            Point point = paymentService.chargePoint(user, payment, paymentReqDto);
            try{
                //포인트 충전 내역 저장 -> 별도 트랜잭션
                PointHistory pointHistory = paymentService.savePointHistory(point);
                return new PaymentRespDto(pointHistory);
            }catch (PointHistoryException e){
                handleFailedPointHistorySave(point, e);

                //실패한 포인트 충전 내역 로그 저장 -> 별도 트랜잭션
                try {
                    paymentService.saveFailedPointHistoryLog(point, e);
                } catch(Exception logSaveFailException){//이 경우 로깅만 하고 넘어가되 모니터링은 필요함
                    handleFailedLogSave(point, logSaveFailException);
                }
                throw e;
            }
        }catch (PointChargeException e){
            rollbackPointCharge(payment, paymentReqDto, user, e);
            try {
                metricService.recordFailedCharge(user.getId(), payment.getId());
            }catch(Exception metricSaveException){
                log.error("메트릭 수집 실패: {}", metricSaveException.getMessage(), metricSaveException);
            }
            throw e;
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
            log.error("토스 결제 승인 실패: {}", e.getMessage(), e);
            alertService.sendAlert("결제 승인 실패", String.format("userId=%d, orderId=%s", user.getId(), paymentReqDto.getOrderId()));
            throw new PaymentConfirmException("결제 승인 실패", e);
        }
    }

    private void handleFailedPointHistorySave(Point point, PointHistoryException e){
        log.error("포인트 충전 이력 저장 실패. 재시도 큐에 등록됨. pointId = {}", point.getId());
        alertService.sendAlert("포인트 충전 이력 저장 실패",
                String.format("userId=%d, pointId=%d", point.getUser().getId(), point.getId()));
    }

    private void handleFailedLogSave(Point point, Exception e) {
        log.error("실패한 포인트 이력 로그 저장에 실패: {}",e.getMessage(), e);
        alertService.sendAlert("실패한 포인트 이력 로그 저장 실패",
                String.format("userId=%d, pointId=%d, error=%s",
                        point.getUser().getId(),
                        point.getId(),
                        e.getMessage()
                )
        );
        metricService.recordFailedLogSave(point.getId());
    }

    private void rollbackPointCharge(Payment payment, PaymentReqDto paymentReqDto, User user, Exception e) {
        try {
            handlePaymentCancellation(payment, paymentReqDto, user, e);
        } catch (Exception cancelException) {
            log.error("[긴급] Payment 롤백 실패: {}",cancelException.getMessage(), cancelException);
            //이 때는 수동 개입이 필요한 상황이므로 긴급 알람을 보낸다
            alertService.sendEmergencyAlert("Payment rollback failed",
                    String.format("Payment: %s, User: %s", payment.getId(), user.getId()));
            metricService.recordFailedCharge(user.getId(), payment.getId());
        }
    }

    private void handlePaymentCancellation(Payment payment, PaymentReqDto paymentReqDto, User user, Exception e){
        TossPaymentRespDto canceledPaymentInfo =
                paymentService.cancelPaymentAndThrowWhenFailToUpdatePoint(paymentReqDto, user, e);
        try {
            paymentService.updatePaymentStatus(payment, canceledPaymentInfo);
        } catch(Exception updateFailException){
            log.error("결제 취소 상태 업데이트 실패: {}",updateFailException.getMessage(), updateFailException);
            String message = String.format("결제 취소 상태 업데이트 실패 - payment: %s, user: %s", payment.getId(), user.getId());
            throw new PaymentCancellationException(message, updateFailException);
        }
    }
}
