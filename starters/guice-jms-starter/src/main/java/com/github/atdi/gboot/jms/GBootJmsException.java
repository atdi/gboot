package com.github.atdi.gboot.jms;

import javax.jms.JMSException;

public class GBootJmsException extends RuntimeException {
    public GBootJmsException(String message, Throwable e) {
        super(message, e);
    }
}
