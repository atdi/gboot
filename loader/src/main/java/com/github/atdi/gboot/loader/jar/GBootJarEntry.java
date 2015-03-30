package com.github.atdi.gboot.loader.jar;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSigner;
import java.security.cert.Certificate;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Extended variant of {@link java.util.jar.JarEntry} returned by {@link com.github.atdi.gboot.loader.jar.GBootJarFile}s.
 */
public class GBootJarEntry  extends java.util.jar.JarEntry {

    private final GBootJarEntryData source;

    private Certificate[] certificates;

    private CodeSigner[] codeSigners;

    public GBootJarEntry(GBootJarEntryData source) {
        super(source.getName().toString());
        this.source = source;
    }

    /**
     * Return the source {@link GBootJarEntryData} that was used to create this entry.
     * @return the source of the entry
     */
    public GBootJarEntryData getSource() {
        return this.source;
    }

    /**
     * Return a {@link java.net.URL} for this {@link com.github.atdi.gboot.loader.jar.GBootJarEntry}.
     * @return the URL for the entry
     * @throws java.net.MalformedURLException if the URL is not valid
     */
    public URL getUrl() throws MalformedURLException {
        return new URL(this.source.getSource().getUrl(), getName());
    }

    @Override
    public Attributes getAttributes() throws IOException {
        Manifest manifest = this.source.getSource().getManifest();
        return (manifest == null ? null : manifest.getAttributes(getName()));
    }

    @Override
    public Certificate[] getCertificates() {
        if (this.source.getSource().isSigned() && this.certificates == null) {
            this.source.getSource().setupEntryCertificates();
        }
        return this.certificates;
    }

    @Override
    public CodeSigner[] getCodeSigners() {
        if (this.source.getSource().isSigned() && this.codeSigners == null) {
            this.source.getSource().setupEntryCertificates();
        }
        return this.codeSigners;
    }

    void setupCertificates(java.util.jar.JarEntry entry) {
        this.certificates = entry.getCertificates();
        this.codeSigners = entry.getCodeSigners();
    }
}
