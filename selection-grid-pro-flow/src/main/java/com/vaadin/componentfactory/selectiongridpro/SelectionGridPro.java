package com.vaadin.componentfactory.selectiongridpro;

/*
 * #%L
 * selection-grid-pro-flow
 * %%
 * Copyright (C) 2020 - 2023 Vaadin Ltd
 * %%
 * This program is available under Vaadin Commercial License and Service Terms.
 * 
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 * #L%
 */

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridMultiSelectionModel;
import com.vaadin.flow.component.grid.GridSelectionModel;
import com.vaadin.flow.component.gridpro.GridPro;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.selection.SelectionModel;

@Tag("vaadin-selection-grid-pro")
@CssImport(value = "./styles/grid.css", themeFor = "vaadin-selection-grid-pro")
@JsModule("./src/vcf-selection-grid-pro.js")
@JsModule("./src/selection-grid-pro.js")
public class SelectionGridPro<T> extends GridPro<T> {
  
    private Integer selectRangeOnlyFromIndex = null;
    private Set<T> selectRangeOnlySelection = new HashSet<T>();

    /**
     * @see Grid#Grid()
     */
    public SelectionGridPro() {
        super();
    }

    /**
     * @param pageSize - the page size. Must be greater than zero.
     * @see Grid#Grid(int)
     */
    public SelectionGridPro(int pageSize) {
        super(pageSize);
    }

    /**
     * @param beanType - the bean type to use, not null
     * @see Grid#Grid(Class)
     */
    public SelectionGridPro(Class<T> beanType) {
        super(beanType);
    }

