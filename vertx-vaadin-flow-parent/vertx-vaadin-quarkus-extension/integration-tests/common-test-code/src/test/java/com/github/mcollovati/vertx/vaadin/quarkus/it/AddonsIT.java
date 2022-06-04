package com.github.mcollovati.vertx.vaadin.quarkus.it;

import com.vaadin.testbench.TestBenchElement;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusIntegrationTest
public class AddonsIT extends AbstractChromeIT {

    @Override
    protected String getTestPath() {
        return "/addons";
    }

    @Test
    void addonWithoutJandexIndexedOnApplicationPropertiesShouldBeRendered() {
        open();
        TestBenchElement addon = waitUntil(driver -> $("hello-world").first());
        Assertions.assertTrue(
                addon.$("span").attribute("id", "without-jandex").exists(),
                "Expecting span element with id 'without-jandex' to be present in 'hello-world' shadow DOM, but was not");
        Assertions.assertTrue(
                addon.$("axa-input-text").attribute("id", "npm-dep").first()
                        .$("input").first().hasClassName("a-input-text__input"),
                "Expecting axa-input-text element with id 'npm-dep' to be rendered in 'hello-world' shadow DOM, but was not");
    }

    @Test
    void addonWithJandexIndexedShouldBeRendered() {
        open();
        TestBenchElement addon = waitUntil(
                driver -> $("hello-world-jandex").first());
        Assertions.assertTrue(
                addon.$("span").attribute("id", "with-jandex").exists(),
                "Expecting span element with id 'with-jandex' to be present in 'hello-world-jandex' shadow DOM, but was not");
        Assertions.assertTrue(
                addon.$("axa-input-text").attribute("id", "npm-dep-jandex")
                        .first().$("input").first()
                        .hasClassName("a-input-text__input"),
                "Expecting axa-input-text element with id 'npm-dep-jandex' to be rendered in 'hello-world-jandex' shadow DOM, but was not");
    }
}
