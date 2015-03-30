package com.github.atdi.gboot.loader.jar;


import com.github.atdi.gboot.loader.util.AsciiBytes;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.jar.Manifest;

/**
 * {@link java.net.JarURLConnection} used to support {@link JarFile#getUrl()}.
 *
 */
public class GBootJarURLConnection extends java.net.JarURLConnection {

    private static final FileNotFoundException FILE_NOT_FOUND_EXCEPTION = new FileNotFoundException();

    private static final String SEPARATOR = "!/";

    private static final URL EMPTY_JAR_URL;

    static {
        try {
            EMPTY_JAR_URL = new URL("jar:", null, 0, "file:!/", new URLStreamHandler() {
                @Override
                protected URLConnection openConnection(URL u) throws IOException {
                    // Stub URLStreamHandler to prevent the wrong JAR Handler from being
                    // Instantiated and cached.
                    return null;
                }
            });
        }
        catch (MalformedURLException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static final JarEntryName EMPTY_JAR_ENTRY_NAME = new JarEntryName("");

    private static ThreadLocal<Boolean> useFastExceptions = new ThreadLocal<Boolean>();

    private final GBootJarFile jarFile;

    private GBootJarEntryData jarEntryData;

    private URL jarFileUrl;

    private JarEntryName jarEntryName;

    protected GBootJarURLConnection(URL url, GBootJarFile jarFile) throws IOException {
        // What we pass to super is ultimately ignored
        super(EMPTY_JAR_URL);
        this.url = url;
        String spec = url.getFile().substring(jarFile.getUrl().getFile().length());
        int separator;
        while ((separator = spec.indexOf(SEPARATOR)) > 0) {
            jarFile = getNestedJarFile(jarFile, spec.substring(0, separator));
            spec = spec.substring(separator + SEPARATOR.length());
        }
        this.jarFile = jarFile;
        this.jarEntryName = getJarEntryName(spec);
    }

    private GBootJarFile getNestedJarFile(GBootJarFile jarFile, String name) throws IOException {
        GBootJarEntry jarEntry = jarFile.getJarEntry(name);
        if (jarEntry == null) {
            throwFileNotFound(jarEntry, jarFile);
        }
        return jarFile.getNestedJarFile(jarEntry);
    }

    private JarEntryName getJarEntryName(String spec) {
        if (spec.length() == 0) {
            return EMPTY_JAR_ENTRY_NAME;
        }
        return new JarEntryName(spec);
    }

    @Override
    public void connect() throws IOException {
        if (!this.jarEntryName.isEmpty()) {
            this.jarEntryData = this.jarFile.getJarEntryData(this.jarEntryName
                    .asAsciiBytes());
            if (this.jarEntryData == null) {
                throwFileNotFound(this.jarEntryName, this.jarFile);
            }
        }
        this.connected = true;
    }

    private void throwFileNotFound(Object entry, GBootJarFile jarFile)
            throws FileNotFoundException {
        if (Boolean.TRUE.equals(useFastExceptions.get())) {
            throw FILE_NOT_FOUND_EXCEPTION;
        }
        throw new FileNotFoundException("JAR entry " + entry + " not found in "
                + jarFile.getName());
    }

    @Override
    public Manifest getManifest() throws IOException {
        try {
            return super.getManifest();
        }
        finally {
            this.connected = false;
        }
    }

    @Override
    public GBootJarFile getJarFile() throws IOException {
        connect();
        return this.jarFile;
    }

    @Override
    public URL getJarFileURL() {
        if (this.jarFileUrl == null) {
            this.jarFileUrl = buildJarFileUrl();
        }
        return this.jarFileUrl;
    }

    private URL buildJarFileUrl() {
        try {
            String spec = this.jarFile.getUrl().getFile();
            if (spec.endsWith(SEPARATOR)) {
                spec = spec.substring(0, spec.length() - SEPARATOR.length());
            }
            if (spec.indexOf(SEPARATOR) == -1) {
                return new URL(spec);
            }
            return new URL("jar:" + spec);
        }
        catch (MalformedURLException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public GBootJarEntry getJarEntry() throws IOException {
        connect();
        return (this.jarEntryData == null ? null : this.jarEntryData.asJarEntry());
    }

    @Override
    public String getEntryName() {
        return this.jarEntryName.toString();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        connect();
        if (this.jarEntryName.isEmpty()) {
            throw new IOException("no entry name specified");
        }
        return this.jarEntryData.getInputStream();
    }

    @Override
    public int getContentLength() {
        try {
            connect();
            if (this.jarEntryData != null) {
                return this.jarEntryData.getSize();
            }
            return this.jarFile.size();
        }
        catch (IOException ex) {
            return -1;
        }
    }

    @Override
    public Object getContent() throws IOException {
        connect();
        return (this.jarEntryData == null ? this.jarFile : super.getContent());
    }

    @Override
    public String getContentType() {
        return this.jarEntryName.getContentType();
    }

    static void setUseFastExceptions(boolean useFastExceptions) {
        GBootJarURLConnection.useFastExceptions.set(useFastExceptions);
    }

    /**
     * A JarEntryName parsed from a URL String.
     */
    private static class JarEntryName {

        private final AsciiBytes name;

        private String contentType;

        public JarEntryName(String spec) {
            this.name = decode(spec);
        }

        private AsciiBytes decode(String source) {
            int length = (source == null ? 0 : source.length());
            if ((length == 0) || (source.indexOf('%') < 0)) {
                return new AsciiBytes(source);
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream(length);
            for (int i = 0; i < length; i++) {
                int ch = source.charAt(i);
                if (ch == '%') {
                    if ((i + 2) >= length) {
                        throw new IllegalArgumentException("Invalid encoded sequence \""
                                + source.substring(i) + "\"");
                    }
                    ch = decodeEscapeSequence(source, i);
                    i += 2;
                }
                bos.write(ch);
            }
            // AsciiBytes is what is used to store the JarEntries so make it symmetric
            return new AsciiBytes(bos.toByteArray());
        }

        private char decodeEscapeSequence(String source, int i) {
            int hi = Character.digit(source.charAt(i + 1), 16);
            int lo = Character.digit(source.charAt(i + 2), 16);
            if (hi == -1 || lo == -1) {
                throw new IllegalArgumentException("Invalid encoded sequence \""
                        + source.substring(i) + "\"");
            }
            return ((char) ((hi << 4) + lo));
        }

        @Override
        public String toString() {
            return this.name.toString();
        }

        public AsciiBytes asAsciiBytes() {
            return this.name;
        }

        public boolean isEmpty() {
            return this.name.length() == 0;
        }

        public String getContentType() {
            if (this.contentType == null) {
                this.contentType = deduceContentType();
            }
            return this.contentType;
        }

        private String deduceContentType() {
            // Guess the content type, don't bother with streams as mark is not supported
            String type = (isEmpty() ? "x-java/jar" : null);
            type = (type != null ? type : guessContentTypeFromName(toString()));
            type = (type != null ? type : "content/unknown");
            return type;
        }

    }

}
