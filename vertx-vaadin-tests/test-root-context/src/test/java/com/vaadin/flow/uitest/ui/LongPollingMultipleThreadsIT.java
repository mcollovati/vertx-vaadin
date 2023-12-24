/*
 * Copyright 2000-2020 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package com.vaadin.flow.uitest.ui;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.testutil.ChromeBrowserTest;

public class LongPollingMultipleThreadsIT extends ChromeBrowserTest {

    @Test
    public void openPage_startUpdatingLabelThrowThreads_thereAreNoErrorsInTheConsole() {
        open();
        WebElement button = findElement(By.id("start-button"));
        for (int i = 0; i < 10; i++) {
            button.click();
            /*
             * SockJS client may try to connect to sockjs node server:
             * https://github.com/sockjs/sockjs-node/blob/master/README.md.
             *
             * This entry may be ignored.
             */

            checkLogsForErrors(msg -> msg.contains("sockjs-node")
                    || msg.contains("[WDS] Disconnected!"));
        }

    }
}