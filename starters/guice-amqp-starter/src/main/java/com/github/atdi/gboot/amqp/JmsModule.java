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

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Guice JMS module.
 */
public class JmsModule extends AbstractModule {

    private final Map<String, Destination> destinations = new HashMap<>();

    private final Session session;

    private final Connection connection;

    private JmsModule(Session session, Map<String, Destination> destinations, Connection connection) {
        this.destinations.putAll(destinations);
        this.session = session;
        this.connection = connection;
    }

    protected void configure() {
        for (Map.Entry<String, Destination> entry : destinations.entrySet()) {
            bind(Destination.class)
                    .annotatedWith(Names.named(entry.getKey()))
                    .toInstance(entry.getValue());
            bind(JmsTemplate.class).annotatedWith(Names.named(entry.getKey()))
                    .toInstance(new JmsTemplateImpl(session, entry.getValue(), connection));
        }
        bind(Session.class).toInstance(session);
        bind(Connection.class).toInstance(connection);
    }

    public static class Builder {
        private Set<String> queues = new HashSet<>();
        private Set<String> topics = new HashSet<>();
        private Context context = null;
        private Map<String, MessageListener> listeners = new HashMap<>();
        private ConnectionFactory connectionFactory;
        private boolean usingJNDI = false;

        public JmsModule buildModule() {
            try {
                Session session;
                Map<String, Destination> destinations = new HashMap<>();
                Connection connection;
                if (usingJNDI) {
                    if (context == null)
                        context = new InitialContext();
                    connectionFactory = (ConnectionFactory) context.lookup("ConnectionFactory");
                    connection = connectionFactory.createConnection();
                    session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                    for(String queue : queues) {
                        destinations.put(queue, (Destination) context.lookup(queue));
                    }
                } else {
                    connection = connectionFactory.createConnection();
                    session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                    for(String queue : queues) {
                        destinations.put(queue, session.createQueue(queue));
                    }
                }

                for (Map.Entry<String, MessageListener> entry : listeners.entrySet()) {
                    MessageConsumer consumer = session.createConsumer(destinations.get(entry.getKey()));
                    consumer.setMessageListener(entry.getValue());
                    connection.start();
                }

                return new JmsModule(session, destinations, connection);
            } catch (JMSException e) {
                throw new GBootAmqpException("Could not connect to ampq destination", e);
            } catch (NamingException e) {
                throw new GBootAmqpException("Could not create jndi context", e);
            }
        }

        public Builder usingJNDI() {
            this.usingJNDI = true;
            return this;
        }


        public Builder context(Context context) {
            this.context = context;
            return this;
        }

        public Builder addListener(String key, MessageListener listener) {
            this.listeners.put(key, listener);
            return this;
        }

        public Builder withConnectionFactory(ConnectionFactory cf) {
            this.connectionFactory = cf;
            return this;
        }

        public Builder addQueue(String queue) {
            this.queues.add(queue);
            return this;
        }

        public Builder addTopic(String topic) {
            this.topics.add(topic);
            return this;
        }
    }
}
