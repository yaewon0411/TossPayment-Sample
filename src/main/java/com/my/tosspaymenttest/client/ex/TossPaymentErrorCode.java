package com.my.tosspaymenttest.client.ex;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TossPaymentErrorCode {
    // 400 Bad Request
    ALREADY_PROCESSED_PAYMENT("이미 처리된 결제입니다"),
    PROVIDER_ERROR("일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요"),
    INVALID_REQUEST("잘못된 요청입니다"),
    INVALID_API_KEY("잘못된 시크릿키 연동 정보입니다"),
    INVALID_REJECT_CARD("카드 사용이 거절되었습니다. 카드사 문의가 필요합니다"),
    INVALID_CARD_EXPIRATION("카드 정보를 다시 확인해주세요. (유효기간)"),
    INVALID_STOPPED_CARD("정지된 카드 입니다"),
    EXCEED_MAX_DAILY_PAYMENT_COUNT("하루 결제 가능 횟수를 초과했습니다"),
    NOT_SUPPORTED_INSTALLMENT_PLAN_CARD_OR_MERCHANT("할부가 지원되지 않는 카드 또는 가맹점 입니다."),
    EXCEED_MAX_PAYMENT_AMOUNT("하루 결제 가능 금액을 초과했습니다"),
    CARD_PROCESSING_ERROR("카드사에서 오류가 발생했습니다"),
    NOT_AVAILABLE_PAYMENT("결제가 불가능한 시간대입니다"),
    NOT_SUPPORTED_MONTHLY_INSTALLMENT_PLAN_BELOW_AMOUNT("5만원 이하의 결제는 할부가 불가능해서 결제에 실패했습니다"),
    ALREADY_CANCELED_PAYMENT("이미 취소된 결제 입니다"),
    REFUND_REJECTED("환불이 거절됐습니다. 결제사에 문의 부탁드립니다."),
    ALREADY_REFUND_PAYMENT("이미 환불된 결제입니다."),


    // 401 Unauthorized
    UNAUTHORIZED_KEY("인증되지 않은 시크릿 키 혹은 클라이언트 키입니다"),

    // 403 Forbidden
    REJECT_CARD_PAYMENT("한도초과 혹은 잔액부족으로 결제에 실패했습니다"),
    REJECT_CARD_COMPANY("결제 승인이 거절되었습니다"),
    FORBIDDEN_REQUEST("허용되지 않은 요청입니다"),
    NOT_CANCELABLE_AMOUNT("취소 할 수 없는 금액 입니다."),
    FORBIDDEN_CONSECUTIVE_REQUEST("반복적인 요청은 허용되지 않습니다. 잠시 후 다시 시도해주세요."),
    NOT_CANCELABLE_PAYMENT("취소 할 수 없는 결제 입니다."),
    NOT_AVAILABLE_BANK("은행 서비스 시간이 아닙니다."),


    // 404 Not Found
    NOT_FOUND_PAYMENT("존재하지 않는 결제 정보입니다"),
    NOT_FOUND_PAYMENT_SESSION("결제 시간이 만료되어 결제 진행 데이터가 존재하지 않습니다"),

    // 500 Internal Server Error
    FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING("결제가 완료되지 않았어요. 다시 시도해주세요"),
    FAILED_INTERNAL_SYSTEM_PROCESSING("내부 시스템 처리 작업이 실패했습니다. 잠시 후 다시 시도해주세요"),
    UNKNOWN_PAYMENT_ERROR("결제에 실패했어요. 같은 문제가 반복된다면 은행이나 카드사로 문의해주세요"),

    //정의되지 않은 오류
    UNKNOWN_ERROR("알 수 없는 오류가 발생했습니다. 잠시 후 다시 시도해주세요");

    private final String message;

    public static TossPaymentErrorCode fromString(String code) {
        try {
            return TossPaymentErrorCode.valueOf(code);
        } catch (IllegalArgumentException e) {
            return UNKNOWN_PAYMENT_ERROR;
        }
    }
}
