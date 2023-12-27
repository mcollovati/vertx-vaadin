package org.vaadin.jandex;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;

@Tag("hello-world-jandex")
@NpmPackage(value = "@axa-ch/input-text", version = "6.1.0")
@JsModule("./src/hello-world-jandex.ts")
public class HelloWorldJandex extends Component {

    /**
     * Creates the hello world template.
     */
    public HelloWorldJandex() {
    }
}
