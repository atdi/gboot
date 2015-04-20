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
package com.github.atdi.gboot.gtj;

import com.github.atdi.gboot.common.guice.GBootApplication;
import com.github.atdi.gboot.common.guice.web.GuiceInjectorCreator;
import com.google.inject.Module;
import org.apache.catalina.Context;
import org.apache.catalina.Manager;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.servlet.ServletProperties;

import java.io.File;

/**
 * Embedded tomcat application starter.
 * @param <T> tomcat session manager
 */
public class GtjApplication<T extends Manager> extends GBootApplication {

    private Tomcat tomcat;

    /**
     * Constructor.
     * @param resourceConfigClassName resource configuration class name
     * @param args input arguments
     * @param sessionManager session manager
     * @param modules additional guice modules
     */
    public GtjApplication(String resourceConfigClassName, String[] args, T sessionManager, Module... modules) {
        super(args);
        tomcat = new Tomcat();
        tomcat.setPort(getPort());
        File base = new File(System.getProperty("java.io.tmpdir"));
        Context rootCtx = tomcat.addContext("", base.getAbsolutePath());
        if(sessionManager != null) {
            rootCtx.setManager(sessionManager);
        }
        // Modules
        Module[] tempModules = new Module[1];
        int moduleIndex = 0;
        if(modules == null) {
            tempModules = new Module[modules.length+1];
            moduleIndex = modules.length;
        }
        tempModules[moduleIndex] = getConfigurationModule();
        GuiceInjectorCreator.createInjector(tempModules);
        Wrapper wrapper = Tomcat.addServlet(rootCtx, "restServlet",
                new ServletContainer());
        wrapper.addInitParameter(ServletProperties.JAXRS_APPLICATION_CLASS,
                resourceConfigClassName);
        wrapper.setLoadOnStartup(1);

        rootCtx.addServletMapping("/" + getJerseyRootPath() + "/*", "restServlet");

    }

    @Override
    public void start() throws Exception {
        tomcat.start();
    }

    @Override
    public void join() throws Exception {
        tomcat.getServer().await();
    }

    @Override
    public void stop() throws Exception {
        tomcat.stop();
    }
}
