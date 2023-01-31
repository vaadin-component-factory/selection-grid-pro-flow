package com.vaadin.componentfactory.selectiongridpro;

import java.util.List;

import com.vaadin.componentfactory.selectiongridpro.bean.Person;
import com.vaadin.componentfactory.selectiongridpro.service.PersonService;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.gridpro.GridPro;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

/**
 * Basic example with setItems
 */
@Route(value = "", layout = MainLayout.class)
public class SimpleView extends Div {

    public SimpleView() {
        Div messageDiv = new Div();

        List<Person> personList = getItems();
        GridPro<Person> grid = new SelectionGridPro<>();
        grid.setItems(personList);

        grid.addEditColumn(Person::getFirstName).text(Person::setFirstName).setHeader("First Name");
        grid.addEditColumn(Person::getLastName).text(Person::setLastName).setHeader("Last Name");
        grid.addColumn(Person::getAge).setHeader("Age");
        grid.addEditColumn(Person::isSubscriber).checkbox(Person::setSubscriber)
        .setHeader("Subscriber");

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
