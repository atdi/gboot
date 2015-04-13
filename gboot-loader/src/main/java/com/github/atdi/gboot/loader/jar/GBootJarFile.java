package com.github.atdi.gboot.loader.jar;

import com.github.atdi.gboot.loader.data.RandomAccessData;
import com.github.atdi.gboot.loader.data.RandomAccessDataFile;
import com.github.atdi.gboot.loader.util.AsciiBytes;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

/**
 * Extended variant of {@link java.util.jar.JarFile} that behaves in the same way but
 * offers the following additional functionality.
 * <ul>
 * <li>New filtered files can be {@link #getFilteredJarFile(GBootJarEntryFilter...) created}
 * from existing files.</li>
 * <li>A nested {@link com.github.atdi.gboot.loader.jar.GBootJarFile} can be {@link #getNestedJarFile(java.util.zip.ZipEntry) obtained} based
 * on any directory entry.</li>
 * <li>A nested {@link com.github.atdi.gboot.loader.jar.GBootJarFile} can be {@link #getNestedJarFile(java.util.zip.ZipEntry) obtained} for
 * embedded JAR files (as long as their entry is not compressed).</li>
 * <li>Entry data can be accessed as {@link RandomAccessData}.</li>
 * </ul>
 *
 */
public class GBootJarFile extends java.util.jar.JarFile implements Iterable<GBootJarEntryData> {

    private static final AsciiBytes META_INF = new AsciiBytes("META-INF/");

    private static final AsciiBytes MANIFEST_MF = new AsciiBytes("META-INF/MANIFEST.MF");

    private static final AsciiBytes SIGNATURE_FILE_EXTENSION = new AsciiBytes(".SF");

    private static final String PROTOCOL_HANDLER = "java.protocol.handler.pkgs";

    private static final String HANDLERS_PACKAGE = "org.springframework.boot.loader";

    private static final AsciiBytes SLASH = new AsciiBytes("/");

    private final RandomAccessDataFile rootFile;

    private final String pathFromRoot;

    private final RandomAccessData data;

    private final List<GBootJarEntryData> entries;

    private SoftReference<Map<AsciiBytes, GBootJarEntryData>> entriesByName;

    private boolean signed;

    private GBootJarEntryData manifestEntry;

    private SoftReference<Manifest> manifest;

    private URL url;

    private static final Logger logger = Logger.getLogger(GBootJarFile.class.getName());

    /**
     * Create a new {@link com.github.atdi.gboot.loader.jar.GBootJarFile} backed by the specified file.
     * @param file the root jar file
     * @throws java.io.IOException
     */
    public GBootJarFile(File file) throws IOException {
        this(new RandomAccessDataFile(file));
    }

    /**
     * Create a new {@link com.github.atdi.gboot.loader.jar.GBootJarFile} backed by the specified file.
     * @param file the root jar file
     * @throws java.io.IOException
     */
    GBootJarFile(RandomAccessDataFile file) throws IOException {
        this(file, "", file);
    }

    /**
     * Private constructor used to create a new {@link com.github.atdi.gboot.loader.jar.GBootJarFile} either directly or from a
     * nested entry.
     * @param rootFile the root jar file
     * @param pathFromRoot the name of this file
     * @param data the underlying data
     * @throws java.io.IOException
     */
    private GBootJarFile(RandomAccessDataFile rootFile, String pathFromRoot,
                    RandomAccessData data) throws IOException {
        super(rootFile.getFile());
        CentralDirectoryEndRecord endRecord = new CentralDirectoryEndRecord(data);
        this.rootFile = rootFile;
        this.pathFromRoot = pathFromRoot;
        this.data = getArchiveData(endRecord, data);
        this.entries = loadJarEntries(endRecord);
    }

    private GBootJarFile(RandomAccessDataFile rootFile, String pathFromRoot,
                    RandomAccessData data, List<GBootJarEntryData> entries, GBootJarEntryFilter... filters)
            throws IOException {
        super(rootFile.getFile());
        this.rootFile = rootFile;
        this.pathFromRoot = pathFromRoot;
        this.data = data;
        this.entries = filterEntries(entries, filters);
    }

    private RandomAccessData getArchiveData(CentralDirectoryEndRecord endRecord,
                                            RandomAccessData data) {
        long offset = endRecord.getStartOfArchive(data);
        if (offset == 0) {
            return data;
        }
        return data.getSubsection(offset, data.getSize() - offset);
    }

    private List<GBootJarEntryData> loadJarEntries(CentralDirectoryEndRecord endRecord)
            throws IOException {
        RandomAccessData centralDirectory = endRecord.getCentralDirectory(this.data);
        int numberOfRecords = endRecord.getNumberOfRecords();
        List<GBootJarEntryData> entries = new ArrayList<GBootJarEntryData>(numberOfRecords);
        InputStream inputStream = centralDirectory.getInputStream(RandomAccessData.ResourceAccess.ONCE);
        try {
            GBootJarEntryData entry = GBootJarEntryData.fromInputStream(this, inputStream);
            while (entry != null) {
                entries.add(entry);
                processEntry(entry);
                entry = GBootJarEntryData.fromInputStream(this, inputStream);
            }
        }
        finally {
            inputStream.close();
        }
        return entries;
    }

