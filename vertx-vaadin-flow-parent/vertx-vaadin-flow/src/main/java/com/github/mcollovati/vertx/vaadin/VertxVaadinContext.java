package com.github.mcollovati.vertx.vaadin;

import java.util.function.Supplier;

import com.vaadin.flow.server.VaadinContext;
import io.vertx.core.Context;
import io.vertx.core.Vertx;

public class VertxVaadinContext implements VaadinContext {

    private transient final Context context;
    private transient final Vertx vertx;

    public VertxVaadinContext(Vertx vertx) {
        this.vertx = vertx;
        this.context = vertx.getOrCreateContext();
    }

    @Override
    public <T> T getAttribute(Class<T> type, Supplier<T> defaultValueSupplier) {
        T result = context.get(type.getName());
        if (result == null && defaultValueSupplier != null) {
            result = defaultValueSupplier.get();
            context.put(type.getName(), result);
        }
        return result;
    }

    @Override
    public <T> void setAttribute(T value) {
        assert value != null;
        context.put(value.getClass().getName(), value);
    }

    @Override
    public void removeAttribute(Class<?> clazz) {
        context.remove(clazz.getName());
    }
}
