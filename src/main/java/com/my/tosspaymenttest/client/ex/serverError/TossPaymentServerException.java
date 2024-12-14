package com.my.tosspaymenttest.client.ex.serverError;

import com.my.tosspaymenttest.client.ex.TossPaymentErrorCode;
import com.my.tosspaymenttest.client.ex.TossPaymentException;

public class TossPaymentServerException extends TossPaymentException {
    public TossPaymentServerException(TossPaymentErrorCode errorCode, String paymentKey) {
        super(errorCode, paymentKey, errorCode.getMessage());
    }
}
