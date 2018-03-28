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
package com.github.mcollovati.vertx.vaadin;

import io.netty.handler.codec.http.cookie.ClientCookieDecoder;

import javax.servlet.http.Cookie;
import java.util.Optional;

/**
 * Created by marco on 24/07/16.
 */
class CookieUtils {


    static Cookie fromVertxCookie(io.vertx.ext.web.Cookie cookie) {
        io.netty.handler.codec.http.cookie.Cookie decoded = ClientCookieDecoder.STRICT.decode(cookie.encode());
        Cookie out = new Cookie(decoded.name(), decoded.value());
        Optional.ofNullable(decoded.domain()).ifPresent(out::setDomain);
        out.setPath(decoded.path());
        out.setHttpOnly(decoded.isHttpOnly());
        out.setSecure(decoded.isSecure());
        if (decoded.maxAge() != Long.MIN_VALUE) {
            out.setMaxAge((int) decoded.maxAge());
        }

        // TODO extract other values
        return out;
    }

    public static io.vertx.ext.web.Cookie toVertxCookie(Cookie cookie) {
        return io.vertx.ext.web.Cookie.cookie(cookie.getName(), cookie.getValue())
            .setMaxAge(cookie.getMaxAge()).setSecure(cookie.getSecure())
            .setHttpOnly(cookie.isHttpOnly()).setPath(cookie.getPath())
            .setDomain(cookie.getDomain());
    }
}
