package com.github.atdi.gboot.ampq;

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
        factory.setUri("amqp://guest:guest@localhost:5672");
    }

    @Test
    public void testSend() throws Exception {
        connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare("amq.direct", "direct", true);
        channel.queueDeclare("test", true, false, false, null);
        channel.queueBind("test", "amq.direct", "test");
        byte[] messageBodyBytes = "Hello, world111!".getBytes();
        channel.basicPublish("amq.direct", "test", null, messageBodyBytes);
        channel.close();
        connection.close();
    }
}