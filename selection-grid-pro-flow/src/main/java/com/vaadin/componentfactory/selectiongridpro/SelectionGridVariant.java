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
/**
 * Set of theme variants applicable for {@code vaadin-selection-grid} component.
 * @author Stefan Uebe
 */
public enum SelectionGridVariant {
    SELECTABLE_TEXT("selectable-text");

    private final String variant;

    SelectionGridVariant(String variant) {
        this.variant = variant;
    }

    /**
     * Gets the variant name.
     *
     * @return variant name
     */
    public String getVariantName() {
        return variant;
    }
}
