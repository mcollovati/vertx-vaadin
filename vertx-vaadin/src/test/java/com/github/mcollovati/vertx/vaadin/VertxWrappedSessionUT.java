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

import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import com.github.mcollovati.vertx.web.ExtendedSession;
import org.assertj.core.api.ThrowableAssert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by marco on 25/07/16.
 */
@SuppressWarnings("unchecked")
public class VertxWrappedSessionUT {

    @Rule
    public MockitoRule mokitoRule = MockitoJUnit.rule();

    @Mock
    ExtendedSession session;

    @Mock
    HttpSessionBindingListener sessionBindingListenerObject;

    VertxWrappedSession vertxWrappedSession;

    @Before
    public void setUp() throws Exception {
        vertxWrappedSession = new VertxWrappedSession(session);
    }

    @Test
    public void shouldDelegateGetMaxInactiveInterval() throws Exception {
        long timeout = 30000L;
        when(session.timeout()).thenReturn(timeout);
        assertThat(vertxWrappedSession.getMaxInactiveInterval())
            .isEqualTo(Long.valueOf(timeout).intValue() / 1000);
    }

    @Test
    public void setMaxInactiveIntervalIsNotSupported() throws Exception {
        assertThatExceptionOfType(UnsupportedOperationException.class)
            .isThrownBy(() -> vertxWrappedSession.setMaxInactiveInterval(10));
    }

    @Test
    public void shouldDelegateGetAttribute() throws Exception {
        String attrName = "attributeName";
        Object value = new Object();
        when(session.get(attrName)).thenReturn(value, null);
        when(session.isDestroyed()).thenReturn(false);
        assertThat(vertxWrappedSession.getAttribute(attrName)).isSameAs(value);
        assertThat(vertxWrappedSession.getAttribute(attrName)).isNull();
    }

    @Test
    public void getAttributeShouldThrowExceptionWhenSessionIsInvalidated() {
        shouldThrowExceptionWhenSessionIsInvalidated(() -> vertxWrappedSession.getAttribute("attr"));
    }

    @Test
    public void shouldDelegateSetAttribute() throws Exception {
        String attrName = "attributeName";
        Object value1 = new Object();
        vertxWrappedSession.setAttribute(attrName, value1);
        verify(session).put(attrName, value1);
        vertxWrappedSession.setAttribute(attrName, null);
        verify(session).remove(attrName);
    }

    @Test
    public void setAttributeShuoldInvokeValueBoundForHttpSessionBindingListener() throws Exception {
        String attrName = "attributeName";
        vertxWrappedSession.setAttribute(attrName, sessionBindingListenerObject);
        ArgumentCaptor<HttpSessionBindingEvent> sessionBindingEventCaptor = ArgumentCaptor.forClass(HttpSessionBindingEvent.class);
        verify(sessionBindingListenerObject).valueBound(sessionBindingEventCaptor.capture());
        assertHttpSessionBindingEvent(attrName, sessionBindingEventCaptor.getValue());
    }

    @Test
    public void setAttributeShuoldInvokeValueUnboundForReplacedHttpSessionBindingListener() throws Exception {
        String attrName = "attributeName";
        when(session.get(attrName)).thenReturn(sessionBindingListenerObject);
        vertxWrappedSession.setAttribute(attrName, new Object());
        ArgumentCaptor<HttpSessionBindingEvent> sessionBindingEventCaptor = ArgumentCaptor.forClass(HttpSessionBindingEvent.class);
        verify(sessionBindingListenerObject).valueUnbound(sessionBindingEventCaptor.capture());
        assertHttpSessionBindingEvent(attrName, sessionBindingEventCaptor.getValue());
    }

    private void assertHttpSessionBindingEvent(String attrName, HttpSessionBindingEvent sessionBindingEvent) {
        assertHttpSessionBindingEvent(attrName, sessionBindingListenerObject, sessionBindingEvent);
    }
    private void assertHttpSessionBindingEvent(String attrName, Object value, HttpSessionBindingEvent sessionBindingEvent) {
        assertThat(sessionBindingEvent.getSession())
            .isInstanceOf(VertxHttpSession.class)
            .extracting("delegate").contains(vertxWrappedSession);
        assertThat(sessionBindingEvent.getName()).isEqualTo(attrName);
        assertThat(sessionBindingEvent.getValue()).isEqualTo(value);
    }

    @Test
    public void setAttributeShouldThrowExceptionWhenSessionIsInvalidated() {
        shouldThrowExceptionWhenSessionIsInvalidated(() -> vertxWrappedSession.setAttribute("attr", new Object()));
    }

    @Test
    public void shouldDelegateGetAttributeNames() throws Exception {
        Map<String, Object> sampleData = new HashMap<>();
        sampleData.put("a", "a");
        sampleData.put("b", "b");
        sampleData.put("c", "c");
        when(session.data()).thenReturn(emptyMap(), sampleData);
        assertThat(vertxWrappedSession.getAttributeNames()).isEmpty();
        assertThat(vertxWrappedSession.getAttributeNames()).containsOnly("a", "b", "c");
    }

