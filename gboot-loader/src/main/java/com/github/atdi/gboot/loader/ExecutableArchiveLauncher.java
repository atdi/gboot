package com.github.atdi.gboot.loader;

import com.github.atdi.gboot.loader.archive.Archive;

import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base class for executable archive {@link Launcher}s.
 *
 */
public abstract class ExecutableArchiveLauncher extends Launcher {

    private final Archive archive;

    private final JavaAgentDetector javaAgentDetector;

    private static final Logger logger = Logger.getLogger(ExecutableArchiveLauncher.class.getName());

    public ExecutableArchiveLauncher() {
        this(new InputArgumentsJavaAgentDetector());
    }

    public ExecutableArchiveLauncher(JavaAgentDetector javaAgentDetector) {
        try {
            this.archive = createArchive();
        }
        catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
        this.javaAgentDetector = javaAgentDetector;
    }

    ExecutableArchiveLauncher(Archive archive) {
        this.javaAgentDetector = new InputArgumentsJavaAgentDetector();
        this.archive = archive;
    }

    protected final Archive getArchive() {
        return this.archive;
    }

    @Override
    protected String getMainClass() throws Exception {
        return this.archive.getMainClass();
    }

    @Override
    protected List<Archive> getClassPathArchives() throws Exception {
        List<Archive> archives = new ArrayList<Archive>(
                this.archive.getNestedArchives(new Archive.EntryFilter() {
                    @Override
                    public boolean matches(Archive.Entry entry) {
                        return isNestedArchive(entry);
                    }
                }));
        postProcessClassPathArchives(archives);
        return archives;
    }

    @Override
    protected ClassLoader createClassLoader(URL[] urls) throws Exception {
        Set<URL> copy = new LinkedHashSet<URL>(urls.length);
        ClassLoader loader = getDefaultClassLoader();
        if (loader instanceof URLClassLoader) {
            for (URL url : ((URLClassLoader) loader).getURLs()) {
                if (addDefaultClassloaderUrl(urls, url)) {
                    copy.add(url);
                }
            }
        }
        Collections.addAll(copy, urls);
        return super.createClassLoader(copy.toArray(new URL[copy.size()]));
    }

    private boolean addDefaultClassloaderUrl(URL[] urls, URL url) throws URISyntaxException {
        String jarUrl = "jar:" + url + "!/";
        for (URL nestedUrl : urls) {
            if (nestedUrl.toURI().equals(url.toURI()) || nestedUrl.toString().equals(jarUrl)) {
                return false;
            }
        }
        return !this.javaAgentDetector.isJavaAgentJar(url.toURI());
    }

    /**
     * Determine if the specified {@link java.util.jar.JarEntry} is a nested item that should be added
     * to the classpath. The method is called once for each entry.
     * @param entry the jar entry
     * @return {@code true} if the entry is a nested item (jar or folder)
     */
    protected abstract boolean isNestedArchive(Archive.Entry entry);

    /**
     * Called to post-process archive entries before they are used. Implementations can
     * add and remove entries.
     * @param archives the archives
     * @throws Exception
     */
    protected void postProcessClassPathArchives(List<Archive> archives) throws Exception {
    }

    private static ClassLoader getDefaultClassLoader() {
        ClassLoader classloader = null;
        try {
            classloader = Thread.currentThread().getContextClassLoader();
        }
        catch (Throwable ex) {
            logger.log(Level.WARNING, "Cannot access thread context ClassLoader - " +
                    "falling back to system class loader...", ex);
        }
        if (classloader == null) {
            classloader = ExecutableArchiveLauncher.class.getClassLoader();
        }
        return classloader;
    }

}
