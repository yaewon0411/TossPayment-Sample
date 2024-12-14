package com.my.tosspaymenttest.client.ex.unAuthorized;

import com.my.tosspaymenttest.client.ex.TossPaymentErrorCode;
import com.my.tosspaymenttest.client.ex.TossPaymentException;

public class TossPaymentUnauthorizedException extends TossPaymentException {
    public TossPaymentUnauthorizedException(TossPaymentErrorCode errorCode, String paymentKey) {
        super(errorCode, paymentKey, errorCode.getMessage());
    }
}
