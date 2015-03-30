package com.github.atdi.gboot.loader.data;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * {@link com.github.atdi.gboot.loader.data.RandomAccessData} implementation backed by a byte array.
 *
 */
public class ByteArrayRandomAccessData implements RandomAccessData {

    private final byte[] bytes;

    private final long offset;

    private final long length;

    public ByteArrayRandomAccessData(byte[] bytes) {
        this(bytes, 0, (bytes == null ? 0 : bytes.length));
    }

    public ByteArrayRandomAccessData(byte[] bytes, long offset, long length) {
        this.bytes = (bytes == null ? new byte[0] : bytes);
        this.offset = offset;
        this.length = length;
    }

    @Override
    public InputStream getInputStream(ResourceAccess access) {
        return new ByteArrayInputStream(this.bytes, (int) this.offset, (int) this.length);
    }

    @Override
    public RandomAccessData getSubsection(long offset, long length) {
        return new ByteArrayRandomAccessData(this.bytes, this.offset + offset, length);
    }

    @Override
    public long getSize() {
        return this.length;
    }
}
