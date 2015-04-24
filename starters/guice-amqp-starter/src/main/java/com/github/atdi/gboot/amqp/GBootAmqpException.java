package com.github.atdi.gboot.amqp;

public class GBootAmqpException extends RuntimeException {
    public GBootAmqpException(String message, Throwable e) {
        super(message, e);
    }
}
