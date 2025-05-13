package org.vaadin.crudui.demo.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.vaadin.crudui.demo.authentication.AccessControl;
import org.vaadin.crudui.demo.service.GreetService;

/**
 * Flow view that is available only for users with role 'ADMIN'
 */
@RolesAllowed(AccessControl.ADMIN_ROLE_NAME)
@Menu(title = "Flow admin", icon = "vaadin:tools")
@Route("flow")
@PageTitle("Flow Admin page")
public class FlowView extends VerticalLayout {

    public FlowView(GreetService service) {

        // Use TextField for standard text input
        TextField textField = new TextField("Your name");

        // Button click listeners can be defined as lambda expressions
        Button button = new Button("Say hello", e -> {
            add(new Paragraph(service.greet(textField.getValue())));
        });
        button.setId("say-hello");

        add(new H3("Flow Admin View"), textField, button);
    }

}
