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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.datacleaner.api.ComponentCategory;
import org.datacleaner.api.ComponentSuperCategory;
import org.datacleaner.util.HasNameComparator;

/**
 * A wrapper around the {@link ComponentSuperCategory} object to make it easier
 * for the documentation template to get to certain aspects that should be
 * presented in the documentation.
 */
public class SuperCategoryDocumentationWrapper implements Comparable<SuperCategoryDocumentationWrapper> {

    private final ComponentSuperCategory _superCategory;
    private final List<ComponentDocumentationWrapper> _components;
    private final Map<ComponentCategory, CategoryDocumentationWrapper> _categories;

    public SuperCategoryDocumentationWrapper(ComponentSuperCategory superCategory) {
        _superCategory = superCategory;
        _components = new ArrayList<>();
        _categories = new TreeMap<>(new HasNameComparator());
    }

    @Override
    public int compareTo(SuperCategoryDocumentationWrapper o) {
        return _superCategory.compareTo(o._superCategory);
    }

    public void addComponent(ComponentDocumentationWrapper component) {
        _components.add(component);
    }

    public List<ComponentDocumentationWrapper> getComponents() {
        return _components;
    }

    public String getName() {
        return _superCategory.getName();
    }

    public Collection<CategoryDocumentationWrapper> getCategories() {
        return _categories.values();
    }

    public void addComponent(ComponentCategory componentCategory, ComponentDocumentationWrapper component) {
        CategoryDocumentationWrapper categoryWrapper = _categories.get(componentCategory);
        if (categoryWrapper == null) {
            categoryWrapper = new CategoryDocumentationWrapper(componentCategory);
            _categories.put(componentCategory, categoryWrapper);
        }
        categoryWrapper.addComponent(component);
    }

}
