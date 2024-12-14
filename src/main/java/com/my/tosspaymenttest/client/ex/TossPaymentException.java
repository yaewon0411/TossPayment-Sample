package com.my.tosspaymenttest.client.ex;

import lombok.Getter;

@Getter
//토스 결제 에러
public class TossPaymentException extends RuntimeException{

    private final TossPaymentErrorCode errorCode;
    private final String paymentKey;

    public TossPaymentException(TossPaymentErrorCode errorCode, String paymentKey, String message) {
        super(message);
        this.errorCode = errorCode;
        this.paymentKey = paymentKey;
    }

}
