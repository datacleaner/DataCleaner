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
package org.datacleaner.widgets;

import java.util.HashSet;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JMenu;

import org.datacleaner.api.ComponentCategory;
import org.datacleaner.util.IconUtils;

/**
 * A menu folder used to group together similar descriptors, eg "coalesce"
 * transformers or "conversion" transformers.
 */
public class DescriptorMenu extends JMenu implements Comparable<DescriptorMenu> {

    private static final long serialVersionUID = 1L;

    private final ComponentCategory _componentCategory;
    private final Set<Class<?>> _componentClasses;

    public DescriptorMenu(final ComponentCategory componentCategory) {
        super(componentCategory.getName());
        _componentCategory = componentCategory;
        _componentClasses = new HashSet<>();
    }

    @Override
    public Icon getIcon() {
        return IconUtils.getComponentCategoryIcon(_componentCategory, IconUtils.ICON_SIZE_MENU_ITEM);
    }

    public ComponentCategory getComponentCategory() {
        return _componentCategory;
    }

    public DescriptorMenu addComponentClass(final Class<?> clazz) {
        _componentClasses.add(clazz);
        return this;
    }

    public DescriptorMenu addComponentClasses(final Class<?>... classes) {
        for (final Class<?> clazz : classes) {
            addComponentClass(clazz);
        }
        return this;
    }

    public int getComponentClassCount() {
        return _componentClasses.size();
    }

    public boolean containsComponentClass(final Class<?> clazz) {
        return _componentClasses.contains(clazz);
    }

    @Override
    public int compareTo(final DescriptorMenu o) {
        final int diff = getText().compareTo(o.getText());
        if (diff == 0) {
            if (getComponentCategory().equals(o.getComponentCategory())) {
                return 0;
            }
            return -1;
        }
        return diff;
    }
}
