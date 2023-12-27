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
package com.github.mcollovati.vertx.vaadin.connect.testendpoint;

import dev.hilla.Endpoint;

/**
 * Source code adapted from Vaadin Flow (https://github.com/vaadin/flow)
 *
 * Test case for https://github.com/vaadin/vaadin-connect/issues/162
 */
public class BridgeMethodTestEndpoint {

    public interface TestInterface<T extends TestInterface2> {
        default T testMethodFromInterface(T a) {
            return null;
        }

        int testNormalMethod(int value);
    }

    public interface TestInterface2 {
        String getId();
    }

    public static class TestInterface2Impl implements TestInterface2 {
        public String id;

        @Override
        public String getId() {
            return id;
        }
    }

    public static class MySecondClass<E> {
        public int testMethodFromClass(E value) {
            return 0;
        }
    }

    @Endpoint
    public static class InheritedClass extends MySecondClass<Integer>
            implements TestInterface<TestInterface2Impl> {
        @Override
        public TestInterface2Impl testMethodFromInterface(
                TestInterface2Impl testInterface2) {
            return testInterface2;
        }

        @Override
        public int testMethodFromClass(Integer value) {
            return value;
        }

        @Override
        public int testNormalMethod(int value) {
            return value;
        }
    }
}
