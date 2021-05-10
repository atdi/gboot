package com.github.atdi.gboot.amqp;

import com.rabbitmq.client.Channel;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Map;


public class RabbitmqChannel implements AmqpChannel {

    private final Channel channel;

    @Inject
    public RabbitmqChannel(Channel channel) {
        this.channel = channel;
    }

    @Override
    public void queueDeclareNoWait(String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments) throws IOException {
        channel.queueDeclareNoWait(queue, durable, exclusive, autoDelete, arguments);
    }

    @Override
    public void close() throws IOException {
        channel.close();
    }
}
