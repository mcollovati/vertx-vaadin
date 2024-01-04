/*
 * The MIT License
 * Copyright © 2024 Marco Collovati (mcollovati@gmail.com)
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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import elemental.json.Json;
import elemental.json.JsonObject;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/*
 * NOTE: this code has been copy/pasted and adapted from vaadin-quarkus extension, credit goes to Vaadin Ltd.
 */

public class ResourceProviderTest {

    @Test
    public void quarkusResourceProvider_returnsExpectedResources() throws IOException {
        QuarkusResourceProvider resourceProvider = new QuarkusResourceProvider();
        // ======== resourceProvider.getApplicationResource(s)(String)
        URL applicationResource = resourceProvider.getApplicationResource("resource-provider/some-resource.json");

        Assertions.assertNotNull(applicationResource);

        List<URL> resources = resourceProvider.getApplicationResources("resource-provider/some-resource.json");

        Assertions.assertEquals(1, resources.size());

        Assertions.assertNotNull(resources.get(0));

        URL nonExistent = resourceProvider.getApplicationResource("resource-provider/non-existent.txt");

        Assertions.assertNull(nonExistent);

        // =========== resourceProvider.getClientResource

        URL clientResource = resourceProvider.getClientResource("resource-provider/some-resource.json");

        Assertions.assertNotNull(clientResource);

        InputStream stream = resourceProvider.getClientResourceAsStream("resource-provider/some-resource.json");

        String content =
                IOUtils.readLines(stream, StandardCharsets.UTF_8).stream().collect(Collectors.joining("\n"));
        JsonObject object = Json.parse(content);
        Assertions.assertTrue(object.getBoolean("client-resource"));
    }
}
