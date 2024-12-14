package com.my.tosspaymenttest.client.ex.notFound;

import com.my.tosspaymenttest.client.ex.TossPaymentErrorCode;
import com.my.tosspaymenttest.client.ex.TossPaymentException;

public class TossPaymentNotFoundException extends TossPaymentException {

    public TossPaymentNotFoundException(TossPaymentErrorCode errorCode, String paymentKey) {
        super(errorCode, paymentKey, errorCode.getMessage());
    }
}
