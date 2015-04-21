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
package com.github.atdi.gboot.jms;

import com.google.inject.AbstractModule;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Guice JMS module.
 */
public class JmsModule extends AbstractModule {

    private final Destination destination;

    private final Session session;

    private final Connection connection;

    private JmsModule(Session session, Destination destination, Connection connection) {
        this.destination = destination;
        this.session = session;
        this.connection = connection;
    }

    protected void configure() {
        bind(Destination.class).toInstance(destination);
        bind(Session.class).toInstance(session);
        bind(Connection.class).toInstance(connection);
        bind(GBootJmsTemplate.class).to(GBootJmsTemplateImpl.class);
    }

    public static class Builder {
        private String queue;
        private Context context = null;
        private MessageListener listener;
        private ConnectionFactory connectionFactory;
        private boolean usingJNDI = false;

        public JmsModule buildModule() {
            try {
                Session session;
                Destination destination;
                Connection connection;
                if (usingJNDI) {
                    if (context == null)
                        context = new InitialContext();
                    connectionFactory = (ConnectionFactory) context.lookup("ConnectionFactory");
                    connection = connectionFactory.createConnection();
                    session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                    destination = (Destination) context.lookup(this.queue);
                } else {
                    connection = connectionFactory.createConnection();
                    session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                    destination = session.createQueue(queue);
                }

                if (listener != null) {
                    MessageConsumer consumer = session.createConsumer(destination);
                    consumer.setMessageListener(listener);
                    connection.start();
                }

                return new JmsModule(session, destination, connection);
            } catch (JMSException e) {
                throw new GBootJmsException("Could not connect to jms destination", e);
            } catch (NamingException e) {
                throw new GBootJmsException("Could not create jndi context", e);
            }
        }

        public Builder usingJNDI() {
            this.usingJNDI = true;
            return this;
        }

        public Builder queue(String queue) {
            this.queue = queue;
            return this;
        }

        public Builder context(Context context) {
            this.context = context;
            return this;
        }

        public Builder withListener(MessageListener listener) {
            this.listener = listener;
            return this;
        }

        public Builder withConnectionFactory(ConnectionFactory cf) {
            this.connectionFactory = cf;

            return this;
        }
    }
}
