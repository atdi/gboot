package com.github.atdi.gboot.amqp;

import com.rabbitmq.client.*;
import org.junit.BeforeClass;
import org.junit.Test;
import com.rabbitmq.client.Connection;

public class JmsTemplateTest {

    private Connection connection;
    private static ConnectionFactory factory;
    @BeforeClass
    public static void setUp() throws Exception {
        factory = new ConnectionFactory();
        factory.setUsername("guest");
        factory.setPassword("guest");
        factory.setVirtualHost("/");
        factory.setHost("localhost");
        factory.setPort(5672);
        factory.setAutomaticRecoveryEnabled(true);
    }

    @Test
    public void testSend() throws Exception {
        connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare("amq.direct", "direct", true);
        channel.queueDeclareNoWait("test", true, false, false, null);
        channel.queueBind("test", "amq.direct", "test");
        byte[] messageBodyBytes = "Hello, world111!".getBytes();
        channel.basicPublish("amq.direct", "test", null, messageBodyBytes);
        channel.close();
        connection.close();
    }
}