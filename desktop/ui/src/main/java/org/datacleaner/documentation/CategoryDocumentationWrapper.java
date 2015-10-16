/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Neopost - Customer Information Management
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.datacleaner.documentation;

import java.util.ArrayList;
import java.util.List;

import org.datacleaner.api.ComponentCategory;

/**
 * A wrapper around the {@link ComponentCategory} object to make it easier for
 * the documentation template to get to certain aspects that should be presented
 * in the documentation.
 */
public class CategoryDocumentationWrapper {

    private final List<ComponentDocumentationWrapper> _components;
    private final ComponentCategory _componentCategory;

    public CategoryDocumentationWrapper(ComponentCategory componentCategory) {
        _componentCategory = componentCategory;
        _components = new ArrayList<>();
    }

    public String getName() {
        return _componentCategory.getName();
    }

    public void addComponent(ComponentDocumentationWrapper component) {
        _components.add(component);
    }

    public List<ComponentDocumentationWrapper> getComponents() {
        return _components;
    }
}
