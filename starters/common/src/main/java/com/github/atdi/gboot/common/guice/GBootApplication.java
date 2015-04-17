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
package com.github.atdi.gboot.common.guice;

import com.github.atdi.gboot.common.guice.modules.AppConfigurationModule;
import com.google.inject.Module;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Base application class, that must be inherited
 * by the custom implementations for different servers.
 */
public abstract class GBootApplication {

    private static final String APPLICATION_PROPERTIES = "application.properties";

    protected static final String JERSEY_SERVLET_ROOT_PATH = "gboot.jersey.path";

    protected static final String SERVER_PORT = "gboot.server.port";

    private int port;

    private String jerseyRootPath;

    private AppConfigurationModule configurationModule;
    /**
     * Default constructor.
     * @param args main class arguments
     */
    public GBootApplication(String args[]) {
        readProperties(args);
    }

    private void readProperties(String[] args) {
        Properties props = new Properties();

        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        try(InputStream resourceStream = loader.getResourceAsStream(APPLICATION_PROPERTIES)) {
            props.load(resourceStream);
        } catch (IOException e) {

        }

        if(args != null && args.length > 0) {
            try(InputStream resourceStream = new FileInputStream(new File(args[0]))) {
                props.load(resourceStream);}
            catch (IOException e) {
                throw new RuntimeException("Specified properties file could not be found", e);
            }
        }



        port = Integer.valueOf(props.getProperty(SERVER_PORT, "8000"));

        jerseyRootPath = props.getProperty(JERSEY_SERVLET_ROOT_PATH, "api");

        configurationModule = new AppConfigurationModule(props);
    }

    protected int getPort() {
        return port;
    }

    protected String getJerseyRootPath() {
        return jerseyRootPath;
    }

    protected Module getConfigurationModule() {
        return configurationModule;
    }

    /**
     * Start application.
     */
    public abstract void start() throws Exception;
}
