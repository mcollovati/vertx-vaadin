/*
 * The MIT License
 * Copyright © 2016-2020 Marco Collovati (mcollovati@gmail.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.github.mcollovati.vertx.web.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.function.Consumer;

public final class SerializationSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(SerializationSupport.class);

    private SerializationSupport() {
    }

    public static void writeToBuffer(final Buffer buffer, final Object object) {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (final ObjectOutputStream objectStream = new ObjectOutputStream(baos)) {
            objectStream.writeObject(object);
            objectStream.flush();
        } catch (final Exception ex) {
            LOGGER.error("Error serializing object of type {}", object.getClass(), ex);
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
        } catch (final Exception ex) {
            LOGGER.error("Error deserializing object", ex);
        }
        return end;
    }

}


