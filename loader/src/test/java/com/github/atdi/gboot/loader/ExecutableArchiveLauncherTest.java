package com.github.atdi.gboot.loader;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.Callable;

import com.github.atdi.gboot.loader.archive.Archive;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;

public class ExecutableArchiveLauncherTest {

    @Mock
    private JavaAgentDetector javaAgentDetector;

    private ExecutableArchiveLauncher launcher;

    @Before
    public void setupMocks() {
        MockitoAnnotations.initMocks(this);

        this.launcher = new UnitTestExecutableArchiveLauncher(this.javaAgentDetector);
    }

    @Test
    public void createdClassLoaderContainsUrlsFromThreadContextClassLoader()
            throws Exception {
        final URL[] urls = new URL[] { new URL("file:one"), new URL("file:two") };

        doWithTccl(new URLClassLoader(urls), new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                ClassLoader classLoader = ExecutableArchiveLauncherTest.this.launcher
                        .createClassLoader(new URL[0]);
                assertClassLoaderUrls(classLoader, urls);
                return null;
            }
        });
    }

    @Test
    public void javaAgentJarsAreExcludedFromClasspath() throws Exception {
        URL javaAgent = new File("my-agent.jar").getCanonicalFile().toURI().toURL();
        final URL one = new URL("file:one");
        given(this.javaAgentDetector.isJavaAgentJar(javaAgent)).willReturn(true);
        doWithTccl(new URLClassLoader(new URL[] { javaAgent, one }),
                new Callable<Void>() {

                    @Override
                    public Void call() throws Exception {
                        ClassLoader classLoader = ExecutableArchiveLauncherTest.this.launcher
                                .createClassLoader(new URL[0]);
                        assertClassLoaderUrls(classLoader, new URL[] { one });
                        return null;
                    }
                });
    }

    private void assertClassLoaderUrls(ClassLoader classLoader, URL[] urls) {
        assertTrue(classLoader instanceof URLClassLoader);
        assertArrayEquals(urls, ((URLClassLoader) classLoader).getURLs());
    }

    private static final class UnitTestExecutableArchiveLauncher extends
            ExecutableArchiveLauncher {

        public UnitTestExecutableArchiveLauncher(JavaAgentDetector javaAgentDetector) {
            super(javaAgentDetector);
        }

        @Override
        protected boolean isNestedArchive(Archive.Entry entry) {
            return false;
        }
    }

    private void doWithTccl(ClassLoader classLoader, Callable<?> action) throws Exception {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            action.call();
        }
        finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

}