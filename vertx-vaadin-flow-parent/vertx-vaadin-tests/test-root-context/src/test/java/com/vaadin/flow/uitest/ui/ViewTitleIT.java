package com.vaadin.flow.uitest.ui;

import com.vaadin.flow.component.html.testbench.SelectElement;
import com.vaadin.flow.testutil.ChromeBrowserTest;
import org.junit.Assert;
import org.junit.Test;

public class ViewTitleIT extends ChromeBrowserTest {

    @Override
    protected void open() {
        getDriver().get(getRootURL() + "/view/");
    }

    @Test
    public void testNoViewTitle() {
        open();
        openView("BasicElementView");

        verifyTitle("");
    }

    @Test
    public void testViewTitleAnnotation() {
        open();
        openView("TitleView");

        verifyTitle("Title view");
    }

    @Test
    public void testViewDynamicTitle() {
        open();
        openView("DynamicTitleView");

        verifyTitle("dynamic title view");
    }

    private void openView(String viewName) {
        SelectElement input = $(SelectElement.class).first();
        input.selectByText(viewName);
    }

    private void verifyTitle(String title) {
        Assert.assertEquals("Page title does not match", title,
            getDriver().getTitle());
    }

}
