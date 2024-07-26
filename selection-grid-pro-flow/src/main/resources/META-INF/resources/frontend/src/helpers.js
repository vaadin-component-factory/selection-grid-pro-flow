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
/* eslint-disable no-invalid-this */

export function _selectionGridSelectRow(e) {   
    const tr = e.composedPath().find((p) => p.nodeName === "TR");
    if (tr && typeof tr.index != 'undefined') {
        const item = tr._item;
        const index = tr.index;

        this._selectionGridSelectRowWithItem(e, item, index);
    }    
}

export function _debounce(func, wait, immediate) {
    var context = this,
        args = arguments;
    var later = function() {
        window.debounceFunction = null;
        if (!immediate) func.apply(context, args);
    };
    var callNow = immediate && !window.debounceFunction;
    clearTimeout(window.debounceFunction);
    window.debounceFunction = setTimeout(later, wait);
    if (callNow) {
		func.apply(context, args);
	}
};

export function _selectionGridSelectRowWithItem(e, item, index) {
    const ctrlKey = (e.metaKey)?e.metaKey:e.ctrlKey; //(this._ios)?e.metaKey:e.ctrlKey;
    // if click select only this row
    if (!ctrlKey && !e.shiftKey) {
        if (this.$server) {
			this._debounce(() => { 
				this.$server.selectRangeOnlyOnClick(index, index);
            }, 100);
            
        } else {
            this.selectedItems = [];
            this.selectItem(item);
        }
    }
    // if ctrl click
    if (e.shiftKey && this.rangeSelectRowFrom >= 0) {
        if((this.rangeSelectRowFrom - index) !== 0) { // clear text selection, if multiple rows are selected using shift
            const sel = window.getSelection ? window.getSelection() : document.selection;
            if (sel) {
                if (sel.removeAllRanges) {
                    sel.removeAllRanges();
                } else if (sel.empty) {
                    sel.empty();
                }
            }
        }

        if (!ctrlKey) {
            if (this.$server) {
				this._debounce(() => { 
                	this.$server.selectRangeOnly(this.rangeSelectRowFrom, index);
            	}, 100);
            }
        } else {
            if (this.$server) {
				this._debounce(() => { 
                	this.$server.selectRange(this.rangeSelectRowFrom, index);
                }, 100);
            }
        }
    } else {
        if (!ctrlKey) {
            if (this.$server) {
			this._debounce(() => { 
				this.$server.selectRangeOnlyOnClick(index, index);
            }, 100);
            }
        } else {
            if (this.selectedItems && this.selectedItems.some((i) => i.key === item.key)) {
                if (this.$connector) {
                    this.$connector.doDeselection([item], true);
                } else {
                    this.deselectItem(item);
                }
            } else {
                if (this.$server) {
					this._debounce(() => { 
                    	this.$server.selectRange(index, index);
                    }, 100);
                }
            }
        }
        this.rangeSelectRowFrom = index;
    }
}

export function _selectionGridRightClickSelectRow(e) {
    const tr = e.composedPath().find((p) => p.nodeName === "TR");
    if (tr && typeof tr.index != 'undefined') {
        const item = tr._item;
        const index = tr.index;

        if (this.$server) {
            if (this.selectedItems && this.selectedItems.some((i) => i.key === item.key)) {
                // keep all selected
                return;
            } else {
				this._debounce(() => { 
                	this.$server.selectRangeOnlyOnClick(index, index);
                }, 100);
            }
        }
    }
}

export function _getItemOverriden(index, el) {
    if (index >= this._effectiveSize) {
        return;
    }
    el.index = index;
    const { cache, scaledIndex } = this._cache.getCacheAndIndex(index);
    const item = cache.items[scaledIndex];
    if (item) {
        this.toggleAttribute("loading", false, el);
        this._updateItem(el, item);
        if (this._isExpanded(item)) {
            cache.ensureSubCacheForScaledIndex(scaledIndex);
        }
    } else {
        this.toggleAttribute("loading", true, el);
        const page = Math.floor(scaledIndex / this.pageSize);
        this._loadPage(page, cache);
    }
    /** focus when get item if there is an item to focus **/
    if (this._rowNumberToFocus > -1) {
        if (index === this._rowNumberToFocus) {
            const row = Array.from(this.$.items.children).filter(
                (child) => child.index === this._rowNumberToFocus
            )[0];
            if (row) {
                this._focus();
            }
        }
    }
};
