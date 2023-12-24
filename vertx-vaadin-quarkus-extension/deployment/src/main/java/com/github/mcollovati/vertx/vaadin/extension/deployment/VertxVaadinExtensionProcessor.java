package com.github.mcollovati.vertx.vaadin.extension.deployment;

import java.util.Collection;

import com.github.mcollovati.vertx.quarkus.QuarkusInstantiator;
import com.github.mcollovati.vertx.quarkus.annotation.NormalRouteScoped;
import com.github.mcollovati.vertx.quarkus.annotation.NormalUIScoped;
import com.github.mcollovati.vertx.quarkus.annotation.RouteScoped;
import com.github.mcollovati.vertx.quarkus.annotation.UIScoped;
import com.github.mcollovati.vertx.quarkus.annotation.VaadinServiceScoped;
import com.github.mcollovati.vertx.quarkus.annotation.VaadinSessionScoped;
import com.github.mcollovati.vertx.quarkus.context.RouteContextWrapper;
import com.github.mcollovati.vertx.quarkus.context.RouteScopedContext;
import com.github.mcollovati.vertx.quarkus.context.UIContextWrapper;
import com.github.mcollovati.vertx.quarkus.context.UIScopedContext;
import com.github.mcollovati.vertx.quarkus.context.VaadinServiceScopedContext;
import com.github.mcollovati.vertx.quarkus.context.VaadinSessionScopedContext;
import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.router.Route;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.BeanDefiningAnnotationBuildItem;
import io.quarkus.arc.deployment.ContextRegistrationPhaseBuildItem;
import io.quarkus.arc.deployment.CustomScopeBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;

class VertxVaadinExtensionProcessor {

    private static final String FEATURE = "vertx-vaadin-extension";

    private static final DotName ROUTE_ANNOTATION = DotName
        .createSimple(Route.class.getName());

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    public void build(
        final BuildProducer<AdditionalBeanBuildItem> additionalBeanProducer,
        final BuildProducer<BeanDefiningAnnotationBuildItem> additionalBeanDefiningAnnotationRegistry) {
        additionalBeanProducer.produce(AdditionalBeanBuildItem.unremovableOf(QuarkusInstantiator.class.getName()));
        // Make and Route annotated Component a bean for injection
        additionalBeanDefiningAnnotationRegistry
            .produce(new BeanDefiningAnnotationBuildItem(ROUTE_ANNOTATION));
    }

    @BuildStep
    public void specifyErrorViewsBeans(CombinedIndexBuildItem item,
                                       BuildProducer<AdditionalBeanBuildItem> additionalBeanProducer) {
        Collection<ClassInfo> errors = item.getComputingIndex()
            .getAllKnownImplementors(DotName
                .createSimple(HasErrorParameter.class.getName()));
        for (ClassInfo errorInfo : errors) {
            additionalBeanProducer.produce(AdditionalBeanBuildItem
                .unremovableOf(errorInfo.name().toString()));
        }
    }

    /*
    @BuildStep
    void mapVaadinServletPaths(final BeanArchiveIndexBuildItem beanArchiveIndex,
                               final BuildProducer<ServletBuildItem> servletProducer) {
        final IndexView indexView = beanArchiveIndex.getIndex();

        // Collect all VaadinServlet instances and remove QuarkusVaadinServlet
        // and VaadinServlet from the list.
        final Collection<ClassInfo> vaadinServlets = indexView
            .getAllKnownSubclasses(
                DotName.createSimple(VaadinServlet.class.getName()))
            .stream()
            .filter(servlet -> !servlet.name().toString()
                .equals(QuarkusVaadinServlet.class.getName())
                && !servlet.name().toString()
                .equals(VaadinServlet.class.getName()))
            .collect(Collectors.toList());

        // If no VaadinServlet instances found register QuarkusVaadinServlet
        if (vaadinServlets.isEmpty()) {
            servletProducer.produce(ServletBuildItem
                .builder(QuarkusVaadinServlet.class.getName(),
                    QuarkusVaadinServlet.class.getName())
                .addMapping("/*").setAsyncSupported(true).build());
        } else {
            registerUserServlets(servletProducer, vaadinServlets);
        }
    }
    */

    @BuildStep
    ContextRegistrationPhaseBuildItem.ContextConfiguratorBuildItem registerVaadinServiceScopedContext(
        ContextRegistrationPhaseBuildItem phase) {
        return new ContextRegistrationPhaseBuildItem.ContextConfiguratorBuildItem(
            phase.getContext().configure(VaadinServiceScoped.class).normal()
                .contextClass(VaadinServiceScopedContext.class));
    }

    @BuildStep
    CustomScopeBuildItem serviceScope() {
        return new CustomScopeBuildItem(VaadinServiceScoped.class);
    }

    @BuildStep
    ContextRegistrationPhaseBuildItem.ContextConfiguratorBuildItem registerVaadinSessionScopedContext(
        ContextRegistrationPhaseBuildItem phase) {
        return new ContextRegistrationPhaseBuildItem.ContextConfiguratorBuildItem(
            phase.getContext().configure(VaadinSessionScoped.class).normal()
                .contextClass(VaadinSessionScopedContext.class));
    }

