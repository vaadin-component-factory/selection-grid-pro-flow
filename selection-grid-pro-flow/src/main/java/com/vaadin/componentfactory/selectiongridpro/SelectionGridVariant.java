package com.vaadin.componentfactory.selectiongridpro;
/*
 * #%L
 * selection-grid-pro-flow
 * %%
 * Copyright (C) 2020 - 2023 Vaadin Ltd
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
