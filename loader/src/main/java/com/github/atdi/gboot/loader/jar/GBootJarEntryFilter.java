package com.github.atdi.gboot.loader.jar;

import com.github.atdi.gboot.loader.util.AsciiBytes;

/**
 * Interface that can be used to filter and optionally rename jar entries.
 */
public interface GBootJarEntryFilter {

    /**
     * Apply the jar entry filter.
     * @param name the current entry name. This may be different that the original entry
     * name if a previous filter has been applied
     * @param entryData the entry data to filter
     * @return the new name of the entry or {@code null} if the entry should not be
     * included.
     */
    AsciiBytes apply(AsciiBytes name, GBootJarEntryData entryData);
}
