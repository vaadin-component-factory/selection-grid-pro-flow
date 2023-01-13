package com.vaadin.componentfactory.selectiongridpro;

import com.vaadin.componentfactory.selectiongridpro.bean.Person;
import com.vaadin.componentfactory.selectiongridpro.service.PersonService;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;

import java.util.List;

/**
 * Basic example with setItems
 */
@Route(value = "filter", layout = MainLayout.class)
public class FilterView extends Div {

    private TextField filterText = new TextField("Filter");
    private Grid<Person> grid = new SelectionGrid<>();
    private List<Person> personList = getItems();
    private ListDataProvider<Person> dataProvider;

    public FilterView() {
        Div messageDiv = new Div();
        filterText.setPlaceholder("Filter by name...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.EAGER);
        filterText.addValueChangeListener(e -> updateList(e.getValue()));
        dataProvider = new ListDataProvider<>(personList);
        grid.setDataProvider(dataProvider);

        grid.addColumn(Person::getFirstName).setHeader("First Name");
        grid.addColumn(Person::getAge).setHeader("Age");

        grid.setSelectionMode(Grid.SelectionMode.MULTI);

        grid.asMultiSelect().addValueChangeListener(event -> {
            String message = String.format("Selection changed from %s to %s",
                event.getOldValue(), event.getValue());
            messageDiv.setText(message);
        });

        // You can pre-select items
        grid.asMultiSelect().select(personList.get(0), personList.get(1));
        add(filterText, grid, messageDiv);
    }
    public void updateList(String filter) {
        if (filter != null && !filter.isEmpty()) {
            dataProvider.setFilter(person -> person.getFirstName().contains(filter));
        } else {
            dataProvider.clearFilters();
        }
    }
    private List<Person> getItems() {
        PersonService personService = new PersonService();
        return personService.fetchAll();
    }
}
