package com.my.tosspaymenttest.client.ex;

import lombok.Getter;

@Getter
public enum PaymentFeature {

    CONFIRM("결제 승인"),
    INQUIRY("결제 조회"),
    CANCEL("결제 취소");

    private final String description;

    PaymentFeature(String description) {
        this.description = description;
    }
}
