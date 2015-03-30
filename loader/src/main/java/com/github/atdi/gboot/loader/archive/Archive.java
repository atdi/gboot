package com.github.atdi.gboot.loader.archive;

import com.github.atdi.gboot.loader.util.AsciiBytes;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.jar.Manifest;

/**
 * An archive that can be launched by the Launcher.
 *
 * @author Phillip Webb
 * @see JarFileArchive
 */
public abstract class Archive {

    /**
     * Returns a URL that can be used to load the archive.
     * @return the archive URL
     * @throws java.net.MalformedURLException
     */
    public abstract URL getUrl() throws MalformedURLException;

    /**
     * Obtain the main class that should be used to launch the application. By default
     * this method uses a {@code Start-Class} manifest entry.
     * @return the main class
     * @throws Exception
     */
    public String getMainClass() throws Exception {
        Manifest manifest = getManifest();
        String mainClass = null;
        if (manifest != null) {
            mainClass = manifest.getMainAttributes().getValue("Start-Class");
        }
        if (mainClass == null) {
            throw new IllegalStateException(
                    "No 'Start-Class' manifest entry specified in " + this);
        }
        return mainClass;
    }

    @Override
    public String toString() {
        try {
            return getUrl().toString();
        }
        catch (Exception ex) {
            return "archive";
        }
    }

    /**
     * Returns the manifest of the archive.
     * @return the manifest
     * @throws java.io.IOException
     */
    public abstract Manifest getManifest() throws IOException;

    /**
     * Returns all entries from the archive.
     * @return the archive entries
     */
    public abstract Collection<Entry> getEntries();

    /**
     * Returns nested {@link com.github.atdi.gboot.loader.archive.Archive}s for entries that match the specified filter.
     * @param filter the filter used to limit entries
     * @return nested archives
     * @throws java.io.IOException
     */
    public abstract List<Archive> getNestedArchives(EntryFilter filter)
            throws IOException;

    /**
     * Returns a filtered version of the archive.
     * @param filter the filter to apply
     * @return a filter archive
     * @throws java.io.IOException
     */
    public abstract Archive getFilteredArchive(EntryRenameFilter filter)
            throws IOException;

    /**
     * Represents a single entry in the archive.
     */
    public static interface Entry {

        /**
         * Returns {@code true} if the entry represents a directory.
         * @return if the entry is a directory
         */
        boolean isDirectory();

        /**
         * Returns the name of the entry
         * @return the name of the entry
         */
        AsciiBytes getName();

    }

    /**
     * Strategy interface to filter {@link com.github.atdi.gboot.loader.archive.Archive.Entry Entries}.
     */
    public static interface EntryFilter {

        /**
         * Apply the jar entry filter.
         * @param entry the entry to filter
         * @return {@code true} if the filter matches
         */
        boolean matches(Entry entry);

    }

    /**
     * Strategy interface to filter or rename {@link com.github.atdi.gboot.loader.archive.Archive.Entry Entries}.
     */
    public static interface EntryRenameFilter {

        /**
         * Apply the jar entry filter.
         * @param entryName the current entry name. This may be different that the
         * original entry name if a previous filter has been applied
         * @param entry the entry to filter
         * @return the new name of the entry or {@code null} if the entry should not be
         * included.
         */
        AsciiBytes apply(AsciiBytes entryName, Entry entry);

    }

}