    private List<GBootJarEntryData> filterEntries(List<GBootJarEntryData> entries,
                                             GBootJarEntryFilter[] filters) {
        List<GBootJarEntryData> filteredEntries = new ArrayList<GBootJarEntryData>(entries.size());
        for (GBootJarEntryData entry : entries) {
            AsciiBytes name = entry.getName();
            for (GBootJarEntryFilter filter : filters) {
                name = (filter == null || name == null ? name : filter.apply(name, entry));
            }
            if (name != null) {
                GBootJarEntryData filteredCopy = entry.createFilteredCopy(this, name);
                filteredEntries.add(filteredCopy);
                processEntry(filteredCopy);
            }
        }
        return filteredEntries;
    }

    private void processEntry(GBootJarEntryData entry) {
        AsciiBytes name = entry.getName();
        if (name.startsWith(META_INF)) {
            processMetaInfEntry(name, entry);
        }
    }

    private void processMetaInfEntry(AsciiBytes name, GBootJarEntryData entry) {
        if (name.equals(MANIFEST_MF)) {
            this.manifestEntry = entry;
        }
        if (name.endsWith(SIGNATURE_FILE_EXTENSION)) {
            this.signed = true;
        }
    }

    protected final RandomAccessDataFile getRootJarFile() {
        return this.rootFile;
    }

    RandomAccessData getData() {
        return this.data;
    }

    @Override
    public Manifest getManifest() throws IOException {
        if (this.manifestEntry == null) {
            return null;
        }
        Manifest manifest = (this.manifest == null ? null : this.manifest.get());
        if (manifest == null) {
            InputStream inputStream = this.manifestEntry.getInputStream();
            try {
                manifest = new Manifest(inputStream);
            }
            finally {
                inputStream.close();
            }
            this.manifest = new SoftReference<Manifest>(manifest);
        }
        return manifest;
    }

    @Override
    public Enumeration<JarEntry> entries() {
        final Iterator<GBootJarEntryData> iterator = iterator();
        return new Enumeration<JarEntry>() {

            @Override
            public boolean hasMoreElements() {
                return iterator.hasNext();
            }

            @Override
            public JarEntry nextElement() {
                return iterator.next().asJarEntry();
            }
        };
    }

    @Override
    public Iterator<GBootJarEntryData> iterator() {
        return this.entries.iterator();
    }

    @Override
    public GBootJarEntry getJarEntry(String name) {
        return (GBootJarEntry) getEntry(name);
    }

    @Override
    public ZipEntry getEntry(String name) {
        GBootJarEntryData jarEntryData = getJarEntryData(name);
        return (jarEntryData == null ? null : jarEntryData.asJarEntry());
    }

    public GBootJarEntryData getJarEntryData(String name) {
        if (name == null) {
            return null;
        }
        return getJarEntryData(new AsciiBytes(name));
    }

    public GBootJarEntryData getJarEntryData(AsciiBytes name) {
        if (name == null) {
            return null;
        }
        Map<AsciiBytes, GBootJarEntryData> entriesByName = (this.entriesByName == null ? null
                : this.entriesByName.get());
        if (entriesByName == null) {
            entriesByName = new HashMap<AsciiBytes, GBootJarEntryData>();
            for (GBootJarEntryData entry : this.entries) {
                entriesByName.put(entry.getName(), entry);
            }
            this.entriesByName = new SoftReference<Map<AsciiBytes, GBootJarEntryData>>(
                    entriesByName);
        }

        GBootJarEntryData entryData = entriesByName.get(name);
        if (entryData == null && !name.endsWith(SLASH)) {
            entryData = entriesByName.get(name.append(SLASH));
        }
        return entryData;
    }

    boolean isSigned() {
        return this.signed;
    }

