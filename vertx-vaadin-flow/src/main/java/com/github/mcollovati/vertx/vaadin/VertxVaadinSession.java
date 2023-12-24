/*
 * The MIT License
 * Copyright © 2016-2020 Marco Collovati (mcollovati@gmail.com)
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
package com.github.mcollovati.vertx.vaadin;

import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.internal.CurrentInstance;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.shared.communication.PushMode;

public class VertxVaadinSession extends VaadinSession {
    /**
     * Creates a new VaadinSession tied to a VaadinService.
     *
     * @param service the Vaadin service for the new session
     */
    public VertxVaadinSession(VertxVaadinService service) {
        super(service);
    }

    @Override
    public void unlock() {
        checkHasLock();
        boolean ultimateRelease = false;
        try {
            /*
             * Run pending tasks and push if the reentrant lock will actually be
             * released by this unlock() invocation.
             */
            if (((ReentrantLock) getLockInstance()).getHoldCount() == 1) {
                ultimateRelease = true;
                getService().runPendingAccessTasks(this);

                for (UI ui : getUIs()) {
                    if (ui.getPushConfiguration()
                        .getPushMode() == PushMode.AUTOMATIC) {
                        Map<Class<?>, CurrentInstance> oldCurrent = CurrentInstance
                            .setCurrent(ui);
                        try {
                            ui.push();
                        } finally {
                            CurrentInstance.restoreInstances(oldCurrent);
                        }
                    }
                }
            }
        } finally {
            getLockInstance().unlock();
        }

        /*
         * If the session is locked when a new access task is added, it is
         * assumed that the queue will be purged when the lock is released. This
         * might however not happen if a task is enqueued between the moment
         * when unlock() purges the queue and the moment when the lock is
         * actually released. This means that the queue should be purged again
         * if it is not empty after unlocking.
         */
        if (ultimateRelease && !getPendingAccessQueue().isEmpty()) {
            getService().ensureAccessQueuePurged(this);
        }
    }

    @Override
    public VertxVaadinService getService() {
        return (VertxVaadinService)super.getService();
    }
}
