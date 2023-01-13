package com.vaadin.componentfactory.selectiongridpro;

import java.util.List;

import com.vaadin.componentfactory.selectiongridpro.bean.Person;
import com.vaadin.componentfactory.selectiongridpro.service.PersonService;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.gridpro.GridPro;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

/**
 * Basic example with setItems and beanType in the constructor
 */
@Route(value = "bean", layout = MainLayout.class)
public class BeanGridView extends Div {


    public BeanGridView() {
        Div messageDiv = new Div();

        List<Person> personList = getItems();
        GridPro<Person> grid = new SelectionGridPro<>(Person.class);
        grid.setItems(personList);

        grid.setSelectionMode(Grid.SelectionMode.MULTI);

        grid.asMultiSelect().addValueChangeListener(event -> {
            String message = String.format("Selection changed from %s to %s",
                event.getOldValue(), event.getValue());
            messageDiv.setText(message);
        });

        // You can pre-select items
        grid.asMultiSelect().select(personList.get(0), personList.get(1));
        add(grid, messageDiv);
    }

    private List<Person> getItems() {
        PersonService personService = new PersonService();
        return personService.fetchAll();
    }
}
