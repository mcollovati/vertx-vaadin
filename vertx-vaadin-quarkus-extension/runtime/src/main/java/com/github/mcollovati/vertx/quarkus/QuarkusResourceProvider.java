/*
 * The MIT License
 * Copyright © 2000-2021 Marco Collovati (mcollovati@gmail.com)
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
package com.github.mcollovati.vertx.quarkus;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.vaadin.flow.di.ResourceProvider;
import org.apache.commons.io.IOUtils;

/**
 * A {@link ResourceProvider} implementation that delegates resource loading to
 * current thread context ClassLoader.
 *
 * NOTE: this code has been copy/pasted and adapted from vaadin-quarkus extension, credit goes to Vaadin Ltd.
 */
public class QuarkusResourceProvider implements ResourceProvider {

    private Map<String, CachedStreamData> cache = new ConcurrentHashMap<>();

    @Override
    public URL getApplicationResource(String path) {
        return Thread.currentThread().getContextClassLoader().getResource(path);
    }

    @Override
    public List<URL> getApplicationResources(String path) throws IOException {
        return Collections.list(Thread.currentThread().getContextClassLoader().getResources(path));
    }

    @Override
    public URL getClientResource(String path) {
        return getApplicationResource(path);
    }

    @Override
    public InputStream getClientResourceAsStream(String path) throws IOException {
        // the client resource should be available in the classpath, so
        // its content is cached once. If an exception is thrown then
        // something is broken and it's also cached and will be rethrown on
        // every subsequent access
        CachedStreamData cached = cache.computeIfAbsent(path, key -> {
            URL url = getClientResource(key);
            try (InputStream stream = url.openStream()) {
                ByteArrayOutputStream tempBuffer = new ByteArrayOutputStream();
                IOUtils.copy(stream, tempBuffer);
                return new CachedStreamData(tempBuffer.toByteArray(), null);
            } catch (IOException e) {
                return new CachedStreamData(null, e);
            }
        });

        IOException exception = cached.exception;
        if (exception == null) {
            return new ByteArrayInputStream(cached.data);
        }
        throw exception;
    }

    private static class CachedStreamData {

        private final byte[] data;
        private final IOException exception;

        private CachedStreamData(byte[] data, IOException exception) {
            this.data = data;
            this.exception = exception;
        }
    }
}