    @BuildStep
    CustomScopeBuildItem sessionScope() {
        return new CustomScopeBuildItem(VaadinSessionScoped.class);
    }

    @BuildStep
    ContextRegistrationPhaseBuildItem.ContextConfiguratorBuildItem registerUIScopedContext(
        ContextRegistrationPhaseBuildItem phase) {
        return new ContextRegistrationPhaseBuildItem.ContextConfiguratorBuildItem(
            phase.getContext().configure(NormalUIScoped.class).normal()
                .contextClass(UIScopedContext.class));
    }

    @BuildStep
    ContextRegistrationPhaseBuildItem.ContextConfiguratorBuildItem registerPseudoUIScopedContext(
        ContextRegistrationPhaseBuildItem phase) {
        return new ContextRegistrationPhaseBuildItem.ContextConfiguratorBuildItem(
            phase.getContext().configure(UIScoped.class)
                .contextClass(UIContextWrapper.class));
    }

    @BuildStep
    CustomScopeBuildItem normalUiScope() {
        return new CustomScopeBuildItem(NormalUIScoped.class);
    }

    @BuildStep
    CustomScopeBuildItem uiScope() {
        return new CustomScopeBuildItem(UIScoped.class);
    }

    @BuildStep
    ContextRegistrationPhaseBuildItem.ContextConfiguratorBuildItem registerRouteScopedContext(
        ContextRegistrationPhaseBuildItem phase) {
        return new ContextRegistrationPhaseBuildItem.ContextConfiguratorBuildItem(
            phase.getContext().configure(NormalRouteScoped.class).normal()
                .contextClass(RouteScopedContext.class));
    }

    @BuildStep
    ContextRegistrationPhaseBuildItem.ContextConfiguratorBuildItem registerPseudoRouteScopedContext(
        ContextRegistrationPhaseBuildItem phase) {
        return new ContextRegistrationPhaseBuildItem.ContextConfiguratorBuildItem(
            phase.getContext().configure(RouteScoped.class)
                .contextClass(RouteContextWrapper.class));
    }

    @BuildStep
    CustomScopeBuildItem normalRouteScope() {
        return new CustomScopeBuildItem(NormalRouteScoped.class);
    }

    @BuildStep
    CustomScopeBuildItem rRouteScope() {
        return new CustomScopeBuildItem(RouteScoped.class);
    }

    /*
    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    void setupPush(ServletDeploymentManagerBuildItem deployment,
                   WebSocketDeploymentInfoBuildItem webSocketDeploymentInfoBuildItem,
                   ServerWebSocketContainerBuildItem serverWebSocketContainerBuildItem,
                   BuildProducer<FilterBuildItem> filterProd,
                   WebsocketHttpSessionAttachRecorder recorder) {

        filterProd.produce(new FilterBuildItem(recorder.createWebSocketHandler(
            webSocketDeploymentInfoBuildItem.getInfo(),
            serverWebSocketContainerBuildItem.getContainer(),
            deployment.getDeploymentManager()), 120));
    }

    private void registerUserServlets(
        BuildProducer<ServletBuildItem> servletProducer,
        Collection<ClassInfo> vaadinServlets) {
        // TODO: check that we don't register 2 of the same mapping
        for (ClassInfo info : vaadinServlets) {
            final AnnotationInstance webServletInstance = info.classAnnotation(
                DotName.createSimple(WebServlet.class.getName()));
            String servletName = Optional
                .ofNullable(webServletInstance.value("name"))
                .map(AnnotationValue::asString)
                .orElse(info.name().toString());
            final ServletBuildItem.Builder servletBuildItem = ServletBuildItem
                .builder(servletName, info.name().toString());

            Stream.of(webServletInstance.value("value"),
                    webServletInstance.value("urlPatterns"))
                .filter(Objects::nonNull)
                .flatMap(value -> Stream.of(value.asStringArray()))
                .forEach(servletBuildItem::addMapping);

            addWebInitParameters(webServletInstance, servletBuildItem);
            setAsyncSupportedIfDefined(webServletInstance, servletBuildItem);

            servletProducer.produce(servletBuildItem.build());
        }
    }

    private void addWebInitParameters(AnnotationInstance webServletInstance,
                                      ServletBuildItem.Builder servletBuildItem) {
        // Add WebInitParam parameters to registration
        AnnotationValue initParams = webServletInstance.value("initParams");
        if (initParams != null) {
            for (AnnotationInstance initParam : initParams.asNestedArray()) {
                servletBuildItem.addInitParam(
                    initParam.value("name").asString(),
                    initParam.value().asString());
            }
        }
    }

    private void setAsyncSupportedIfDefined(
        AnnotationInstance webServletInstance,
        ServletBuildItem.Builder servletBuildItem) {
        final AnnotationValue asyncSupported = webServletInstance
            .value("asyncSupported");
        if (asyncSupported != null) {
            servletBuildItem.setAsyncSupported(asyncSupported.asBoolean());
        }
    }
     */


}
