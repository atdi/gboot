package com.atdi.gboot.examples.guice.jetty.jersey.web;


import com.atdi.gboot.examples.guice.jetty.jersey.modules.PersistenceModule;
import com.google.inject.Guice;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.ResourceConfig;
import org.jvnet.hk2.guice.bridge.api.GuiceBridge;
import org.jvnet.hk2.guice.bridge.api.GuiceIntoHK2Bridge;

import javax.inject.Inject;

public class JerseyResourceConfig extends ResourceConfig {

    @Inject
    public JerseyResourceConfig(ServiceLocator serviceLocator) {
        // Set package to look for resources in
        packages("com.atdi.gboot.examples.guice.jetty.jersey.resources");

        //log.info("Registering injectables...");

        GuiceBridge.getGuiceBridge().initializeGuiceBridge(serviceLocator);

        GuiceIntoHK2Bridge guiceBridge = serviceLocator.getService(GuiceIntoHK2Bridge.class);
        guiceBridge.bridgeGuiceInjector(Guice.createInjector(new PersistenceModule("demo-guice-boot")));

    }
}
