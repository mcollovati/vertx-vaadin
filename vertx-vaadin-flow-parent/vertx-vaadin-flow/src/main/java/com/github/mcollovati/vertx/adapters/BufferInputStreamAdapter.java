package com.github.mcollovati.vertx.adapters;

import java.io.IOException;
import java.io.InputStream;

import io.vertx.core.buffer.Buffer;

public class BufferInputStreamAdapter extends InputStream {

    private final Buffer buffer;
    private int position = 0;

    public BufferInputStreamAdapter(Buffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public int read() throws IOException {
        return (position < buffer.length()) ? buffer.getByte(position++) & 0xff : -1;
    }

}
