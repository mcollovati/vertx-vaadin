package com.github.mcollovati.vertx.vaadin.quarkus.it.regression;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;
import com.github.mcollovati.vertx.quarkus.annotation.UIScoped;

@UIScoped
@Route("regression")
public class RemoveOldContentView extends Div implements RouterLayout {

    public static String NAVIGATE_TO_ANOTHER_ROUTE_INSIDE_MAIN_LAYOUT_BUTTON_ID = "navigate-to-another-route-inside-main-layout-button-id";
    public static String NAVIGATE_TO_ANOTHER_ROUTE_OUTSIDE_MAIN_LAYOUT_BUTTON_ID = "navigate-to-another-layout-button-id";
    public static String NAVIGATE_BACK_FROM_ANOTHER_ROUTE_INSIDE_MAIN_LAYOUT_BUTTON_ID = "navigate-back-from-another-route-inside-main-layout-button-id";
    public static String NAVIGATE_BACK_FROM_ANOTHER_ROUTE_OUTSIDE_MAIN_LAYOUT_BUTTON_ID = "navigate-back-from-another-route-outside-main-layout-button-id";
    public static String SUB_LAYOUT_ID = "sub-layout-id";

    public RemoveOldContentView() {
        add(new Span("This is a topmost parent router layout"));
    }

    @UIScoped
    @ParentLayout(RemoveOldContentView.class)
    public static class SubLayout extends Div implements RouterLayout {
        public SubLayout() {
            setId(SUB_LAYOUT_ID);
            add(new Span("This is a sub router layout"));
        }
    }

    @Route(value = "first-child-route", layout = SubLayout.class)
    public static class FirstView extends Div {

        public FirstView() {
            add(new Span("This is a child route inside main layout"));
            NativeButton navigateToAnotherViewButton = new NativeButton(
                    "Navigate to another route inside Main Layout",
                    click -> UI.getCurrent().navigate(SecondView.class));
            navigateToAnotherViewButton.setId(
                    NAVIGATE_TO_ANOTHER_ROUTE_INSIDE_MAIN_LAYOUT_BUTTON_ID);
            add(navigateToAnotherViewButton);
            NativeButton navigateToAnotherLayoutButton = new NativeButton(
                    "Navigate to another route outside Main Layout",
                    click -> UI.getCurrent()
                            .navigate(AnotherParentLayout.ThirdView.class));
            navigateToAnotherLayoutButton.setId(
                    NAVIGATE_TO_ANOTHER_ROUTE_OUTSIDE_MAIN_LAYOUT_BUTTON_ID);
            add(navigateToAnotherLayoutButton);
        }
    }

    @Route(value = "second-child-route", layout = RemoveOldContentView.class)
    public static class SecondView extends Div {

        public SecondView() {
            add(new Span("This is another route inside Main Layout"));
            NativeButton navigateToChildView = new NativeButton(
                    "Navigate to first route",
                    click -> UI.getCurrent().navigate(FirstView.class));
            navigateToChildView.setId(
                    NAVIGATE_BACK_FROM_ANOTHER_ROUTE_OUTSIDE_MAIN_LAYOUT_BUTTON_ID);
            add(navigateToChildView);
        }
    }

    @Route("secondary")
    public static class AnotherParentLayout extends Div
            implements RouterLayout {

        public AnotherParentLayout() {
            add(new Span("This is an another topmost parent router layout"));
        }

        @Route(value = "third-child-route", layout = AnotherParentLayout.class)
        public static class ThirdView extends Div {

            public ThirdView() {
                add(new Span("This is another route outside of Main Layout"));
                NativeButton navigateToChildView = new NativeButton(
                        "Navigate to first view",
                        click -> UI.getCurrent().navigate(
                                FirstView.class));
                navigateToChildView.setId(
                        NAVIGATE_BACK_FROM_ANOTHER_ROUTE_INSIDE_MAIN_LAYOUT_BUTTON_ID);
                add(navigateToChildView);
            }
        }
    }

}
