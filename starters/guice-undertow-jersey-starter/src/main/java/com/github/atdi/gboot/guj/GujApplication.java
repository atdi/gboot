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
package com.github.atdi.gboot.guj;

import com.github.atdi.gboot.common.guice.GBootApplication;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.session.SessionAttachmentHandler;
import io.undertow.server.session.SessionCookieConfig;
import io.undertow.server.session.SessionManager;
import io.undertow.util.Headers;

/**
 * Embedded undertow application starter.
 */
public class GujApplication<T extends SessionManager> extends GBootApplication {

    private Undertow server;

    /**
     * Default constructor.
     *
     * @param args main class arguments
     */
    public GujApplication(String[] args, T sessionManager) {
        super(args);
        SessionAttachmentHandler handler = new SessionAttachmentHandler(sessionManager, new SessionCookieConfig());
        server = Undertow.builder()
                .addHttpListener(getPort(), "0.0.0.0")
                .setHandler(new HttpHandler() {
                    @Override
                    public void handleRequest(final HttpServerExchange exchange) throws Exception {
                        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                        exchange.getResponseSender().send("Hello World");
                    }
                }).build();

    }

    @Override
    public void start() throws Exception {
        server.start();
    }

    @Override
    public void join() throws Exception {
        // Do nothing
    }

    @Override
    public void stop() throws Exception {
        server.stop();
    }
}
