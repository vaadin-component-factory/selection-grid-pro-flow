package com.vaadin.componentfactory.selectiongrid;

/*
 * #%L
 * selection-grid-flow
 * %%
 * Copyright (C) 2020 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file license.html distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <http://vaadin.com/license/cval-3>.
 * #L%
 */

import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataCommunicator;
import com.vaadin.flow.data.provider.hierarchy.HierarchyMapper;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@JsModule("./src/selection-grid-connector.js")
public class SelectionGrid<T> extends Grid<T> {

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
     * @param item item to scroll and focus
     * @param columnKey column to focus
     */
    public void focusOnCell(T item, String columnKey) {
        String index = getIndexForItem(item);
        System.out.println("index=" + index);
        if (index != null) {
            int rowIndex = Integer.parseInt(index);
            rowIndex--;
            if (columnKey != null) {
                Column<T> columnByKey = getColumnByKey(columnKey);
                System.out.println("columnByKey " + columnByKey.getKey());
            }
            int colIndex = (columnKey == null)? 0: 1;
            this.getElement().executeJs("this.focusOnCell($0, $1);", rowIndex, colIndex);
        }
    }


    private String getIndexForItem(T item) {

        DataCommunicator<T> dataCommunicator = super.getDataCommunicator();
        return dataCommunicator.getKeyMapper().key(item);
    }
}
