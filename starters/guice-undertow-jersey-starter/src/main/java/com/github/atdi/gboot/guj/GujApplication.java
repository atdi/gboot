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
import com.github.atdi.gboot.common.guice.web.GuiceInjectorCreator;
import com.google.inject.Module;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.session.InMemorySessionManager;
import io.undertow.server.session.SessionAttachmentHandler;
import io.undertow.server.session.SessionCookieConfig;
import io.undertow.server.session.SessionManager;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.servlet.ServletProperties;

import javax.servlet.ServletException;

/**
 * Embedded undertow application starter.
 * @param <T> clustering session manager
 */
public class GujApplication<T extends SessionManager> extends GBootApplication {

    private Undertow server;


    /**
     * Constructor.
     * @param resourceConfigClassName
     * @param args
     * @param sessionManager
     * @param modules
     * @throws ServletException
     */
    public GujApplication(String resourceConfigClassName, String[] args, T sessionManager, Module... modules) throws ServletException {
        super(args);
        // Modules
        Module[] tempModules = new Module[1];
        int moduleIndex = 0;
        if(modules == null) {
            tempModules = new Module[modules.length+1];
            moduleIndex = modules.length;
        }
        tempModules[moduleIndex] = getConfigurationModule();
        GuiceInjectorCreator.createInjector(tempModules);
        DeploymentInfo servletBuilder = Servlets.deployment()
                .setClassLoader(Thread.currentThread().getContextClassLoader())
                .setContextPath("/")
                .setDeploymentName("application.war")
                .addServlets(
                        Servlets.servlet("restServlet", ServletContainer.class)
                                .addInitParam(ServletProperties.JAXRS_APPLICATION_CLASS, resourceConfigClassName)
                                .addMapping("/" + getJerseyRootPath()  + "/*"));

        DeploymentManager manager = Servlets.defaultContainer().addDeployment(servletBuilder);
        manager.deploy();
        PathHandler path = Handlers.path(Handlers.redirect("/"))
                .addPrefixPath("/", manager.start());
        SessionManager tempSessionManager = sessionManager;
        if(sessionManager == null) {
            tempSessionManager = new InMemorySessionManager("SESSION_MANAGER");
        }

        SessionAttachmentHandler handler = new SessionAttachmentHandler(tempSessionManager,
                new SessionCookieConfig());
        handler.setNext(path);

        server = Undertow.builder()
                .addHttpListener(getPort(), "0.0.0.0")
                .setHandler(handler).build();

    }

    @Override
    public void start() throws Exception {
        server.start();
    }

    @Override
    public void join() throws Exception {

    }

    @Override
    public void stop() throws Exception {
        server.stop();
    }
}
