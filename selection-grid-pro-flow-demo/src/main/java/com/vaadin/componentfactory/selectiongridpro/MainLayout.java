package com.vaadin.componentfactory.selectiongridpro;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;

public class MainLayout extends AppLayout {

    public MainLayout() {
        final DrawerToggle drawerToggle = new DrawerToggle();
        final RouterLink simple = new RouterLink("Selection Grid", SimpleView.class);
        final RouterLink beanGrid = new RouterLink("Selection Grid with a bean class", BeanGridView.class);
        final RouterLink lazy = new RouterLink("Lazy Grid view", LazyDataView.class);
        final VerticalLayout menuLayout = new VerticalLayout(simple, beanGrid, lazy);
        addToDrawer(menuLayout);
        addToNavbar(drawerToggle);
    }

}