    @Test
    public void getAttributeNamesShouldThrowExceptionWhenSessionIsInvalidated() {
        shouldThrowExceptionWhenSessionIsInvalidated(() -> vertxWrappedSession.getAttributeNames());
    }

    @Test
    public void shouldDelegateInvalidate() throws Exception {
        vertxWrappedSession.invalidate();
        verify(session).destroy();
    }
    @Test
    public void shouldUnbindOnInvalidate() throws Exception {

        Map<String, Object> sampleData = new HashMap<>();
        HttpSessionBindingListener mockA = mock(HttpSessionBindingListener.class);
        HttpSessionBindingListener mockC = mock(HttpSessionBindingListener.class);
        sampleData.put("a", mockA);
        sampleData.put("b", "b");
        sampleData.put("c", mockC);
        sampleData.put("b", "b");
        when(session.data()).thenReturn(sampleData);
        vertxWrappedSession.invalidate();
        verify(session).destroy();
        ArgumentCaptor<HttpSessionBindingEvent> sessionBindingEventCaptor = ArgumentCaptor.forClass(HttpSessionBindingEvent.class);
        verify(mockA).valueUnbound(sessionBindingEventCaptor.capture());
        verify(mockC).valueUnbound(sessionBindingEventCaptor.capture());
        assertThat(sessionBindingEventCaptor.getAllValues()).hasSize(2);
        assertHttpSessionBindingEvent("a", mockA, sessionBindingEventCaptor.getAllValues().get(0));
        assertHttpSessionBindingEvent("c", mockC, sessionBindingEventCaptor.getAllValues().get(1));
    }

    @Test
    public void invalidateShouldThrowExceptionWhenSessionIsInvalidated() {
        shouldThrowExceptionWhenSessionIsInvalidated(() -> vertxWrappedSession.invalidate());
    }

    @Test
    public void shouldDelegateGetId() throws Exception {
        String sessionId = "sessionId";
        when(session.id()).thenReturn(sessionId);
        assertThat(vertxWrappedSession.getId()).isEqualTo(sessionId);
    }

    @Test
    public void shouldDelegateGetCreationTime() throws Exception {
        long now = Instant.now().minusSeconds(60 * 20).toEpochMilli();
        when(session.createdAt()).thenReturn(now);
        assertThat(vertxWrappedSession.getCreationTime()).isEqualTo(now);
    }

    @Test
    public void shouldDelegateGetLastAccessedTime() throws Exception {
        long lastAccessed = Instant.now().minusSeconds(10).toEpochMilli();
        when(session.lastAccessed()).thenReturn(lastAccessed);
        assertThat(vertxWrappedSession.getLastAccessedTime()).isEqualTo(lastAccessed);
    }
    @Test
    public void getLastAccessedTimeShouldThrowExceptionWhenSessionIsInvalidated() {
        shouldThrowExceptionWhenSessionIsInvalidated(() -> vertxWrappedSession.getLastAccessedTime());
    }

    @Test
    public void isNewShouldBeTrueIfLastAccessedIsNotSet() throws Exception {
        when(session.lastAccessed()).thenReturn(0L,System.currentTimeMillis());
        assertThat(vertxWrappedSession.isNew()).isTrue();
        assertThat(vertxWrappedSession.isNew()).isFalse();
    }
    @Test
    public void isNewShouldThrowExceptionWhenSessionIsInvalidated() {
        shouldThrowExceptionWhenSessionIsInvalidated(() -> vertxWrappedSession.isNew());
    }

    @Test
    public void shouldDelegateRemoveAttribute() throws Exception {
        vertxWrappedSession.removeAttribute("attrName");
        verify(session).remove("attrName");
    }
    @Test
    public void removeAttributeShuoldInvokeValueUnboundForHttpSessionBindingListeners() throws Exception {
        String attrName = "attributeName";
        when(session.remove(attrName)).thenReturn(sessionBindingListenerObject);
        vertxWrappedSession.removeAttribute(attrName);
        ArgumentCaptor<HttpSessionBindingEvent> sessionBindingEventCaptor = ArgumentCaptor.forClass(HttpSessionBindingEvent.class);
        verify(sessionBindingListenerObject).valueUnbound(sessionBindingEventCaptor.capture());
        assertHttpSessionBindingEvent(attrName, sessionBindingEventCaptor.getValue());
    }
    @Test
    public void removeAttributeShouldThrowExceptionWhenSessionIsInvalidated() {
        shouldThrowExceptionWhenSessionIsInvalidated(() -> vertxWrappedSession.removeAttribute("attr"));
    }


    private void shouldThrowExceptionWhenSessionIsInvalidated(ThrowableAssert.ThrowingCallable op) {
        when(session.isDestroyed()).thenReturn(true);
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(op);
    }

}