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

/**
 * AmqpConnectionFactory interface.
 */
public interface AmqpConnectionFactory {

    /**
     * Set connection uri.
     * @param uri string uri
     */
    void setUri(String uri);

    /**
     * Create a new connection.
     * @return connection
     * @throws java.io.IOException in case of an error IOException is thrown
     */
    AmqpConnection newConnection() throws IOException;

}