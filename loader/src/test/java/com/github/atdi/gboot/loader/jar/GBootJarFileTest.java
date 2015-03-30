/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.atdi.gboot.loader.jar;

import com.github.atdi.gboot.loader.DummyJarCreator;
import com.github.atdi.gboot.loader.data.RandomAccessDataFile;
import com.github.atdi.gboot.loader.util.AsciiBytes;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * @author aurel
 */
public class GBootJarFileTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private File rootJarFile;

    private GBootJarFile jarFile;

    @Before
    public void setUp() throws Exception {
        this.rootJarFile = this.temporaryFolder.newFile("junit.jar");
        DummyJarCreator.createTestJar(this.rootJarFile);
        this.jarFile = new GBootJarFile(this.rootJarFile);
    }

    @After
    public void tearDown() {
        this.rootJarFile.delete();
    }


    @Test
    public void createFromFile() throws Exception {
        GBootJarFile jarFile = new GBootJarFile(this.rootJarFile);
        assertThat(jarFile.getName(), notNullValue(String.class));
        jarFile.close();
    }

    @Test
    public void getManifest() throws Exception {
        assertThat(this.jarFile.getManifest().getMainAttributes().getValue("Built-By"),
                equalTo("j1"));
    }

    @Test
    public void getManifestEntry() throws Exception {
        ZipEntry entry = this.jarFile.getJarEntry("META-INF/MANIFEST.MF");
        Manifest manifest = new Manifest(this.jarFile.getInputStream(entry));
        assertThat(manifest.getMainAttributes().getValue("Built-By"), equalTo("j1"));
    }

    @Test
    public void getEntries() throws Exception {
        Enumeration<java.util.jar.JarEntry> entries = this.jarFile.entries();
        assertThat(entries.nextElement().getName(), equalTo("META-INF/"));
        assertThat(entries.nextElement().getName(), equalTo("META-INF/MANIFEST.MF"));
        assertThat(entries.nextElement().getName(), equalTo("1.dat"));
        assertThat(entries.nextElement().getName(), equalTo("2.dat"));
        assertThat(entries.nextElement().getName(), equalTo("d/"));
        assertThat(entries.nextElement().getName(), equalTo("d/9.dat"));
        assertThat(entries.nextElement().getName(), equalTo("special/"));
        assertThat(entries.nextElement().getName(), equalTo("special/\u00EB.dat"));
        assertThat(entries.nextElement().getName(), equalTo("nested.jar"));
        assertThat(entries.hasMoreElements(), equalTo(false));
    }

    @Test
    public void getSpecialResourceViaClassLoader() throws Exception {
        URLClassLoader urlClassLoader = new URLClassLoader(
                new URL[]{this.jarFile.getUrl()});
        assertThat(urlClassLoader.getResource("special/\u00EB.dat"), notNullValue());
        urlClassLoader.close();
    }

    @Test
    public void getJarEntry() throws Exception {
        java.util.jar.JarEntry entry = this.jarFile.getJarEntry("1.dat");
        assertThat(entry, notNullValue(ZipEntry.class));
        assertThat(entry.getName(), equalTo("1.dat"));
    }

    @Test
    public void getInputStream() throws Exception {
        InputStream inputStream = this.jarFile.getInputStream(this.jarFile
                .getEntry("1.dat"));
        assertThat(inputStream.available(), equalTo(1));
        assertThat(inputStream.read(), equalTo(1));
        assertThat(inputStream.available(), equalTo(0));
        assertThat(inputStream.read(), equalTo(-1));
    }

    @Test
    public void getName() throws Exception {
        assertThat(this.jarFile.getName(), equalTo(this.rootJarFile.getPath()));
    }

    @Test
    public void getSize() throws Exception {
        assertThat(this.jarFile.size(), equalTo((int) this.rootJarFile.length()));
    }

    @Test
    public void close() throws Exception {
        RandomAccessDataFile randomAccessDataFile = spy(new RandomAccessDataFile(
                this.rootJarFile, 1));
        GBootJarFile jarFile = new GBootJarFile(randomAccessDataFile);
        jarFile.close();
        verify(randomAccessDataFile).close();
    }

    @Test
    public void getUrl() throws Exception {
        URL url = this.jarFile.getUrl();
        assertThat(url.toString(), equalTo("jar:" + this.rootJarFile.toURI() + "!/"));
        GBootJarURLConnection jarURLConnection = (GBootJarURLConnection) url.openConnection();
        assertThat(jarURLConnection.getJarFile(), sameInstance(this.jarFile));
        assertThat(jarURLConnection.getJarEntry(), nullValue());
        assertThat(jarURLConnection.getContentLength(), greaterThan(1));
        assertThat(jarURLConnection.getContent(), sameInstance((Object) this.jarFile));
        assertThat(jarURLConnection.getContentType(), equalTo("x-java/jar"));
        assertThat(jarURLConnection.getJarFileURL().toURI(),
                equalTo(this.rootJarFile.toURI()));
    }

    @Test
    public void createEntryUrl() throws Exception {
        URL url = new URL(this.jarFile.getUrl(), "1.dat");
        assertThat(url.toString(), equalTo("jar:" + this.rootJarFile.toURI() + "!/1.dat"));
        GBootJarURLConnection jarURLConnection = (GBootJarURLConnection) url.openConnection();
        assertThat(jarURLConnection.getJarFile(), sameInstance(this.jarFile));
        assertThat(jarURLConnection.getJarEntry(),
                sameInstance(this.jarFile.getJarEntry("1.dat")));
        assertThat(jarURLConnection.getContentLength(), equalTo(1));
        assertThat(jarURLConnection.getContent(), instanceOf(InputStream.class));
        assertThat(jarURLConnection.getContentType(), equalTo("content/unknown"));
    }

    @Test
    public void getMissingEntryUrl() throws Exception {
        URL url = new URL(this.jarFile.getUrl(), "missing.dat");
        assertThat(url.toString(), equalTo("jar:" + this.rootJarFile.toURI()
                + "!/missing.dat"));
        this.thrown.expect(FileNotFoundException.class);
        ((GBootJarURLConnection) url.openConnection()).getJarEntry();
    }

    @Test
    public void getUrlStream() throws Exception {
        URL url = this.jarFile.getUrl();
        url.openConnection();
        this.thrown.expect(IOException.class);
        url.openStream();
    }

    @Test
    public void getEntryUrlStream() throws Exception {
        URL url = new URL(this.jarFile.getUrl(), "1.dat");
        url.openConnection();
        InputStream stream = url.openStream();
        assertThat(stream.read(), equalTo(1));
        assertThat(stream.read(), equalTo(-1));
    }

    @Test
    public void getNestedJarFile() throws Exception {
        GBootJarFile nestedJarFile = this.jarFile.getNestedJarFile(this.jarFile
                .getEntry("nested.jar"));

        Enumeration<java.util.jar.JarEntry> entries = nestedJarFile.entries();
        assertThat(entries.nextElement().getName(), equalTo("META-INF/"));
        assertThat(entries.nextElement().getName(), equalTo("META-INF/MANIFEST.MF"));
        assertThat(entries.nextElement().getName(), equalTo("3.dat"));
        assertThat(entries.nextElement().getName(), equalTo("4.dat"));
        assertThat(entries.nextElement().getName(), equalTo("\u00E4.dat"));
        assertThat(entries.hasMoreElements(), equalTo(false));

        InputStream inputStream = nestedJarFile.getInputStream(nestedJarFile
                .getEntry("3.dat"));
        assertThat(inputStream.read(), equalTo(3));
        assertThat(inputStream.read(), equalTo(-1));

        URL url = nestedJarFile.getUrl();
        assertThat(url.toString(), equalTo("jar:" + this.rootJarFile.toURI()
                + "!/nested.jar!/"));
        GBootJarURLConnection conn = (GBootJarURLConnection) url.openConnection();
        assertThat(conn.getJarFile(), sameInstance(nestedJarFile));
        assertThat(conn.getJarFileURL().toString(),
                equalTo("jar:" + this.rootJarFile.toURI() + "!/nested.jar"));
    }

    @Test
    public void getNestedJarDirectory() throws Exception {
        GBootJarFile nestedJarFile = this.jarFile
                .getNestedJarFile(this.jarFile.getEntry("d/"));

        Enumeration<java.util.jar.JarEntry> entries = nestedJarFile.entries();
        assertThat(entries.nextElement().getName(), equalTo("9.dat"));
        assertThat(entries.hasMoreElements(), equalTo(false));

        InputStream inputStream = nestedJarFile.getInputStream(nestedJarFile
                .getEntry("9.dat"));
        assertThat(inputStream.read(), equalTo(9));
        assertThat(inputStream.read(), equalTo(-1));

        URL url = nestedJarFile.getUrl();
        assertThat(url.toString(), equalTo("jar:" + this.rootJarFile.toURI() + "!/d!/"));
        assertThat(((GBootJarURLConnection) url.openConnection()).getJarFile(),
                sameInstance(nestedJarFile));
    }

    @Test
    public void getNestJarEntryUrl() throws Exception {
        GBootJarFile nestedJarFile = this.jarFile.getNestedJarFile(this.jarFile
                .getEntry("nested.jar"));
        URL url = nestedJarFile.getJarEntry("3.dat").getUrl();
        assertThat(url.toString(), equalTo("jar:" + this.rootJarFile.toURI()
                + "!/nested.jar!/3.dat"));
        InputStream inputStream = url.openStream();
        assertThat(inputStream, notNullValue());
        assertThat(inputStream.read(), equalTo(3));
    }

    @Ignore(value = "Checked with a different java version")
    @Test
    public void createUrlFromString() throws Exception {
        GBootJarFile.registerUrlProtocolHandler();
        String spec = "jar:" + this.rootJarFile.toURI() + "!/nested.jar!/3.dat";
        URL url = new URL(spec);
        assertThat(url.toString(), equalTo(spec));
        InputStream inputStream = url.openStream();
        assertThat(inputStream, notNullValue());
        assertThat(inputStream.read(), equalTo(80));
        JarURLConnection connection = (JarURLConnection) url.openConnection();
        assertThat(connection.getURL().toString(), equalTo(spec));
        assertThat(connection.getJarFileURL().toString(), equalTo("jar:"
                + this.rootJarFile.toURI() + "!/nested.jar"));
        assertThat(connection.getEntryName(), equalTo("3.dat"));
    }

    @Test
    public void createNonNestedUrlFromString() throws Exception {
        GBootJarFile.registerUrlProtocolHandler();
        String spec = "jar:" + this.rootJarFile.toURI() + "!/2.dat";
        URL url = new URL(spec);
        assertThat(url.toString(), equalTo(spec));
        InputStream inputStream = url.openStream();
        assertThat(inputStream, notNullValue());
        assertThat(inputStream.read(), equalTo(2));
        JarURLConnection connection = (JarURLConnection) url.openConnection();
        assertThat(connection.getURL().toString(), equalTo(spec));
        assertThat(connection.getJarFileURL().toURI(), equalTo(this.rootJarFile.toURI()));
        assertThat(connection.getEntryName(), equalTo("2.dat"));
    }

    @Test
    public void getDirectoryInputStream() throws Exception {
        InputStream inputStream = this.jarFile
                .getInputStream(this.jarFile.getEntry("d/"));
        assertThat(inputStream, notNullValue());
        assertThat(inputStream.read(), equalTo(-1));
    }

    @Test
    public void getDirectoryInputStreamWithoutSlash() throws Exception {
        InputStream inputStream = this.jarFile.getInputStream(this.jarFile.getEntry("d"));
        assertThat(inputStream, notNullValue());
        assertThat(inputStream.read(), equalTo(-1));
    }

    @Test
    public void getFilteredJarFile() throws Exception {
        GBootJarFile filteredJarFile = this.jarFile.getFilteredJarFile(new GBootJarEntryFilter() {
            @Override
            public AsciiBytes apply(AsciiBytes entryName, GBootJarEntryData entry) {
                if (entryName.toString().equals("1.dat")) {
                    return new AsciiBytes("x.dat");
                }
                return null;
            }
        });
        Enumeration<java.util.jar.JarEntry> entries = filteredJarFile.entries();
        assertThat(entries.nextElement().getName(), equalTo("x.dat"));
        assertThat(entries.hasMoreElements(), equalTo(false));

        InputStream inputStream = filteredJarFile.getInputStream(filteredJarFile
                .getEntry("x.dat"));
        assertThat(inputStream.read(), equalTo(1));
        assertThat(inputStream.read(), equalTo(-1));
    }

    @Test
    public void sensibleToString() throws Exception {
        assertThat(this.jarFile.toString(), equalTo(this.rootJarFile.getPath()));
        assertThat(this.jarFile.getNestedJarFile(this.jarFile.getEntry("nested.jar"))
                .toString(), equalTo(this.rootJarFile.getPath() + "!/nested.jar"));
    }


    @Test
    public void cannotLoadMissingJar() throws Exception {
        GBootJarFile nestedJarFile = this.jarFile.getNestedJarFile(this.jarFile
                .getEntry("nested.jar"));
        URL nestedUrl = nestedJarFile.getUrl();
        URL url = new URL(nestedUrl, nestedJarFile.getUrl() + "missing.jar!/3.dat");
        this.thrown.expect(FileNotFoundException.class);
        url.openConnection().getInputStream();
    }
}
