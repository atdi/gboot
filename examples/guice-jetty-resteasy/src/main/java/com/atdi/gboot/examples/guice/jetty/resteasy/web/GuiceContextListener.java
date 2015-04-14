package com.atdi.gboot.examples.guice.jetty.resteasy.web;

import com.atdi.gboot.examples.guice.jetty.resteasy.modules.PersistenceModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;

public class GuiceContextListener extends GuiceServletContextListener {

    public static Injector injector;

    @Override
    protected Injector getInjector() {
        injector = Guice.createInjector(new ServletModule() {
            // Configure your IOC
            @Override
            protected void configureServlets() {
                //PackagesResourceConfig resourceConfig = new PackagesResourceConfig("jersey.resources.package");
                //for (Class<?> resource : resourceConfig.getClasses()) {
                //}
            }
        }, new PersistenceModule("demo-guice-boot"));

        return injector;
    }
}
