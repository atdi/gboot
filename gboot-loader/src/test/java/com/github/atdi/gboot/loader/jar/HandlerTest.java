package com.github.atdi.gboot.loader.jar;

import com.github.atdi.gboot.loader.DummyJarCreator;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import sun.net.www.protocol.jar.JarURLConnection;

import java.io.File;
import java.net.URL;
import java.net.URLConnection;

import static org.junit.Assert.*;

public class HandlerTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private Handler handler;

    private File rootJarFile;

    private GBootJarFile jarFile;

    @Before
    public void setUp() throws Exception {
        this.rootJarFile = this.temporaryFolder.newFile("junit.jar");
        DummyJarCreator.createTestJar(this.rootJarFile);
        this.jarFile = new GBootJarFile(this.rootJarFile);
        handler = new Handler(this.jarFile);
    }

    @After
    public void tearDown() throws Exception {
        this.rootJarFile.delete();

    }

    @Test
    public void testOpenConnection() throws Exception {
        URL url = this.jarFile.getUrl();
        URLConnection urlConnection = handler.openConnection(url);
        assertThat(urlConnection, CoreMatchers.instanceOf(GBootJarURLConnection.class));
    }


    @Test
    public void testOpenConnectionNullJarFile() throws Exception {
        Handler nullJar = new Handler();
        URL url = this.jarFile.getUrl();
        URLConnection urlConnection = nullJar.openConnection(url);
        assertThat(urlConnection, CoreMatchers.instanceOf(GBootJarURLConnection.class));
    }

    @Test
    public void testOpenConnectionNullJarFileThrowException() throws Exception {
        Handler nullJar = new Handler();
        URL url = new URL("jar:file:/tmp/junit2345063031420552069/junitnot.jar!/");
        URLConnection urlConnection = nullJar.openConnection(url);
        assertThat(urlConnection, CoreMatchers.instanceOf(JarURLConnection.class));
    }

    @Test
    public void testGetRootJarFileFromUrl() throws Exception {
        URL url = this.jarFile.getUrl();
        GBootJarFile responseJarFile = handler.getRootJarFileFromUrl(url);
        assertThat(responseJarFile.getUrl(), CoreMatchers.equalTo(this.jarFile.getUrl()));
    }

    @Test
    public void testAddToRootFileCache() throws Exception {
        Handler.addToRootFileCache(this.rootJarFile, this.jarFile);
    }

    @Test
    public void testSetUseFastConnectionExceptions() throws Exception {
    }
}