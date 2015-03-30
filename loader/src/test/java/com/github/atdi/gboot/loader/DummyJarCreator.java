package com.github.atdi.gboot.loader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;


public class DummyJarCreator {

    private DummyJarCreator() {

    }

    public static void createTestJar(File file) throws Exception {
        createTestJar(file, false);
    }

    public static void createTestJar(File file, boolean unpackNested) throws Exception {
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        JarOutputStream jarOutputStream = new JarOutputStream(fileOutputStream);
        try {
            writeManifest(jarOutputStream, "j1");
            writeEntry(jarOutputStream, "1.dat", 1);
            writeEntry(jarOutputStream, "2.dat", 2);
            writeDirEntry(jarOutputStream, "d/");
            writeEntry(jarOutputStream, "d/9.dat", 9);
            writeDirEntry(jarOutputStream, "special/");
            writeEntry(jarOutputStream, "special/\u00EB.dat", '\u00EB');

            JarEntry nestedEntry = new JarEntry("nested.jar");
            byte[] nestedJarData = getNestedJarData();
            nestedEntry.setSize(nestedJarData.length);
            nestedEntry.setCompressedSize(nestedJarData.length);
            if (unpackNested) {
                nestedEntry.setComment("UNPACK:0000000000000000000000000000000000000000");
            }
            CRC32 crc32 = new CRC32();
            crc32.update(nestedJarData);
            nestedEntry.setCrc(crc32.getValue());

            nestedEntry.setMethod(ZipEntry.STORED);
            jarOutputStream.putNextEntry(nestedEntry);
            jarOutputStream.write(nestedJarData);
            jarOutputStream.closeEntry();
        }
        finally {
            jarOutputStream.close();
        }
    }

    private static byte[] getNestedJarData() throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        JarOutputStream jarOutputStream = new JarOutputStream(byteArrayOutputStream);
        writeManifest(jarOutputStream, "j2");
        writeEntry(jarOutputStream, "3.dat", 3);
        writeEntry(jarOutputStream, "4.dat", 4);
        writeEntry(jarOutputStream, "\u00E4.dat", '\u00E4');
        jarOutputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

    private static void writeManifest(JarOutputStream jarOutputStream, String name)
            throws Exception {
        writeDirEntry(jarOutputStream, "META-INF/");
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().putValue("Built-By", name);
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        jarOutputStream.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));
        manifest.write(jarOutputStream);
        jarOutputStream.closeEntry();
    }

    private static void writeDirEntry(JarOutputStream jarOutputStream, String name)
            throws IOException {
        jarOutputStream.putNextEntry(new JarEntry(name));
        jarOutputStream.closeEntry();
    }

    private static void writeEntry(JarOutputStream jarOutputStream, String name, int data)
            throws IOException {
        jarOutputStream.putNextEntry(new JarEntry(name));
        jarOutputStream.write(new byte[] { (byte) data });
        jarOutputStream.closeEntry();
    }
}
