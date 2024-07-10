package com.vaadin.componentfactory.selectiongridpro;

import com.vaadin.componentfactory.selectiongridpro.bean.Person;
import com.vaadin.componentfactory.selectiongridpro.service.PersonData;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Demo showing lazy data loading with filtering and sorting.
 */
@Route(value = "lazyfilter", layout = MainLayout.class)
public class LazyDataWithFilterView extends VerticalLayout {
  
  private static final long serialVersionUID = 2692354461215796348L;

  private PersonDataProvider personDataProvider = new PersonDataProvider();
    
    private PersonFilter personFilter = new PersonFilter();
    
    private ConfigurableFilterDataProvider<Person, Void, PersonFilter> filterDataProvider = personDataProvider
        .withConfigurableFilter();

    public LazyDataWithFilterView()
    {
        Div messageDiv = new Div();

        SelectionGridPro<Person> grid = new SelectionGridPro<>();
        grid.setDataProvider(filterDataProvider);

        grid.addEditColumn(Person::getFirstName).text(Person::setFirstName).setHeader("First Name").setSortProperty("firstName");
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
        
        TextField searchField = new TextField();
        searchField.setWidth("50%");
        searchField.setPlaceholder("Search");
        searchField.setClearButtonVisible(true);
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(e -> {
            personFilter.setSearchTerm(e.getValue());
            filterDataProvider.setFilter(personFilter);
        });
                            
        VerticalLayout layout = new VerticalLayout(searchField, grid, messageDiv);
        layout.setPadding(false);
        add(layout);
    }
    
    public static class PersonFilter {

      private String searchTerm;

      public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
      }

      public boolean test(Person person) {
        return matches(person.getFirstName(), searchTerm);
      }

      private boolean matches(String value, String searchTerm) {
        return searchTerm == null || searchTerm.isEmpty()
            || value.toLowerCase().contains(searchTerm.toLowerCase());
      }
    }
    
    public static class PersonDataProvider extends AbstractBackEndDataProvider<Person, PersonFilter> {
      PersonData data = new PersonData();
      final List<Person> database = new ArrayList<>(data.getPersons());

      @Override
      protected Stream<Person> fetchFromBackEnd(Query<Person, PersonFilter> query) {
          // A real app should use a real database or a service
          // to fetch, filter and sort data.
          Stream<Person> stream = database.stream();

          // Filtering
          if (query.getFilter().isPresent()) {
              stream = stream.filter(person -> query.getFilter().get().test(person));
          }

          // Sorting
          if (query.getSortOrders().size() > 0) {
              stream = stream.sorted(sortComparator(query.getSortOrders()));
          }

          // Pagination
          return stream.skip(query.getOffset()).limit(query.getLimit());
      }

      @Override
      protected int sizeInBackEnd(Query<Person, PersonFilter> query) {
          return (int) fetchFromBackEnd(query).count();
      }

      private static Comparator<Person> sortComparator(List<QuerySortOrder> sortOrders) {
          return sortOrders.stream().map(sortOrder -> {
              Comparator<Person> comparator = personFieldComparator(sortOrder.getSorted());

              if (sortOrder.getDirection() == SortDirection.DESCENDING) {
                  comparator = comparator.reversed();
              }

              return comparator;
          }).reduce(Comparator::thenComparing).orElse((p1, p2) -> 0);
      }

      private static Comparator<Person> personFieldComparator(String sorted) {
          if (sorted.equals("firstName")) {
              return Comparator.comparing(person -> person.getFirstName());
          } 
          return (p1, p2) -> 0;
      }
  }
}