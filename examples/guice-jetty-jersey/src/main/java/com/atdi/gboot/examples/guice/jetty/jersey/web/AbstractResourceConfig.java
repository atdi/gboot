package com.atdi.gboot.examples.guice.jetty.jersey.web;


import com.atdi.gboot.examples.guice.jetty.jersey.GuiceContextListener;
import com.google.inject.Guice;
import com.google.inject.Module;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.ResourceConfig;
import org.jvnet.hk2.guice.bridge.api.GuiceBridge;
import org.jvnet.hk2.guice.bridge.api.GuiceIntoHK2Bridge;


public abstract class AbstractResourceConfig extends ResourceConfig {


    protected AbstractResourceConfig(ServiceLocator serviceLocator,
                                     Module... modules) {
        GuiceBridge.getGuiceBridge().initializeGuiceBridge(serviceLocator);

        GuiceIntoHK2Bridge guiceBridge = serviceLocator.getService(GuiceIntoHK2Bridge.class);
        guiceBridge.bridgeGuiceInjector(GuiceContextListener.injector.createChildInjector(modules));
    }
}
