package com.github.atdi.gboot.gtj.test.utils;


import com.github.atdi.gboot.common.guice.web.AbstractResourceConfig;
import org.glassfish.hk2.api.ServiceLocator;

import javax.inject.Inject;

public class JerseyResourceConfig extends AbstractResourceConfig {

    /**
     * Default constructor.
     *
     * @param serviceLocator injected service locator by HK2 DI.
     */
    @Inject
    protected JerseyResourceConfig(ServiceLocator serviceLocator) {
        super(serviceLocator);
        packages("com.github.atdi.gboot.gtj.test.utils");
    }
}
