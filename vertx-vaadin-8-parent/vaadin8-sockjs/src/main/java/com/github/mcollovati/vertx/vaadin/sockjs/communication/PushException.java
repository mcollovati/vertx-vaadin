/*
 * The MIT License
 * Copyright © 2016-2018 Marco Collovati (mcollovati@gmail.com)
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
package com.github.mcollovati.vertx.vaadin.sockjs.communication;

/**
 * This exception is thrown when the server is unable
 * to push data to the client.
 */
public class PushException extends RuntimeException {

    /**
     * Constructs a new instance of {@link PushException} using the
     * arguments supplied.
     *
     * @param message Additional detail about this exception.
     * @see java.lang.Throwable#getMessage
     */
    public PushException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance of {@link PushException} using the
     * arguments supplied.
     *
     * @param message Additional detail about this exception.
     * @param cause   The cause of the exception
     * @see java.lang.Throwable#getMessage
     */
    public PushException(String message, Throwable cause) {
        super(message, cause);
    }
}
