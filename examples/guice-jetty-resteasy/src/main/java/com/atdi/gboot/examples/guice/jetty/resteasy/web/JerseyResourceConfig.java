package com.atdi.gboot.examples.guice.jetty.resteasy.web;


import com.atdi.gboot.examples.guice.jetty.resteasy.modules.PersistenceModule;
import com.google.inject.Guice;
import com.google.inject.servlet.ServletModule;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.ResourceConfig;
import org.jvnet.hk2.guice.bridge.api.GuiceBridge;
import org.jvnet.hk2.guice.bridge.api.GuiceIntoHK2Bridge;

import javax.inject.Inject;

public class JerseyResourceConfig extends ResourceConfig {

    @Inject
    public JerseyResourceConfig(ServiceLocator serviceLocator) {
        // Set package to look for resources in
        packages("com.atdi.gboot.examples.guice.jetty.resteasy.resources");

        //log.info("Registering injectables...");

        GuiceBridge.getGuiceBridge().initializeGuiceBridge(serviceLocator);

        GuiceIntoHK2Bridge guiceBridge = serviceLocator.getService(GuiceIntoHK2Bridge.class);
        guiceBridge.bridgeGuiceInjector(Guice.createInjector(new ServletModule() {
            // Configure your IOC
            @Override
            protected void configureServlets() {
                //PackagesResourceConfig resourceConfig = new PackagesResourceConfig("jersey.resources.package");
                //for (Class<?> resource : resourceConfig.getClasses()) {
                //}
            }
        }, new PersistenceModule("demo-guice-boot")));

    }
}
