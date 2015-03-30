package com.github.atdi.gboot.loader;

import com.github.atdi.gboot.loader.archive.Archive;
import com.github.atdi.gboot.loader.util.AsciiBytes;

import java.util.List;

/**
 * {@link Launcher} for JAR based archives. This launcher assumes that dependency jars are
 * included inside a {@code /lib} directory.
 *
 */
public class JarLauncher extends ExecutableArchiveLauncher {

    private static final AsciiBytes LIB = new AsciiBytes("lib/");

    @Override
    protected boolean isNestedArchive(Archive.Entry entry) {
        return !entry.isDirectory() && entry.getName().startsWith(LIB);
    }

    @Override
    protected void postProcessClassPathArchives(List<Archive> archives) throws Exception {
        archives.add(0, getArchive());
    }

    public static void main(String[] args) {
        new JarLauncher().launch(args);
    }

}
