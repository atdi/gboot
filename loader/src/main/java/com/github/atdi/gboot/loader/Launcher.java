package com.github.atdi.gboot.loader;

import com.github.atdi.gboot.loader.archive.Archive;
import com.github.atdi.gboot.loader.archive.ExplodedArchive;
import com.github.atdi.gboot.loader.archive.JarFileArchive;
import com.github.atdi.gboot.loader.jar.GBootJarFile;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Base class for launchers that can start an application with a fully configured
 * classpath backed by one or more {@link com.github.atdi.gboot.loader.archive.Archive}s.
 *
 */
public abstract class Launcher {

    protected Logger logger = Logger.getLogger(Launcher.class.getName());

    /**
     * The main runner class. This must be loaded by the created ClassLoader so cannot be
     * directly referenced.
     */
    private static final String RUNNER_CLASS = Launcher.class.getPackage().getName()
            + ".MainClassRunner";

    /**
     * Launch the application. This method is the initial entry point that should be
     * called by a subclass {@code public static void main(String[] args)} method.
     * @param args the incoming arguments
     */
    protected void launch(String[] args) {
        try {
            GBootJarFile.registerUrlProtocolHandler();
            ClassLoader classLoader = createClassLoader(getClassPathArchives());
            launch(args, getMainClass(), classLoader);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Create a classloader for the specified archives.
     * @param archives the archives
     * @return the classloader
     * @throws Exception
     */
    protected ClassLoader createClassLoader(List<Archive> archives) throws Exception {
        List<URL> urls = new ArrayList<URL>(archives.size());
        for (Archive archive : archives) {
            // Add the current archive at end (it will be reversed and end up taking
            // precedence)
            urls.add(archive.getUrl());
        }
        return createClassLoader(urls.toArray(new URL[urls.size()]));
    }

    /**
     * Create a classloader for the specified URLs
     * @param urls the URLs
     * @return the classloader
     * @throws Exception
     */
    protected ClassLoader createClassLoader(URL[] urls) throws Exception {
        return new GBootClassLoader(urls, getClass().getClassLoader());
    }

    /**
     * Launch the application given the archive file and a fully configured classloader.
     * @param args the incoming arguments
     * @param mainClass the main class to run
     * @param classLoader the classloader
     * @throws Exception
     */
    protected void launch(String[] args, String mainClass, ClassLoader classLoader)
            throws Exception {
        Runnable runner = createMainMethodRunner(mainClass, args, classLoader);
        Thread runnerThread = new Thread(runner);
        runnerThread.setContextClassLoader(classLoader);
        runnerThread.setName(Thread.currentThread().getName());
        runnerThread.start();
    }

    /**
     * Create the {@code MainMethodRunner} used to launch the application.
     * @param mainClass the main class
     * @param args the incoming arguments
     * @param classLoader the classloader
     * @return a runnable used to start the application
     * @throws Exception
     */
    protected Runnable createMainMethodRunner(String mainClass, String[] args,
                                              ClassLoader classLoader) throws Exception {
        Class<?> runnerClass = classLoader.loadClass(RUNNER_CLASS);
        Constructor<?> constructor = runnerClass.getConstructor(String.class,
                String[].class);
        return (Runnable) constructor.newInstance(mainClass, args);
    }

    /**
     * Returns the main class that should be launched.
     * @return the name of the main class
     * @throws Exception
     */
    protected abstract String getMainClass() throws Exception;

    /**
     * Returns the archives that will be used to construct the class path.
     * @return the class path archives
     * @throws Exception
     */
    protected abstract List<Archive> getClassPathArchives() throws Exception;

    protected final Archive createArchive() throws Exception {
        ProtectionDomain protectionDomain = getClass().getProtectionDomain();
        CodeSource codeSource = protectionDomain.getCodeSource();
        URI location = (codeSource == null ? null : codeSource.getLocation().toURI());
        String path = (location == null ? null : location.getSchemeSpecificPart());
        if (path == null) {
            throw new IllegalStateException("Unable to determine code source archive");
        }
        File root = new File(path);
        if (!root.exists()) {
            throw new IllegalStateException(
                    "Unable to determine code source archive from " + root);
        }
        return (root.isDirectory() ? new ExplodedArchive(root) : new JarFileArchive(root));
    }

}
