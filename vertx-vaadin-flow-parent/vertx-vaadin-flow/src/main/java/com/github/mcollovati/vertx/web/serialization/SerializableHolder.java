package com.github.mcollovati.vertx.web.serialization;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.shareddata.impl.ClusterSerializable;

public class SerializableHolder implements ClusterSerializable {

    private transient Object object;

    public SerializableHolder() {
    }

    public SerializableHolder(Object object) {
        this.object = object;
    }

    @Override
    public void writeToBuffer(Buffer buffer) {
        SerializationSupport.writeToBuffer(buffer, object);
    }

    @Override
    public int readFromBuffer(int pos, Buffer buffer) {
        return SerializationSupport.readFromBuffer(pos, buffer, this::set);
    }

    private void set(Object object) {
        this.object = object;
    }

    public Object get() {
        return object;
    }
}
