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
package com.github.atdi.gboot.gjj;

import com.github.atdi.gboot.common.guice.GBootApplication;
import com.github.atdi.gboot.common.guice.web.GBootServletContextListener;
import com.google.inject.servlet.GuiceFilter;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.servlet.ServletProperties;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

/**
 * Embedded jetty application starter.
 */
public class GjjApplication extends GBootApplication {

    private final Server server;

    /**
     * Constructor.
     * @param resourceConfigClassName jersey resource config class name
     * @param args main method arguments
     */
    public GjjApplication(String resourceConfigClassName, String[] args) {
        super(args);
        server = new Server(getPort());
        ServletContextHandler servletContextHandler = new ServletContextHandler(server, "/");
        servletContextHandler.addServlet(DefaultServlet.class, "/");
        servletContextHandler.addFilter(GuiceFilter.class, "/*", EnumSet.allOf(DispatcherType.class));
        servletContextHandler.addEventListener(new GBootServletContextListener(getConfigurationModule()));
        ServletHolder jerseyServletHolder = new ServletHolder(new ServletContainer());
        jerseyServletHolder.setInitParameter(ServletProperties.JAXRS_APPLICATION_CLASS, resourceConfigClassName);
        servletContextHandler.addServlet(jerseyServletHolder, "/" + getJerseyRootPath() + "/*");
    }

    @Override
    public void start() throws Exception {
        server.start();
        server.join();
    }
}
