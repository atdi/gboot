package com.github.atdi.gboot.loader.archive;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.github.atdi.gboot.loader.DummyJarCreator;
import com.github.atdi.gboot.loader.util.AsciiBytes;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;


public class ExplodedArchiveTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private File rootFolder;

    private ExplodedArchive archive;

    @Before
    public void setup() throws Exception {
        File file = this.temporaryFolder.newFile();
        DummyJarCreator.createTestJar(file);

        this.rootFolder = this.temporaryFolder.newFolder();
        JarFile jarFile = new JarFile(file);
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            File destination = new File(this.rootFolder.getAbsolutePath()
                    + File.separator + entry.getName());
            destination.getParentFile().mkdirs();
            if (entry.isDirectory()) {
                destination.mkdir();
            }
            else {
                copy(jarFile.getInputStream(entry), new FileOutputStream(destination));
            }
        }
        this.archive = new ExplodedArchive(this.rootFolder);
        jarFile.close();
    }

    private void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int len = in.read(buffer);
        while (len != -1) {
            out.write(buffer, 0, len);
            len = in.read(buffer);
        }
    }

    @Test
    public void getManifest() throws Exception {
        assertThat(this.archive.getManifest().getMainAttributes().getValue("Built-By"),
                equalTo("j1"));
    }

    @Test
    public void getEntries() throws Exception {
        Map<String, Archive.Entry> entries = getEntriesMap(this.archive);
        assertThat(entries.size(), equalTo(9));
    }

    @Test
    public void getUrl() throws Exception {
        URL url = this.archive.getUrl();
        assertThat(new File(URLDecoder.decode(url.getFile(), "UTF-8")),
                equalTo(this.rootFolder));
    }

    @Test
    public void getNestedArchive() throws Exception {
        Archive.Entry entry = getEntriesMap(this.archive).get("nested.jar");
        Archive nested = this.archive.getNestedArchive(entry);
        assertThat(nested.getUrl().toString(), equalTo("jar:" + this.rootFolder.toURI()
                + "nested.jar!/"));
    }

    @Test
    public void nestedDirArchive() throws Exception {
        Archive.Entry entry = getEntriesMap(this.archive).get("d/");
        Archive nested = this.archive.getNestedArchive(entry);
        Map<String, Archive.Entry> nestedEntries = getEntriesMap(nested);
        assertThat(nestedEntries.size(), equalTo(1));
        assertThat(nested.getUrl().toString(), equalTo("file:"
                + this.rootFolder.toURI().getPath() + "d/"));
    }

    @Test
    public void getFilteredArchive() throws Exception {
        Archive filteredArchive = this.archive
                .getFilteredArchive(new Archive.EntryRenameFilter() {
                    @Override
                    public AsciiBytes apply(AsciiBytes entryName, Archive.Entry entry) {
                        if (entryName.toString().equals("1.dat")) {
                            return entryName;
                        }
                        return null;
                    }
                });
        Map<String, Archive.Entry> entries = getEntriesMap(filteredArchive);
        assertThat(entries.size(), equalTo(1));
        URLClassLoader classLoader = new URLClassLoader(
                new URL[] { filteredArchive.getUrl() });
        assertThat(classLoader.getResourceAsStream("1.dat").read(), equalTo(1));
        assertThat(classLoader.getResourceAsStream("2.dat"), nullValue());
        classLoader.close();
    }

    @Test
    public void getNonRecursiveEntriesForRoot() throws Exception {
        ExplodedArchive archive = new ExplodedArchive(new File("/"), false);
        Map<String, Archive.Entry> entries = getEntriesMap(archive);
        assertThat(entries.size(), greaterThan(1));
    }

    @Test
    public void getNonRecursiveManifest() throws Exception {
        ExplodedArchive archive = new ExplodedArchive(new File("src/test/resources/root"));
        assertNotNull(archive.getManifest());
        Map<String, Archive.Entry> entries = getEntriesMap(archive);
        assertThat(entries.size(), equalTo(4));
    }

    @Test
    public void getNonRecursiveManifestEvenIfNonRecursive() throws Exception {
        ExplodedArchive archive = new ExplodedArchive(
                new File("src/test/resources/root"), false);
        assertNotNull(archive.getManifest());
        Map<String, Archive.Entry> entries = getEntriesMap(archive);
        assertThat(entries.size(), equalTo(3));
    }

    @Test
    public void getResourceAsStream() throws Exception {
        ExplodedArchive archive = new ExplodedArchive(new File("src/test/resources/root"));
        assertNotNull(archive.getManifest());
        URLClassLoader loader = new URLClassLoader(new URL[] { archive.getUrl() });
        assertNotNull(loader.getResourceAsStream("META-INF/spring/application.xml"));
        loader.close();
    }

    @Test
    public void getResourceAsStreamNonRecursive() throws Exception {
        ExplodedArchive archive = new ExplodedArchive(
                new File("src/test/resources/root"), false);
        assertNotNull(archive.getManifest());
        URLClassLoader loader = new URLClassLoader(new URL[] { archive.getUrl() });
        assertNotNull(loader.getResourceAsStream("META-INF/spring/application.xml"));
        loader.close();
    }

    private Map<String, Archive.Entry> getEntriesMap(Archive archive) {
        Map<String, Archive.Entry> entries = new HashMap<String, Archive.Entry>();
        for (Archive.Entry entry : archive.getEntries()) {
            entries.put(entry.getName().toString(), entry);
        }
        return entries;
    }

}