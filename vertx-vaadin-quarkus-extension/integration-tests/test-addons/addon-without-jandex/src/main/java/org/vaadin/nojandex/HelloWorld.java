package org.vaadin.nojandex;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;

@Tag("hello-world")
@NpmPackage(value = "@axa-ch/input-text", version = "6.1.0")
@JsModule("./src/hello-world.ts")
public class HelloWorld extends Component {

    /**
     * Creates the hello world template.
     */
    public HelloWorld() {
    }
}
