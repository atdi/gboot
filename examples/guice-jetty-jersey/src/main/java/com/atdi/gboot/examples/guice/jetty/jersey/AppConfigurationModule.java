package com.atdi.gboot.examples.guice.jetty.jersey;


import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import java.util.Properties;

public class AppConfigurationModule extends AbstractModule {

    private final Properties properties;

    public AppConfigurationModule(Properties properties) {
        this.properties = properties;
    }

    @Override
    protected void configure() {
        Names.bindProperties(binder(), properties);
    }
}
