package com.my.tosspaymenttest.web.ex;

import lombok.Getter;

@Getter
public class PointHistoryException extends RuntimeException{

    private final String message;

    public PointHistoryException(String message, Throwable e) {
        super(message, e);
        this.message = message;
    }
}
