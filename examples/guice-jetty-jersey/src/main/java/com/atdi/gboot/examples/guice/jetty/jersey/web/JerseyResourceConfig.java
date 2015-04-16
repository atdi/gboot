package com.atdi.gboot.examples.guice.jetty.jersey.web;


import com.atdi.gboot.examples.guice.jetty.jersey.modules.PersistenceModule;
import org.glassfish.hk2.api.ServiceLocator;
import javax.inject.Inject;

public class JerseyResourceConfig extends AbstractResourceConfig {

    @Inject
    public JerseyResourceConfig(ServiceLocator serviceLocator) {
        super(serviceLocator, new PersistenceModule("demo-guice-boot"));
        // Set package to look for resources in
        packages("com.atdi.gboot.examples.guice.jetty.jersey.resources");
    }
}
