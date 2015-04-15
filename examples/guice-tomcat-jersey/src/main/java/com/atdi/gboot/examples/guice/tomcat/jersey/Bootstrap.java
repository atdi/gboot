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
package com.atdi.gboot.examples.guice.tomcat.jersey;

import com.atdi.gboot.examples.guice.tomcat.jersey.web.JerseyResourceConfig;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.servlet.ServletProperties;
import org.apache.catalina.Context;
import java.io.File;

/**
 * Main class.
 */
public class Bootstrap {

    public static void main(String args[]) throws Exception {
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8002);
        File base = new File(System.getProperty("java.io.tmpdir"));
        Context rootCtx = tomcat.addContext("", base.getAbsolutePath());
        Wrapper wrapper = Tomcat.addServlet(rootCtx, "restServlet",
                new ServletContainer());
        wrapper.addInitParameter(ServletProperties.JAXRS_APPLICATION_CLASS,
                JerseyResourceConfig.class.getCanonicalName());
        wrapper.setLoadOnStartup(1);
        rootCtx.addServletMapping("/api/*", "restServlet");
        tomcat.start();
        tomcat.getServer().await();
    }
}
