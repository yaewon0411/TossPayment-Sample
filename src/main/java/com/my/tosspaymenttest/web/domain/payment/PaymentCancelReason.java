package com.my.tosspaymenttest.web.domain.payment;

import lombok.Getter;

@Getter
public enum PaymentCancelReason {
    SERVER_ERROR_FAIL_TO_SAVE_PAYMENT("서버 에러로 결제 내역 저장 실패"),
    SERVER_ERROR_FAIL_TO_UPDATE_POINT("서버 에러로 포인트 충전 실패");

    private final String message;

    PaymentCancelReason(String message) {
        this.message = message;
    }
}
