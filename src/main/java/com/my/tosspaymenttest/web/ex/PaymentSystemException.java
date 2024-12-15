package com.my.tosspaymenttest.web.ex;

public class PaymentSystemException extends RuntimeException{
    private final String message;

    public PaymentSystemException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
    }
}
