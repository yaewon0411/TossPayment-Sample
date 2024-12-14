package com.my.tosspaymenttest.web.service.payment;

import com.my.tosspaymenttest.client.TossPaymentClient;
import com.my.tosspaymenttest.client.dto.TossPaymentCancelReqDto;
import com.my.tosspaymenttest.client.dto.TossPaymentReqDto;
import com.my.tosspaymenttest.client.dto.TossPaymentRespDto;
import com.my.tosspaymenttest.web.api.payment.dto.PaymentReqDto;
import com.my.tosspaymenttest.web.api.payment.dto.PaymentRespDto;
import com.my.tosspaymenttest.web.domain.payment.Payment;
import com.my.tosspaymenttest.web.domain.payment.PaymentCancelReason;
import com.my.tosspaymenttest.web.domain.payment.PaymentRepository;
import com.my.tosspaymenttest.web.domain.payment.PaymentType;
import com.my.tosspaymenttest.web.domain.point.Point;
import com.my.tosspaymenttest.web.domain.point.PointRepository;
import com.my.tosspaymenttest.web.domain.pointHistory.FailedPointHistoryLog;
import com.my.tosspaymenttest.web.domain.pointHistory.PointHistory;
import com.my.tosspaymenttest.web.domain.pointHistory.PointTransactionType;
import com.my.tosspaymenttest.web.domain.pointHistory.repository.FailedPointHistoryLogRepository;
import com.my.tosspaymenttest.web.domain.user.User;
import com.my.tosspaymenttest.web.domain.user.UserRepository;
import com.my.tosspaymenttest.web.ex.*;
import com.my.tosspaymenttest.web.service.AlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final TossPaymentClient tossPaymentClient;
    private final UserRepository userRepository;
    private final PointRepository pointRepository;
    private final PaymentRepository paymentRepository;
    private final FailedPointHistoryLogRepository failedPointHistoryLogRepository;
    private final AlertService alertService;
    private static final String PAYMENT_CANCEL_SUCCESS_STATE = "DONE";

    //TODO: 재시도 메커니즘 구현 예정

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public PointHistory savePointHistory(Point point){
        try{
            PointHistory pointHistory = PointHistory.builder()
                    .point(point)
                    .pointTransactionType(PointTransactionType.CHARGE)
                    .amount(point.getAmount())
                    .build();
            log.info("포인트 충전 내역 생성 완료. userId = {}, pointHistoryId = {}", point.getUser().getId(), pointHistory.getId());
            return pointHistory;
        }catch (Exception e){
            log.error("[포인트 충전 내역 저장 중 오류 발생] cause = {}, userId = {}",e.getMessage(), point.getUser().getId());

            //TODO 비동기로 재시도 큐에 등록. 구현할 것


            //예외는 던지되 메인 트랜잭션은 커밋되도록 한다
            throw new PointHistoryException("포인트 충전 이력 저장 실패. 추후 재시도 예정", e);

        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveFailedPointHistoryLog(Point point, Exception e){
        FailedPointHistoryLog failedPointHistoryLog = FailedPointHistoryLog.builder()
                .pointId(point.getId())
                .userId(point.getUser().getId())
                .amount(point.getAmount())
                .errorMessage(e.getMessage())
                .build();
        failedPointHistoryLogRepository.save(failedPointHistoryLog);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Point chargePoint(User user, Payment payment, PaymentReqDto paymentReqDto){
        try {
            Point point = getOrCreatePoint(user);
            point.charge(payment.getAmount());

            log.info("포인트 충전 완료. userId = {}, pointId = {}", user.getId(), point.getId());
            return point;
        }catch (Exception e){
            log.error("[포인트 충전 중 오류 발생] cause = {}, userId = {}", e.getMessage(), user.getId());
            throw new PointChargeException("포인트 충전 실패", e);
        }
    }


    //payment 상태를 취소로 바꾸고 취소 사유를 기재한다
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updatePaymentStatus(Payment payment, TossPaymentRespDto canceledPaymentInfo) {
        try {
            payment.updateFailInfo(
                    PaymentCancelReason.SERVER_ERROR_FAIL_TO_UPDATE_POINT.getMessage(),
                    canceledPaymentInfo.getStatus()
            );
        }catch(Exception e){
            log.error("[결제 내역 취소 상태로 변경 중 오류 발생] cause = {}, paymentId = {}, paymentKey = {}", e.getMessage(), payment.getId(), canceledPaymentInfo.getPaymentKey());
            throw e;
        }
    }


    public Point getOrCreatePoint(User user){
        return pointRepository.findByUser(user)
                .orElseGet(() -> Point.builder()
                        .user(user)
                        .amount(0)
                        .build()
                );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Payment savePaymentInfo(PaymentReqDto paymentReqDto, TossPaymentRespDto tossPaymentRespDto, User user){
        try {
            Payment payment = paymentRepository.save(paymentReqDto.toEntity(
                    tossPaymentRespDto,
                    user,
                    PaymentType.POINT_CHARGE));
            log.info("결제 내역 생성 완료. userId = {}, paymentId = {}", user.getId(), payment.getId());
            return payment;
        }catch(Exception e){
            log.error("[결제 내역 저장 중 오류 발생] cause = {}, userId = {}", e.getMessage(), user.getId());
            cancelPaymentAndThrowWhenFailToSavePayment(paymentReqDto, user, e);
            throw new PaymentSaveException("결제 정보 저장 실패", e);
        }
    }

    public TossPaymentRespDto cancelPaymentAndThrowWhenFailToUpdatePoint(PaymentReqDto paymentReqDto, User user, Exception e){
        //토스페이먼츠 결제 취소 요청
        TossPaymentRespDto canceledPaymentInfo = cancelPaymentWhenFailToUpdatePoint(paymentReqDto);

        //결제 취소 상태 다시 확인 -> DONE 이면 결제 취소 정상 수행
        doubleCheckWhenPaymentCanceled(user, e, canceledPaymentInfo);
        return canceledPaymentInfo;
    }

    private void cancelPaymentAndThrowWhenFailToSavePayment(PaymentReqDto paymentReqDto, User user, Exception e){
        //토스페이먼츠 결제 취소 요청
        TossPaymentRespDto canceledPaymentInfo = cancelPaymentWhenFailToSavePayment(paymentReqDto);

        //결제 취소 상태 다시 확인 -> DONE 이면 결제 취소 정상 수행
        doubleCheckWhenPaymentCanceled(user, e, canceledPaymentInfo);
    }


    public User validateUserAndPayment(PaymentReqDto paymentReqDto){
        //결제 요청 유저 조회
        User user = userRepository.findById(paymentReqDto.getUserId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 유저"));

        //사용자의 결제 정보가 일치하는지 확인
        validatePaymentInfo(paymentReqDto);

        return user;
    }


    private TossPaymentRespDto cancelPaymentWhenFailToSavePayment(PaymentReqDto paymentReqDto){
        return tossPaymentClient.cancelPayment(
                paymentReqDto.getPaymentKey(),
                new TossPaymentCancelReqDto(PaymentCancelReason.SERVER_ERROR_FAIL_TO_SAVE_PAYMENT.getMessage())
        );
    }


    private TossPaymentRespDto cancelPaymentWhenFailToUpdatePoint(PaymentReqDto paymentReqDto){
        return tossPaymentClient.cancelPayment(
                paymentReqDto.getPaymentKey(),
                new TossPaymentCancelReqDto(PaymentCancelReason.SERVER_ERROR_FAIL_TO_UPDATE_POINT.getMessage())
        );
    }

    private void doubleCheckWhenPaymentCanceled(User user, Exception e, TossPaymentRespDto canceledPaymentInfo) {
        String cancelStatus = canceledPaymentInfo.getCancels().stream()
                .findFirst()
                .map(TossPaymentRespDto.Cancels::getCancelStatus)
                .orElseThrow(() -> new CanceledPaymentException("취소 정보가 없습니다", e));
        if(!cancelStatus.equals(PAYMENT_CANCEL_SUCCESS_STATE)) {
            log.error("check 2: [토스페이먼츠 결제 취소 실패] paymentKey={}, orderId={}, totalAmount={}, status = {} ",
                    canceledPaymentInfo.getPaymentKey(),
                    canceledPaymentInfo.getOrderId(),
                    canceledPaymentInfo.getTotalAmount(),
                    cancelStatus
            );
            String errorMessage = String.format("결제 취소 double checking에서 불일치 발생: userId = %d, paymentKey = %s, orderId = %s, status = %s",
                    user.getId(),
                    canceledPaymentInfo.getPaymentKey(),
                    canceledPaymentInfo.getOrderId(),
                    cancelStatus
            );
            throw new CanceledPaymentException(errorMessage, e);
        }
    }


    private void validatePaymentInfo(PaymentReqDto paymentReqDto){
        TossPaymentRespDto shouldBeCheck = tossPaymentClient.checkPaymentInfo(paymentReqDto.getPaymentKey());
        List<String> validationErrors = new ArrayList<>();

        if(!paymentReqDto.getAmount().equals(shouldBeCheck.getTotalAmount())) {
            validationErrors.add(String.format("금액 불일치: shouldBe = %d, but request is %s",
                    shouldBeCheck.getTotalAmount(), paymentReqDto.getAmount()));
        }
        if(!paymentReqDto.getOrderId().equals(shouldBeCheck.getOrderId())) {
            validationErrors.add(String.format("주문 번호 불일치: shouldBe = %s, but request is %s",
                    shouldBeCheck.getOrderId(), paymentReqDto.getOrderId()));
        }
        if(!paymentReqDto.getPaymentKey().equals(shouldBeCheck.getPaymentKey())) {
            validationErrors.add(String.format("토스 paymentKey 불일치: shouldBe = %s, but request is %s",
                    shouldBeCheck.getPaymentKey(), paymentReqDto.getPaymentKey()));
        }

        if(!validationErrors.isEmpty()) {
            throw new PaymentValidationException(String.join("\n", validationErrors));
        }
        log.debug("결제 정보 무결성 검증 완료");
    }

}
