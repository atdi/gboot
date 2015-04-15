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
package com.atdi.gboot.examples.guice.jetty.jersey;

import com.atdi.gboot.examples.guice.jetty.jersey.web.JerseyResourceConfig;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.servlet.ServletProperties;

/**
 * Main class.
 */
public class Bootstrap {

    public static void main(String args[]) throws Exception {
        Server server = new Server(8001);
        ServletContextHandler sch = new ServletContextHandler(server, "/");
        sch.addServlet(DefaultServlet.class, "/");

        ServletHolder jerseyServletHolder = new ServletHolder(new ServletContainer());
        jerseyServletHolder.setInitParameter(ServletProperties.JAXRS_APPLICATION_CLASS, JerseyResourceConfig.class.getCanonicalName());
        sch.addServlet(jerseyServletHolder, "/api/*");

        server.start();
        server.join();
    }
}