    /**
     * Runs the super.onAttach and hides the multi selection column afterwards (if necessary).
     *
     * @param attachEvent event
     */
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        if (this.getSelectionModel() instanceof SelectionModel.Multi) {
            hideMultiSelectionColumn();
        }
    }

    @Override
    public void scrollToIndex(int rowIndex) {
        super.scrollToIndex(rowIndex);
    }

    /**
     * Focus on the first cell on the row
     *
     * @param item item to scroll and focus
     */
    public void focusOnCell(T item) {
        focusOnCell(item, null);
    }

    /**
     * Focus on the specific column on the row
     *
     * @param item   item to scroll and focus
     * @param column column to focus
     */
    public void focusOnCell(T item, Column<T> column) {
        int index = getIndexForItem(item);
        if (index > 0) {
            int colIndex = (column != null) ? getColumns().indexOf(column) : 0;
            // delay the call of focus on cell if it's used on the same round trip (grid creation + focusCell)
            this.getElement().executeJs("setTimeout(function() { $0.focusOnCell($1, $2) });", getElement(), index, colIndex);
        }
    }


    private int getIndexForItem(T item) {
        return getItemsInOrder().indexOf(item);
    }

    private List<T> getItemsInOrder() {
        DataCommunicator<T> dataCommunicator = super.getDataCommunicator();
        Method fetchFromProvider;
        Method getDataProviderSize;
        try {
            fetchFromProvider = DataCommunicator.class.getDeclaredMethod("fetchFromProvider", int.class, int.class);
            getDataProviderSize = DataCommunicator.class.getDeclaredMethod("getDataProviderSize");
            fetchFromProvider.setAccessible(true);
            getDataProviderSize.setAccessible(true);
            int size = (Integer) getDataProviderSize.invoke(dataCommunicator);
            return ((Stream<T>) fetchFromProvider.invoke(dataCommunicator, 0, size)).collect(Collectors.toList());
        } catch (Exception ignored) {
        }
        return new ArrayList<>();
    }

    private String getColumnInternalId(Column<T> column) {
        Method getInternalId;
        try {
            getInternalId = Column.class.getDeclaredMethod("getInternalId");
            getInternalId.setAccessible(true);
            return (String) getInternalId.invoke(column);
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
        throw new IllegalArgumentException("getInternalId");
    }

    /**
     * Select the range and keep the other items selected
     *
     * @param fromIndex
     * @param toIndex
     */
    @ClientCallable
    private void selectRange(int fromIndex, int toIndex) {
        GridSelectionModel<T> model = getSelectionModel();
        if (model instanceof GridMultiSelectionModel) {
			this.getUI().ifPresent(ui->ui.beforeClientResponse(this, (ctx)->{
				Set<T> newSelectedItems = obtainNewSelectedItems(fromIndex, toIndex);
				asMultiSelect().select(newSelectedItems);
			}));
        }
    }

	@SuppressWarnings("unchecked")
	private Set<T> obtainNewSelectedItems(int fromIndex, int toIndex) {
		DataCommunicator<T> dataCommunicator = super.getDataCommunicator();
		Set<T> newSelectedItems = new HashSet<>();
		int from = Math.min(fromIndex, toIndex);
		int to = Math.max(fromIndex, toIndex) + 1;
		int pageSize = dataCommunicator.getPageSize();
		if (to - from < (pageSize * 2) - 3) {
			// if the range to be retrieved is smaller than 2 pages
			// ask the dataCommunicator to retrieve the items so the cache is used
			for(int i = from; i < to; i++) {
				newSelectedItems.add(dataCommunicator.getItem(i));
			}
		} else {
			// if the range to be retrieved is bigger then use the fetchFromProvider method
			// that load the items in pages reducing the amount of queries to the backend
		    Method fetchFromProvider;
		    try {
		        fetchFromProvider = DataCommunicator.class.getDeclaredMethod("fetchFromProvider", int.class, int.class);
		        fetchFromProvider.setAccessible(true);
		        newSelectedItems.addAll(((Stream<T>) fetchFromProvider.invoke(dataCommunicator, from, to - from + 1)).collect(Collectors.toList()));
		    } catch (Exception ignored) {
		        ignored.printStackTrace();
		    }
		}
		return newSelectedItems;
	}

    /**
     * Select the range and deselect the other items
     *
     * @param fromIndex
     * @param toIndex
     */
    @ClientCallable
    private void selectRangeOnly(int fromIndex, int toIndex) {
		int start = fromIndex < toIndex ? fromIndex : toIndex;
		int end = fromIndex < toIndex ? toIndex : fromIndex;
        GridSelectionModel<T> model = getSelectionModel();
        if (model instanceof GridMultiSelectionModel) {
                      
            Set<T> newSelectedItems = new HashSet<T>();
          
            int calculatedFromIndex = start;
          
            // selectRangeOnlySelection will keep the items already selected so there's no unnecessary
            // call to backend done
            if (!selectRangeOnlySelection.isEmpty()) {
              int firstKey = selectRangeOnlyFromIndex;
              int lastKey = firstKey + selectRangeOnlySelection.size() - 1;

              // recalculate from index so already selected items are not re-selected and no
              // unnecessary call to backend is done
              if (start == firstKey && end > lastKey) {
                calculatedFromIndex = lastKey;
                newSelectedItems.addAll(selectRangeOnlySelection);                
              }
            }
            
            final int calculatedFromIndexFinal = calculatedFromIndex;
			this.getUI().ifPresent(ui->ui.beforeClientResponse(this, (ctx)->{
	            newSelectedItems.addAll(obtainNewSelectedItems(calculatedFromIndexFinal, end));
	            HashSet<T> oldSelectedItems = new HashSet<>(getSelectedItems());
	            oldSelectedItems.removeAll(newSelectedItems);
	            asMultiSelect().updateSelection(newSelectedItems, oldSelectedItems);
			}));
            
            // update selectRangeOnlySelection with new selected items
            selectRangeOnlySelection = new HashSet<T>(getSelectedItems());
            selectRangeOnlyFromIndex = fromIndex;
        }
    }
    
    /**
     * Select the range on click and makes sure selectRangeOnlySelection is cleared.
     * 
     * @param fromIndex
     * @param toIndex
     */
    @ClientCallable
    private void selectRangeOnlyOnClick(int fromIndex, int toIndex) {
        selectRangeOnlySelection.clear();
        selectRangeOnlyFromIndex = null;
		this.getUI().ifPresent(ui->ui.beforeClientResponse(this, (ctx)->{
			this.selectRangeOnly(fromIndex, toIndex);
		}));
    }
       
    @Override
    protected void setSelectionModel(GridSelectionModel<T> model, SelectionMode selectionMode) {
        if (selectionMode == SelectionMode.MULTI) {
            hideMultiSelectionColumn();
        }
        super.setSelectionModel(model, selectionMode);
    }

    /**
     * Runs a JavaScript snippet to hide the multi selection / checkbox column on the client side. The column
     * is not removed, but set to "hidden" explicitly.
     */
    protected void hideMultiSelectionColumn() {
        getElement().getNode().runWhenAttached(ui ->
                ui.beforeClientResponse(this, context ->
                        getElement().executeJs(
                                "if (this.querySelector('vaadin-grid-flow-selection-column')) {" +
                                        " this.querySelector('vaadin-grid-flow-selection-column').hidden = true }")));
    }

    /**
     * Adds theme variants to the component.
     *
     * @param variants theme variants to add
     */
    public void addThemeVariants(SelectionGridVariant... variants) {
        getThemeNames().addAll(Stream.of(variants)
                .map(SelectionGridVariant::getVariantName).collect(Collectors.toList()));
    }

    /**
     * Removes theme variants from the component.
     *
     * @param variants theme variants to remove
     */
    public void removeThemeVariants(SelectionGridVariant... variants) {
        getThemeNames().removeAll(Stream.of(variants)
                .map(SelectionGridVariant::getVariantName).collect(Collectors.toList()));
    }
    
    /**
     * Sets whether right click selection for rows is enabled or not.
     * <ul>
     * <li>if no rows selected, then right click on a row -> select the target row </li>
     * <li>if already few rows selected, then right click on unselected row -> unselect already selected and select target row </li>
	 * <li>if already few rows selected, then right click on selected row -> keep all selected </li>
	 * </ul>
     * 
     * @param enabled true to enable right click selection
     */
    public void setRightClickSelection(boolean enabled) {
    	this.getElement().setProperty("rightClickEnabled", enabled);
    }
    
}
