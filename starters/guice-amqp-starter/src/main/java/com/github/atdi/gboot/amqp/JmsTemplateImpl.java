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
package com.github.atdi.gboot.amqp;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;


/**
 * JMS template impl.
 */
public class JmsTemplateImpl implements JmsTemplate {

    private final Session session;
    private final Destination destination;
    private final Connection connection;


    public JmsTemplateImpl(Session session, Destination destination, Connection connection) {
        this.session = session;
        this.destination = destination;
        this.connection = connection;
    }

    @Override
    public void send(MessageCreator messageCreator) throws JMSException {
        Message message = messageCreator.createMessage(session);
        session.createProducer(destination).send(message);
    }

    @Override
    public Message receive() throws JMSException {
        MessageConsumer consumer = session.createConsumer(destination);
        connection.start();
        return consumer.receive();
    }

    @Override
    public void close() throws Exception {
        connection.close();
        session.close();
    }
}
