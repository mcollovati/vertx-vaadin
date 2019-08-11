package com.github.mcollovati.vertx.web.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.function.Consumer;

import io.vertx.core.buffer.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SerializationSupport {

    private static final Logger logger = LoggerFactory.getLogger(SerializationSupport.class);

    public static void writeToBuffer(Buffer buffer, Object object) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream objectStream = new ObjectOutputStream(baos)) {
            objectStream.writeObject(object);
            objectStream.flush();
        } catch (Exception ex) {
            logger.error("Error serializing object of type {}", object.getClass(), ex);
        }
        buffer.appendInt(baos.size());
        buffer.appendBytes(baos.toByteArray());
    }

    @SuppressWarnings("unchecked")
    public static <T> int readFromBuffer(int pos, Buffer buffer, Consumer<T> objectConsumer) {
        int size = buffer.getInt(pos);
        pos += 4;
        int end = pos + size;
        try (ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(buffer.getBytes(pos, end)))) {
            Object object = is.readObject();
            objectConsumer.accept((T) object);
        } catch (Exception ex) {
            logger.error("Error deserializing object", ex);
        }
        return end;
    }

}


