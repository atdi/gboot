package com.atdi.gboot.examples.guice.jetty.jersey.web;


import com.atdi.gboot.examples.guice.jetty.jersey.modules.PersistenceModule;
import com.google.inject.Guice;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.ResourceConfig;
import org.jvnet.hk2.guice.bridge.api.GuiceBridge;
import org.jvnet.hk2.guice.bridge.api.GuiceIntoHK2Bridge;

import javax.inject.Inject;

public class JerseyResourceConfig extends AbstractResourceConfig {

    @Inject
    public JerseyResourceConfig(ServiceLocator serviceLocator) {
        super(serviceLocator, new PersistenceModule("demo-guice-boot"));
        // Set package to look for resources in
        packages("com.atdi.gboot.examples.guice.jetty.jersey.resources");
    }
}
