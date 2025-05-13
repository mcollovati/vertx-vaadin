package org.vaadin.crudui.demo.ui;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.shared.SlotUtils;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.*;

import com.vaadin.flow.server.auth.AnonymousAllowed;

import jakarta.annotation.security.RolesAllowed;
import org.vaadin.crudui.demo.DemoUtils;
import org.vaadin.crudui.demo.authentication.AccessControl;
import org.vaadin.crudui.demo.ui.view.CustomizedView;
import org.vaadin.crudui.demo.ui.view.DefaultView;
import org.vaadin.crudui.demo.ui.view.HomeView;
import org.vaadin.crudui.demo.ui.view.TreeView;


@JsModule("./theme-handler.js")
@RolesAllowed(AccessControl.ADMIN_ROLE_NAME)
@Menu(title = "Ignite Table admin", icon = "vaadin:table")
@Route("crudui")
public class MainLayout extends VerticalLayout implements RouterLayout, BeforeEnterObserver, AfterNavigationObserver {
	private HorizontalLayout headerLayout;
	private VerticalLayout viewContainer = new VerticalLayout();
	private HorizontalLayout footer = new HorizontalLayout();
	private Tabs tabs = new Tabs();
	private Image logo = new Image();
	private Button themeSwitcher = new Button(VaadinIcon.MOON_O.create());
	private Map<Tab, Class<? extends HasComponents>> tabToView = new LinkedHashMap<>();
	private Map<Class<? extends HasComponents>, Tab> viewToTab = new LinkedHashMap<>();

	public MainLayout() {
		logo.addClassName("logo");
		logo.setHeight("44px");

		tabs.addSelectedChangeListener(this::tabsSelectionChanged);
		addTab(HomeView.class);
		addTab(CustomizedView.class);
		addTab(DefaultView.class);
		addTab(TreeView.class);

		tabs.setSelectedIndex(0);
		tabs.setAutoselect(true);


		themeSwitcher.setId("theme-switch");
		themeSwitcher.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
		themeSwitcher.addClickListener(e -> UI.getCurrent().getPage().executeJs("window.switchTheme()"));

		headerLayout = new HorizontalLayout(logo, tabs, themeSwitcher);
		headerLayout.setMargin(true);
		headerLayout.setWidthFull();
		headerLayout.expand(tabs);
		addToNavbar(true,headerLayout);

		viewContainer.setSizeFull();
		viewContainer.setPadding(false);

		footer.setSpacing(false);
		footer.setMargin(false);
		footer.setPadding(true);

		//var content = this; //new VerticalLayout();
		this.setSizeFull();
		this.add(viewContainer, footer);

		UI.getCurrent().getPage().executeJs("window.applySystemTheme()");
	}

	public void addToNavbar(boolean touchOptimized, Component... components) {
		String slot = "navbar" + (touchOptimized ? " touch-optimized" : "");
		//SlotUtils.addToSlot(this, slot, components);
		this.add(components);
	}

	public Component getContent() {
		return this.viewContainer;
	}

	public void setContent(Component content) {
		this.removeAll();
		if (content != null) {
			content.getElement().removeAttribute("slot");
			this.add(headerLayout);
			this.add(content);
			this.add(footer);
		}
	}

	public void showRouterLayoutContent(HasElement content) {
		if(content==null){
			UI.getCurrent().navigate(HomeView.class);
			return;
		}
		viewContainer.removeAll();
		viewContainer.add(content.getElement().getComponent().get());
		afterNavigation(null);
	}

	private void tabsSelectionChanged(Tabs.SelectedChangeEvent event) {
		if (event.isFromClient()) {
			UI.getCurrent().navigate((Class<? extends Component>) tabToView.get(event.getSelectedTab()));
		}
	}

	private void addTab(Class<? extends HasComponents> clazz) {
		Tab tab = new Tab(DemoUtils.getViewName(clazz));
		tabs.add(tab);
		tabToView.put(tab, clazz);
		viewToTab.put(clazz, tab);
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		selectTabByCurrentView(event);
	}

	public void selectTabByCurrentView(BeforeEnterEvent event) {
		Class<?> viewClass = event.getNavigationTarget();
		Tab selectTab = viewToTab.get(viewClass);
		// 如果Tabs为空，设置默认的Tab
		if (selectTab == null) {
			tabs.setSelectedIndex(0);
		}
		else {
			tabs.setSelectedTab(selectTab);
		}
	}

	@Override
	public void afterNavigation(AfterNavigationEvent event) {
		updatePageTitle();
		addSourceCodeAnchorToCurrentView();
	}

	public void updatePageTitle() {
		Class<? extends HasComponents> viewClass = tabToView.get(tabs.getSelectedTab());
		if(viewClass==null) return;
		UI.getCurrent().getPage().setTitle(DemoUtils.getViewName(viewClass) + " - " + "Crud UI add-on demo");
	}

	public void addSourceCodeAnchorToCurrentView() {
		footer.removeAll();
		Class<? extends HasComponents> viewClass = tabToView.get(tabs.getSelectedTab());
		if(viewClass==null) return;
		if (!HomeView.class.equals(viewClass)) {
			footer.add(
					new Html("<span>Source code 👉&nbsp;</span>"),
					new Anchor(DemoUtils.getGitHubLink(viewClass), viewClass.getSimpleName() + ".java"));
		}
	}

}
