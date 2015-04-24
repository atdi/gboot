/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.atdi.gboot.ampq;

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * Jms template interface.
 */
public interface JmsTemplate extends AutoCloseable {

    /**
     * Put message on queue.
     * @param messageCreator message creator
     * @throws JMSException exception
     */
    public void send(MessageCreator messageCreator) throws JMSException;

    /**
     * Receive message.
     * @return message
     * @throws JMSException exception
     */
    public Message receive() throws JMSException;

}
