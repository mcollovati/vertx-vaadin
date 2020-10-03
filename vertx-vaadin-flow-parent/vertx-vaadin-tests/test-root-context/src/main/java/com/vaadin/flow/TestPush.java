package com.vaadin.flow;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.vaadin.flow.shared.communication.PushMode;
import com.vaadin.flow.shared.ui.Transport;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TestPush {

    /**
     * The {@link PushMode} to use for the annotated root navigation target (or custom UI). The default
     * push mode when this annotation is present is {@link PushMode#AUTOMATIC}.
     *
     * @return the push mode to use
     */
    PushMode value() default PushMode.AUTOMATIC;

    /**
     * Transport type used for the push for the annotated root navigation target (or custom UI). The
     * default transport type when this annotation is present is
     * {@link Transport#WEBSOCKET_XHR}.
     *
     * @return the transport type to use
     */
    Transport transport() default Transport.WEBSOCKET_XHR;


}
