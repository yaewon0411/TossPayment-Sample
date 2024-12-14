package com.my.tosspaymenttest.client.ex.badRequest;

import com.my.tosspaymenttest.client.ex.TossPaymentErrorCode;
import com.my.tosspaymenttest.client.ex.TossPaymentException;
import lombok.Getter;

public class TossPaymentBadRequestException extends TossPaymentException {

    public TossPaymentBadRequestException(TossPaymentErrorCode errorCode, String paymentKey) {
        super(errorCode, paymentKey, errorCode.getMessage());
    }
}
