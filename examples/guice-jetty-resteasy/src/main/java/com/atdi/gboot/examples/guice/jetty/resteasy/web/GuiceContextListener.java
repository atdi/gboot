package com.atdi.gboot.examples.guice.jetty.resteasy.web;

import com.atdi.gboot.examples.guice.jetty.resteasy.modules.PersistenceModule;
import com.atdi.gboot.examples.guice.jetty.resteasy.modules.ResourcesModule;
import com.google.inject.Module;
import org.jboss.resteasy.plugins.guice.GuiceResteasyBootstrapServletContextListener;

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.List;

/**
 * Servlet context listener for loading guice modules.
 */
public class GuiceContextListener extends GuiceResteasyBootstrapServletContextListener {

    private final String persistenceUnit;

    public GuiceContextListener(String persistenceUnit) {
        this.persistenceUnit = persistenceUnit;
    }


    /**
     * Override this method to instantiate your {@link com.google.inject.Module}s yourself.
     *
     * @param context
     * @return
     */
    protected List<? extends Module> getModules(final ServletContext context)
    {
        final List<Module> result = new ArrayList<Module>();
        result.add(new PersistenceModule(persistenceUnit));
        result.add(new ResourcesModule());
        return result;
    }
}
