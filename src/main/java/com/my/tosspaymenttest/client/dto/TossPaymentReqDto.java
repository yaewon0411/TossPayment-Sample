package com.my.tosspaymenttest.client.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class TossPaymentReqDto {

    private String orderId;
    private Integer amount;
    private String paymentKey;

    public TossPaymentReqDto(String orderId, Integer amount, String paymentKey) {
        this.orderId = orderId;
        this.amount = amount;
        this.paymentKey = paymentKey;
    }
}
