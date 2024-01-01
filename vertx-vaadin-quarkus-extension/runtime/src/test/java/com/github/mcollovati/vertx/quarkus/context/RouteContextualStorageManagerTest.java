/*
 * The MIT License
 * Copyright © 2000-2021 Marco Collovati (mcollovati@gmail.com)
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
package com.github.mcollovati.vertx.quarkus.context;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.page.ExtendedClientDetails;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.LocationChangeEvent;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import io.quarkus.arc.InjectableBean;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.github.mcollovati.vertx.quarkus.annotation.RouteScopeOwner;
import com.github.mcollovati.vertx.quarkus.context.RouteScopedContext.NavigationData;

@QuarkusTest
@SuppressWarnings({"rawtypes", "unchecked"})
public class RouteContextualStorageManagerTest {

    private static final String STATE = "hello";

    @Route("group1")
    public static class Group1 extends TestHasElement {}

    @RouteScopeOwner(Group1.class)
    public static class MemberOfGroup1 extends TestBean {}

    public static class NoOwnerBean extends TestBean {}

    @Route("group2")
    public static class Group2 extends TestHasElement {}

    @Route("")
    @Tag(Tag.A)
    public static class InitialRoute extends Component {}

    private UIUnderTestContext uiUnderTestContext;

    @Inject
    Event<BeforeEnterEvent> beforeNavigationTrigger;

    @Inject
    Event<AfterNavigationEvent> afterNavigationTrigger;

    private TestRouteScopedContext routeContext = new TestRouteScopedContext();

    final CreationalContext<TestBean> creationalContext = Mockito.mock(CreationalContext.class);
    final InjectableBean<TestBean> contextual = Mockito.mock(InjectableBean.class);

    private Set<TestBean> destroyedBeans = new HashSet<>();

    private BeforeEnterEvent event;
    private AfterNavigationEvent afterEvent;
    private LocationChangeEvent changeEvent;

    private NavigationData data = Mockito.mock(NavigationData.class);

    @BeforeEach
    public void setUp() {
        doSetUp(null, null);

        // create a contextual storage manager as a bean and store it in the
        // session
        BeanProvider.getContextualReference(routeContext.getBeanManager(), TestContextualStorageManager.class, false);
    }

    @AfterEach
    public void tearDown() {
        uiUnderTestContext.tearDownAll();
    }

    @Test
    public void onBeforeEnter_initialNavigationTarget_scopeDoesNotExist_Throws() {
        Mockito.when(event.getNavigationTarget()).thenReturn((Class) InitialRoute.class);
        beforeNavigationTrigger.fire(event);

        Assertions.assertThrows(IllegalStateException.class, () -> {
            getMemberOfGroupProducer(contextual).get();
        });
    }

    @Test
    public void afterNavigation_initialNavigationTarget_scopeDoesNotExist_Throws() {
        Mockito.when(afterEvent.getActiveChain()).thenReturn(Collections.singletonList(new InitialRoute()));
        afterNavigationTrigger.fire(afterEvent);

        Assertions.assertThrows(IllegalStateException.class, () -> {
            getMemberOfGroupProducer(contextual).get();
        });
    }

    @Test
    public void onBeforeEnter_group1Navigation_beansAreScoped() {
        Mockito.when(event.getNavigationTarget()).thenReturn((Class) Group1.class);
        beforeNavigationTrigger.fire(event);

        Supplier<MemberOfGroup1> producer = getMemberOfGroupProducer(contextual);
        MemberOfGroup1 bean = producer.get();
        bean.setState(STATE);
        MemberOfGroup1 anotherBean = (MemberOfGroup1) routeContext.get(contextual);
        Assertions.assertEquals(STATE, anotherBean.getState());

        Supplier<NoOwnerBean> noOwnerProducer = getNoOwnerProducer();
        NoOwnerBean noOwner = noOwnerProducer.get();
        noOwner.setState(STATE);

        NoOwnerBean anotherNoOwner = noOwnerProducer.get();

        Assertions.assertEquals(STATE, anotherNoOwner.getState());
    }

    @Test
    public void onBeforeEnter_group2NavigationAfterGroup1_beansAreDestroyed() {
        Mockito.when(event.getNavigationTarget()).thenReturn((Class) Group1.class);
        beforeNavigationTrigger.fire(event);

        MemberOfGroup1 bean1 = getMemberOfGroupProducer(contextual).get();
        bean1.setState(STATE);

        Supplier<NoOwnerBean> noOwnerProducer = getNoOwnerProducer();
        NoOwnerBean bean2 = noOwnerProducer.get();
        bean2.setState(STATE);

        Mockito.when(event.getNavigationTarget()).thenReturn((Class) Group2.class);
        beforeNavigationTrigger.fire(event);

        Assertions.assertTrue(destroyedBeans.contains(bean1));
        Assertions.assertTrue(destroyedBeans.contains(bean2));

        NoOwnerBean anotherNoOwner = noOwnerProducer.get();

        // no owner bean is not preserved: the new one is created
        Assertions.assertNotEquals(STATE, anotherNoOwner.getState());
    }

    @Test
    public void afterNavigation_group2NavigationAftergroup1_beansAreDestroyed() {
        Mockito.when(event.getNavigationTarget()).thenReturn((Class) Group1.class);
        beforeNavigationTrigger.fire(event);

        MemberOfGroup1 bean1 = getMemberOfGroupProducer(contextual).get();
        bean1.setState(STATE);

        Supplier<NoOwnerBean> noOwnerProducer = getNoOwnerProducer();
        NoOwnerBean bean2 = noOwnerProducer.get();
        bean2.setState(STATE);

        Mockito.when(afterEvent.getActiveChain()).thenReturn(Collections.singletonList(new Group2()));
        afterNavigationTrigger.fire(afterEvent);

        Assertions.assertTrue(destroyedBeans.contains(bean1));
        Assertions.assertTrue(destroyedBeans.contains(bean2));
    }

    @Test
    public void preserveOnRefresh_anotherUIHasSameWindowName_beanIsPreserved() {
        UI ui = doSetUp("foo", null);
        Mockito.when(event.getNavigationTarget()).thenReturn((Class) Group1.class);
        beforeNavigationTrigger.fire(event);

        MemberOfGroup1 bean1 = getMemberOfGroupProducer(contextual).get();
        bean1.setState(STATE);

        // set another UI instance with the same window name into the context
        doSetUp("foo", ui.getSession());
        Mockito.when(event.getNavigationTarget()).thenReturn((Class) Group1.class);
        beforeNavigationTrigger.fire(event);

        ComponentUtil.onComponentDetach(ui);

        MemberOfGroup1 anotherBean = (MemberOfGroup1) routeContext.get(contextual, creationalContext);

        Assertions.assertFalse(destroyedBeans.contains(bean1));
        Assertions.assertEquals(STATE, anotherBean.getState());
    }

    @Test
    public void onBeforeEnter_anotherUIHasNoWindowName_beanIsDestroyedOnUiDestroy() {
        UI ui = doSetUp("foo", null);
        Mockito.when(event.getNavigationTarget()).thenReturn((Class) Group1.class);
        beforeNavigationTrigger.fire(event);

        MemberOfGroup1 bean1 = getMemberOfGroupProducer(contextual).get();
        bean1.setState(STATE);

        // set another UI instance with the same window name into the context
        doSetUp(null, ui.getSession());
        Mockito.when(event.getNavigationTarget()).thenReturn((Class) Group1.class);
        beforeNavigationTrigger.fire(event);

        ComponentUtil.onComponentDetach(ui);

        MemberOfGroup1 anotherBean = (MemberOfGroup1) routeContext.get(contextual, creationalContext);

        Assertions.assertTrue(destroyedBeans.contains(bean1));
        Assertions.assertNotEquals(STATE, anotherBean.getState());
    }

    private UI doSetUp(String windowName, VaadinSession session) {
        changeEvent = Mockito.mock(LocationChangeEvent.class);

        Mockito.when(data.getNavigationTarget()).thenReturn((Class) InitialRoute.class);
        event = Mockito.mock(BeforeEnterEvent.class);
        uiUnderTestContext = new UIUnderTestContext(session) {

            @Override
            public void activate() {
                super.activate();

                if (windowName != null) {
                    ExtendedClientDetails details = Mockito.mock(ExtendedClientDetails.class);
                    Mockito.when(details.getWindowName()).thenReturn(windowName);
                    getUi().getInternals().setExtendedClientDetails(details);
                }

                ComponentUtil.setData(getUi(), NavigationData.class, data);
                Mockito.when(changeEvent.getUI()).thenReturn(getUi());
                Mockito.when(event.getUI()).thenReturn(uiUnderTestContext.getUi());
            }
        };
        uiUnderTestContext.activate();

        UI ui = uiUnderTestContext.getUi();

        uiUnderTestContext.getUi().getSession().addUI(ui);

        afterEvent = Mockito.mock(AfterNavigationEvent.class);
        Mockito.when(afterEvent.getLocationChangeEvent()).thenReturn(changeEvent);

        return uiUnderTestContext.getUi();
    }

    private Supplier<NoOwnerBean> getNoOwnerProducer() {
        InjectableBean injectableBean = Mockito.mock(InjectableBean.class);
        mockRouteScopeOwner(NoOwnerBean.class, NoOwnerBean::new, injectableBean);
        return () -> (NoOwnerBean) routeContext.get(injectableBean, (CreationalContext) creationalContext);
    }

    private Supplier<MemberOfGroup1> getMemberOfGroupProducer(InjectableBean injectableBean) {
        mockRouteScopeOwner(MemberOfGroup1.class, MemberOfGroup1::new, injectableBean);
        return () -> (MemberOfGroup1) routeContext.get(injectableBean, (CreationalContext) creationalContext);
    }

    private <T extends TestBean> void mockRouteScopeOwner(
            Class<T> beanType, Supplier<T> supplier, InjectableBean<T> bean) {
        Mockito.doAnswer(invocation -> supplier.get()).when(bean).create((CreationalContext) creationalContext);

        Annotation owher = getOwner(beanType);
        if (owher != null) {
            Mockito.when(bean.getQualifiers()).thenReturn(Collections.singleton(getOwner(beanType)));
        }

        Mockito.when(bean.getBeanClass()).thenReturn((Class) beanType);

        Mockito.doAnswer(invocation -> {
                    destroyedBeans.add(invocation.getArgument(0));
                    return null;
                })
                .when((InjectableBean) bean)
                .destroy(Mockito.any(), Mockito.eq(creationalContext));
    }

    private Annotation getOwner(Class<?> beanClass) {
        return beanClass.getAnnotation(RouteScopeOwner.class);
    }
}
