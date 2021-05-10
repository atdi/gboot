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

import java.io.IOException;
import java.util.Map;

/**
 * Ampq channel interface.
 */
public interface AmqpChannel extends AutoCloseable {

    /**
     * Create queue.
     * @param queue the name of the queue
     * @param durable true if we are declaring a durable queue (the queue will survive a server restart)
     * @param exclusive true if we are declaring an exclusive queue (restricted to this connection)
     * @param autoDelete true if we are declaring an autodelete queue (server will delete it when no longer in use)
     * @param arguments other properties (construction arguments) for the queue
     * @throws java.io.IOException if an error is encountered
     */
    void queueDeclareNoWait(String queue, boolean durable, boolean exclusive, boolean autoDelete,
                            Map<String, Object> arguments) throws IOException;


    /**
     * Close this channel.
     *
     * @throws java.io.IOException if an error is encountered
     */
    void close() throws IOException;
}
