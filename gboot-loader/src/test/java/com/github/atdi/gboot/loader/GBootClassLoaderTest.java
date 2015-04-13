package com.github.atdi.gboot.loader;

import java.io.File;
import java.net.URL;

import com.github.atdi.gboot.loader.jar.GBootJarFile;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class GBootClassLoaderTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void resolveResourceFromWindowsFilesystem() throws Exception {
        // This path is invalid - it should return null even on Windows.
        // A regular URLClassLoader will deal with it gracefully.
        assertNull(getClass().getClassLoader().getResource(
                "c:\\Users\\user\\bar.properties"));
        GBootClassLoader loader = new GBootClassLoader(new URL[] { new URL(
                "jar:file:src/test/resources/jars/model.jar!/") }, getClass()
                .getClassLoader());
        // So we should too...
        assertNull(loader.getResource("c:\\Users\\user\\bar.properties"));
    }

    @Test
    public void resolveResourceFromArchive() throws Exception {
        GBootClassLoader loader = new GBootClassLoader(new URL[] { new URL(
                "jar:file:src/test/resources/jars/model.jar!/") }, getClass()
                .getClassLoader());
        assertNotNull(loader.getResource("com/play/Model.class"));
        loader.loadClass("com.play.Model");
    }

    @Test
    public void resolveResourcesFromArchive() throws Exception {
        GBootClassLoader loader = new GBootClassLoader(new URL[] { new URL(
                "jar:file:src/test/resources/jars/model.jar!/") }, getClass()
                .getClassLoader());
        assertTrue(loader.getResources("com/play/Model.class").hasMoreElements());
        loader.loadClass("com.play.Model", true);
    }

    @Test
    public void resolveRootPathFromArchive() throws Exception {
        GBootClassLoader loader = new GBootClassLoader(new URL[] { new URL(
                "jar:file:src/test/resources/jars/model.jar!/") }, getClass()
                .getClassLoader());
        assertNotNull(loader.getResource(""));
    }

    @Test
    public void resolveRootResourcesFromArchive() throws Exception {
        GBootClassLoader loader = new GBootClassLoader(new URL[] { new URL(
                "jar:file:src/test/resources/jars/model.jar!/") }, getClass()
                .getClassLoader());
        assertTrue(loader.getResources("").hasMoreElements());
    }

    @Test
    public void resolveFromNested() throws Exception {
        File file = this.temporaryFolder.newFile();
        DummyJarCreator.createTestJar(file);
        GBootJarFile jarFile = new GBootJarFile(file);
        URL url = jarFile.getUrl();
        GBootClassLoader loader = new GBootClassLoader(new URL[] { url },
                null);
        URL resource = loader.getResource("nested.jar!/3.dat");
        assertThat(resource.toString(), equalTo(url + "nested.jar!/3.dat"));
        assertThat(resource.openConnection().getInputStream().read(), equalTo(3));
    }

}