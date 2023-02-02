/*-
 * #%L
 * Selection Grid Pro
 * %%
 * Copyright (C) 2020 - 2023 Vaadin Ltd
 * %%
 * This program is available under Vaadin Commercial License and Service Terms.
 * 
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 * #L%
 */

import { ThemableMixin } from '@vaadin/vaadin-themable-mixin';
import { ElementMixin } from '@vaadin/component-base/src/element-mixin';
import { GridPro } from  '@vaadin/grid-pro/src/vaadin-grid-pro.js';

import {
    _getItemOverriden,
    _selectionGridSelectRow,
    _selectionGridSelectRowWithItem
} from './helpers';

class VcfSelectionGridPro extends ElementMixin(ThemableMixin(GridPro)) {

    constructor() {
        super();
        this._getItemOverriden = _getItemOverriden.bind(this);
        this._selectionGridSelectRow = _selectionGridSelectRow.bind(this);
        this._selectionGridSelectRowWithItem = _selectionGridSelectRowWithItem.bind(this);
    }

    static get properties() {
        return {
            rangeSelectRowFrom: {
                type: Number,
                value: -1
            }
        };
    }

    ready() {
        super.ready();
        this._getItem = this._getItemOverriden;
    }

    connectedCallback() {
        super.connectedCallback();

    }

    focusOnCell(rowNumber, cellNumber, nbOfCalls = 1) {
        if (nbOfCalls < 11) { // dont make an infinite loop
            if (rowNumber < 0 || cellNumber < 0) {
                throw "index out of bound";
            }
            this.scrollToIndex(rowNumber);
            /** workaround when the expanded node opens children the index is outside the grid size
             * https://github.com/vaadin/vaadin-grid/issues/2060
             * Remove this once this is fixed
             **/
            if (rowNumber > this._effectiveSize) {
                const that = this;
                setTimeout(() => {
                    that.focusOnCell(rowNumber, cellNumber, nbOfCalls + 1);
                }, 200);
            } else {
                this._startToFocus(rowNumber, cellNumber);
            }
            /** End of workaround **/
        }
    };

    _startToFocus(rowNumber, cellNumber) {
        this._rowNumberToFocus = rowNumber;
        this._cellNumberToFocus = cellNumber;
        const row = Array.from(this.$.items.children).filter(
            (child) => child.index === rowNumber
        )[0];
        // if row is already
        if (row) {
            const cell = row.children[cellNumber];
            if (cell) {
                cell.focus();
            } else {
                throw "index out of bound";
            }
        }
    };

    _focus() {
        const rowNumber = this._rowNumberToFocus;
        const cellNumber = this._cellNumberToFocus;
        this._rowNumberToFocus = -1;
        this._cellNumberToFocus = -1;
        const row = Array.from(this.$.items.children).filter(
            (child) => child.index === rowNumber
        )[0];
        const cell = row.children[cellNumber];
        if (cell) {
            cell.focus();
        } else {
            throw "index out of bound";
        }
        this._rowNumberToFocus = -1;
        this._cellNumberToFocus = -1;
    };

    focusOnCellWhenReady(rowIndex, colId, firstCall) {
        if (this.loading || firstCall) {
            var that = this;
            setTimeout(function () {
                that.focusOnCellWhenReady(rowIndex, colId, false);
            }, 1);
        } else {
            this.focusOnCell(rowIndex, colId);
        }
    };

    scrollWhenReady(index, firstCall) {
        if (this.loading || firstCall) {
            var that = this;
            setTimeout(function () {
                that.scrollWhenReady(index, false);
            }, 200);
        } else {
            var that = this;
            setTimeout(function () {
                that.scrollToIndex(index);
            }, 200);
        }
    };


    static get is() {
        /** prefix with vaadin because grid column requires this **/
        return 'vaadin-selection-grid-pro';
    }

    static get version() {
        return '0.2.0';
    }
}

customElements.define(VcfSelectionGridPro.is, VcfSelectionGridPro);

/**
 * @namespace Vaadin
 */
window.Vaadin.VcfSelectionGridPro = VcfSelectionGridPro;