    void setupEntryCertificates() {
        // Fallback to JarInputStream to obtain certificates, not fast but hopefully not
        // happening that often.
        try {
            JarInputStream inputStream = new JarInputStream(getData().getInputStream(
                    RandomAccessData.ResourceAccess.ONCE));
            try {
                JarEntry entry = inputStream.getNextJarEntry();
                while (entry != null) {
                    inputStream.closeEntry();
                    GBootJarEntry jarEntry = getJarEntry(entry.getName());
                    if (jarEntry != null) {
                        jarEntry.setupCertificates(entry);
                    }
                    entry = inputStream.getNextJarEntry();
                }
            }
            finally {
                inputStream.close();
            }
        }
        catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public synchronized InputStream getInputStream(ZipEntry ze) throws IOException {
        return getContainedEntry(ze).getSource().getInputStream();
    }

    /**
     * Return a nested {@link com.github.atdi.gboot.loader.jar.GBootJarFile} loaded from the specified entry.
     * @param ze the zip entry
     * @return a {@link com.github.atdi.gboot.loader.jar.GBootJarFile} for the entry
     * @throws java.io.IOException
     */
    public synchronized GBootJarFile getNestedJarFile(final ZipEntry ze) throws IOException {
        return getNestedJarFile(getContainedEntry(ze).getSource());
    }

    /**
     * Return a nested {@link com.github.atdi.gboot.loader.jar.GBootJarFile} loaded from the specified entry.
     * @param sourceEntry the zip entry
     * @return a {@link com.github.atdi.gboot.loader.jar.GBootJarFile} for the entry
     * @throws java.io.IOException
     */
    public synchronized GBootJarFile getNestedJarFile(GBootJarEntryData sourceEntry)
            throws IOException {
        try {
            if (sourceEntry.nestedJar == null) {
                sourceEntry.nestedJar = createJarFileFromEntry(sourceEntry);
            }
            return sourceEntry.nestedJar;
        }
        catch (IOException ex) {
            throw new IOException("Unable to open nested jar file '"
                    + sourceEntry.getName() + "'", ex);
        }
    }

    private GBootJarFile createJarFileFromEntry(GBootJarEntryData sourceEntry) throws IOException {
        if (sourceEntry.isDirectory()) {
            return createJarFileFromDirectoryEntry(sourceEntry);
        }
        return createJarFileFromFileEntry(sourceEntry);
    }

    private GBootJarFile createJarFileFromDirectoryEntry(GBootJarEntryData sourceEntry)
            throws IOException {
        final AsciiBytes sourceName = sourceEntry.getName();
        GBootJarEntryFilter filter = new GBootJarEntryFilter() {
            @Override
            public AsciiBytes apply(AsciiBytes name, GBootJarEntryData entryData) {
                if (name.startsWith(sourceName) && !name.equals(sourceName)) {
                    return name.substring(sourceName.length());
                }
                return null;
            }
        };
        return new GBootJarFile(this.rootFile, this.pathFromRoot + "!/"
                + sourceEntry.getName().substring(0, sourceName.length() - 1), this.data,
                this.entries, filter);
    }

    private GBootJarFile createJarFileFromFileEntry(GBootJarEntryData sourceEntry)
            throws IOException {
        if (sourceEntry.getMethod() != ZipEntry.STORED) {
            throw new IllegalStateException("Unable to open nested entry '"
                    + sourceEntry.getName() + "'. It has been compressed and nested "
                    + "jar files must be stored without compression. Please check the "
                    + "mechanism used to create your executable jar file");
        }
        return new GBootJarFile(this.rootFile, this.pathFromRoot + "!/"
                + sourceEntry.getName(), sourceEntry.getData());
    }

    /**
     * Return a new jar based on the filtered contents of this file.
     * @param filters the set of jar entry filters to be applied
     * @return a filtered {@link com.github.atdi.gboot.loader.jar.GBootJarFile}
     * @throws java.io.IOException
     */
    public synchronized GBootJarFile getFilteredJarFile(GBootJarEntryFilter... filters)
            throws IOException {
        return new GBootJarFile(this.rootFile, this.pathFromRoot, this.data, this.entries,
                filters);
    }

    private GBootJarEntry getContainedEntry(ZipEntry zipEntry) throws IOException {
        if (zipEntry instanceof JarEntry
                && ((GBootJarEntry) zipEntry).getSource().getSource() == this) {
            return (GBootJarEntry) zipEntry;
        }
        throw new IllegalArgumentException("ZipEntry must be contained in this file");
    }

    @Override
    public int size() {
        return (int) this.data.getSize();
    }

    @Override
    public void close() throws IOException {
        this.rootFile.close();
    }

    /**
     * Return a URL that can be used to access this JAR file. NOTE: the specified URL
     * cannot be serialized and or cloned.
     * @return the URL
     * @throws java.net.MalformedURLException
     */
    public URL getUrl() throws MalformedURLException {
        if (this.url == null) {
            Handler handler = new Handler(this);
            String file = this.rootFile.getFile().toURI() + this.pathFromRoot + "!/";
            file = file.replace("file:////", "file://"); // Fix UNC paths
            this.url = new URL("jar", "", -1, file, handler);
        }
        return this.url;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public String getName() {
        String path = this.pathFromRoot;
        return this.rootFile.getFile() + path;
    }

    /**
     * Register a {@literal 'java.protocol.handler.pkgs'} property so that a
     * {@link java.net.URLStreamHandler} will be located to deal with jar URLs.
     */
    public static void registerUrlProtocolHandler() {
        String handlers = System.getProperty(PROTOCOL_HANDLER);
        System.setProperty(PROTOCOL_HANDLER, ("".equals(handlers) ? HANDLERS_PACKAGE
                : handlers + "|" + HANDLERS_PACKAGE));
        resetCachedUrlHandlers();
    }

    /**
     * Reset any cached handers just in case a jar protocol has already been used. We
     * reset the handler by trying to set a null {@link java.net.URLStreamHandlerFactory} which
     * should have no effect other than clearing the handlers cache.
     */
    private static void resetCachedUrlHandlers() {
        try {
            URL.setURLStreamHandlerFactory(null);
        }
        catch (Error ex) {
            logger.log(Level.SEVERE, "Error during cache reset", ex);
        }
    }

}