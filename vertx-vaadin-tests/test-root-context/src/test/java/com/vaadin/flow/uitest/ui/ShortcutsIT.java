/*
 * The MIT License
 * Copyright © 2000-2020 Marco Collovati (mcollovati@gmail.com)
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
package com.vaadin.flow.uitest.ui;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.flow.testutil.ChromeBrowserTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

public class ShortcutsIT extends ChromeBrowserTest {
    private static final Set<Keys> modifiers =
            Stream.of(Keys.SHIFT, Keys.ALT, Keys.CONTROL, Keys.META).collect(Collectors.toSet());

    private static final String DEFAULT_VALUE = "testing...";

    @Before
    public void before() {
        open();
        resetKeys();
    }

    @Test
    public void clickShortcutWorks() {
        sendKeys(Keys.ALT, "b");
        assertActualEquals("button");
    }

    @Test
    public void focusShortcutWorks() {
        sendKeys(Keys.ALT, "f");

        WebElement input = findElement(By.cssSelector("input#input"));

        // seems no to work with chrome 83
        assertEquals(input, driver.switchTo().activeElement());
        Assert.assertEquals(
                input.getAttribute("id"), driver.switchTo().activeElement().getAttribute("id"));
    }

    @Test
    public void shortcutsOnlyWorkWhenComponentIsVisible() {
        sendKeys(Keys.ALT, "v");
        assertActualEquals("invisibleP");

        // make the paragraph disappear
        sendKeys(Keys.ALT, "i");
        assertActualEquals("toggled!");

        sendKeys(Keys.ALT, "v");
        assertActualEquals("toggled!"); // did not change

        // make the paragraph appear
        sendKeys(Keys.ALT, "i");
        assertActualEquals("toggled!");

        sendKeys(Keys.ALT, "v");
        assertActualEquals("invisibleP");
    }

    @Test
    public void shortcutOnlyWorksWhenComponentIsEnabled() {
        sendKeys(Keys.CONTROL, "U"); // ctrl+shift+u

        // clicking the button disables it, and clicking again should not have
        // and effect
        assertActualEquals("DISABLED CLICKED");

        resetActual();
        assertActualEquals(DEFAULT_VALUE);

        sendKeys(Keys.CONTROL, "U"); // ctrl+shift+u
        assertActualEquals(DEFAULT_VALUE);
    }

    @Test
    public void listenOnScopesTheShortcut() {
        sendKeys(Keys.ALT, "s");
        assertActualEquals(DEFAULT_VALUE); // nothing happened

        WebElement innerInput = findElement(By.id("focusTarget"));
        innerInput.sendKeys(Keys.ALT, "s");
        assertActualEquals("subview");

        // using the shortcut prevented "s" from being written
        Assert.assertEquals("", innerInput.getText());
    }

    @Test
    public void shortcutsOnlyWorkWhenComponentIsAttached() {
        sendKeys(Keys.ALT, "a");
        assertActualEquals(DEFAULT_VALUE); // nothing happens

        // attaches the component
        sendKeys(Keys.ALT, "y");
        assertActualEquals("toggled!");

        sendKeys(Keys.ALT, "a");
        assertActualEquals("attachable");

        // detaches the component
        sendKeys(Keys.ALT, "y");
        assertActualEquals("toggled!");

        sendKeys(Keys.ALT, "a");
        assertActualEquals("toggled!"); // nothing happens
    }

    @Test
    public void modifyingShortcutShouldChangeShortcutEvent() {
        // the shortcut in this test flips its own modifiers
        sendKeys(Keys.ALT, "g");
        assertActualEquals("Alt");

        sendKeys("G");
        assertActualEquals("Shift");

        // check that things revert back.
        sendKeys(Keys.ALT, "g");
        assertActualEquals("Alt");
    }

    @Test
    public void clickShortcutAllowsKeyDefaults() {
        WebElement textField1 = findElement(By.id("click-input-1"));
        WebElement textField2 = findElement(By.id("click-input-2"));

        // ClickButton1: allows browser's default behavior
        textField1.sendKeys("value 1");
        // using sendKeys(...) to send the ENTER instead of textField1
        // .sendKeys(...) since that causes the test to become flaky for some
        // reason
        sendKeys(Keys.ENTER);

        assertActualEquals("click: value 1");

        // ClickButton2: prevents browser's default behavior
        textField2.sendKeys("value 2");
        sendKeys(Keys.ENTER);

        assertActualEquals("click: ");
    }

    @Test
    public void removingShortcutCleansJavascriptEventSettingsItUsed() {
        WebElement removalInput = findElement(By.id("removal-input"));

        Assert.assertEquals("removalInput should be empty", "", removalInput.getAttribute("value"));

        // the removalInput has a shortcut bound on 'd'. When 'd' is typed,
        // instead of printing the letter, the contents are capitalized instead.
        // The shortcut is removed at the same time, so another 'd' should be
        // printed out.

        removalInput.sendKeys("abcd");
        Assert.assertEquals("removalInput should have 'ABC' and no 'd'", "ABC", removalInput.getAttribute("value"));

        removalInput.sendKeys("abcd");
        Assert.assertEquals(
                "removalInput 'ABCabcd'. Since shortcut was removed, 'd' can " + "be typed.",
                "ABCabcd",
                removalInput.getAttribute("value"));
    }

    @Test
    public void bindingShortcutToSameKeyWithDifferentModifiers_shouldNot_triggerTwice() {
        // they bindings are "o", "shift+o", and "alt+o"

        assertActualEquals(DEFAULT_VALUE);

        // bug #5454:
        // if the shortcut is without modifiers, bindings on that
        // key with modifiers also trigger

        // each shortcut has its own counter. each shortcut increments its
        // respective counter and then all the counters are concatenated into
        // "actual" text field. Should shortcuts cross-trigger, number two
        // will be part of the string
        // string order: [o][shift+o][alt+o]
        sendKeys("o");
        assertActualEquals("100");

        sendKeys("O"); // shift+o
        assertActualEquals("110");

        sendKeys(Keys.ALT, "o");
        assertActualEquals("111");
    }

    private void assertActualEquals(String expected) {
        Assert.assertEquals(expected, findElement(By.id("actual")).getAttribute("value"));
    }

    private void resetActual() {
        WebElement actual = findElement(By.id("actual"));
        WebElement blur = findElement(By.id("blur"));
        actual.clear();
        actual.sendKeys("testing...");
        blur.click();
    }

    private void sendKeys(CharSequence... keys) {
        Actions actions = new Actions(driver);
        for (CharSequence keySeq : keys) {
            if (modifiers.contains(keySeq)) {
                actions.keyDown(keySeq);
            } else {
                actions.sendKeys(keySeq);
            }
        }
        actions.build().perform();
        // Implementation that worked for driver < 75.beta:
        // new Actions(driver).sendKeys(keys).build().perform();
        // if keys are not reset, alt will remain down and start flip-flopping
        resetKeys();
    }

    private void resetKeys() {
        Actions actions = new Actions(driver);
        modifiers.forEach(actions::keyUp);
        actions.build().perform();
        // Implementation that worked for driver < 75.beta:
        // new Actions(driver).sendKeys(Keys.NULL).build().perform();
    }
}
