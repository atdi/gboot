package com.github.atdi.gboot.amqp;

public class GBootJmsException extends RuntimeException {
    public GBootJmsException(String message, Throwable e) {
        super(message, e);
    }
}
