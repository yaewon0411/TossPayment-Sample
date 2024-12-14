package com.my.tosspaymenttest.client.ex.forbidden;

import com.my.tosspaymenttest.client.ex.TossPaymentErrorCode;
import com.my.tosspaymenttest.client.ex.TossPaymentException;

public class TossPaymentForbiddenException extends TossPaymentException {
    public TossPaymentForbiddenException(TossPaymentErrorCode errorCode, String paymentKey) {
        super(errorCode, paymentKey, errorCode.getMessage());
    }
}
