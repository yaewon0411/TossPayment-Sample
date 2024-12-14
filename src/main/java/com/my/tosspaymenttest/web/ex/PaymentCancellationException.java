package com.my.tosspaymenttest.web.ex;

import lombok.Getter;

@Getter
public class PaymentCancellationException extends RuntimeException{

    private final String message;

    public PaymentCancellationException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
    }
}
