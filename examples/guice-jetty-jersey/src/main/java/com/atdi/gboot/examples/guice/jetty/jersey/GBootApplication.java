package com.atdi.gboot.examples.guice.jetty.jersey;


import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.servlet.ServletProperties;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class GBootApplication {

    private static final String APPLICATION_PROPERTIES = "application.properties";

    private static final String JERSEY_SERVLET_ROOT_PATH = "gboot.jersey.path";

    private static final String SERVER_PORT = "gboot.server.port";

    private final Server server;

    private int port;

    private String jerseyRootPath ;

    public GBootApplication(String resourceConfigClassName, String args[]) {
        readProperties(args);
        server = new Server(port);
        ServletContextHandler sch = new ServletContextHandler(server, "/");
        sch.addServlet(DefaultServlet.class, "/");

        ServletHolder jerseyServletHolder = new ServletHolder(new ServletContainer());
        jerseyServletHolder.setInitParameter(ServletProperties.JAXRS_APPLICATION_CLASS, resourceConfigClassName);
        sch.addServlet(jerseyServletHolder, "/" + jerseyRootPath + "/*");
    }

    private void readProperties(String[] args) {
        Properties props = new Properties();
        if(args != null && args.length > 0) {
            try(InputStream resourceStream = new FileInputStream(new File(args[0]))) {
                props.load(resourceStream);}
            catch (IOException e) {
                throw new RuntimeException("Specified properties file could not be found", e);
            }
        } else {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();

            try(InputStream resourceStream = loader.getResourceAsStream(APPLICATION_PROPERTIES)) {
                props.load(resourceStream);
            } catch (IOException e) {

            }
        }

        port = Integer.valueOf(props.getProperty(SERVER_PORT, "8000"));

        jerseyRootPath = props.getProperty(JERSEY_SERVLET_ROOT_PATH, "api");

    }

    public void start() throws Exception {
        server.start();
        server.join();
    }


    public void stop() throws Exception {
        server.stop();
    }

    //private int readProperty(String property, int defaultValue) {

    //}




}
