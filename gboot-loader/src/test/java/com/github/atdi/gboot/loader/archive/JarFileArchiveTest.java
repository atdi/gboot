package com.github.atdi.gboot.loader.archive;

import com.github.atdi.gboot.loader.DummyJarCreator;
import com.github.atdi.gboot.loader.util.AsciiBytes;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

public class JarFileArchiveTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private File rootJarFile;

    private JarFileArchive archive;

    private String rootJarFileUrl;

    @Before
    public void setup() throws Exception {
        setup(false);
    }

    private void setup(boolean unpackNested) throws Exception {
        this.rootJarFile = this.temporaryFolder.newFile();
        this.rootJarFileUrl = rootJarFile.toURI().toString();
        System.out.println(rootJarFileUrl);
        DummyJarCreator.createTestJar(this.rootJarFile, unpackNested);
        this.archive = new JarFileArchive(this.rootJarFile);
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
        assertThat(url.toString(), equalTo("jar:" + this.rootJarFileUrl + "!/"));
    }

    @Test
    public void getNestedArchive() throws Exception {
        Archive.Entry entry = getEntriesMap(this.archive).get("nested.jar");
        Archive nested = this.archive.getNestedArchive(entry);
        assertThat(nested.getUrl().toString(), equalTo("jar:" + this.rootJarFileUrl
                + "!/nested.jar!/"));
    }

    @Test
    public void getNestedUnpackedArchive() throws Exception {
        setup(true);
        Archive.Entry entry = getEntriesMap(this.archive).get("nested.jar");
        Archive nested = this.archive.getNestedArchive(entry);
        assertThat(nested.getUrl().toString(), startsWith("file:"));
        assertThat(nested.getUrl().toString(), endsWith(".jar"));
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
    }

    private Map<String, Archive.Entry> getEntriesMap(Archive archive) {
        Map<String, Archive.Entry> entries = new HashMap<String, Archive.Entry>();
        for (Archive.Entry entry : archive.getEntries()) {
            entries.put(entry.getName().toString(), entry);
        }
        return entries;
    }

}