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
package org.eobjects.datacleaner.widgets;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.eobjects.analyzer.beans.api.ComponentCategory;
import org.eobjects.analyzer.beans.writers.WriteDataCategory;
import org.eobjects.analyzer.descriptors.BeanDescriptor;
import org.eobjects.analyzer.util.CollectionUtils2;
import org.eobjects.datacleaner.util.DisplayNameComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Popup menu that groups together bean descriptors by their shared
 * {@link ComponentCategory}.
 * 
 * @param <E>
 */
public abstract class DescriptorMenuBuilder {

    private static final Logger logger = LoggerFactory.getLogger(DescriptorMenuBuilder.class);

    private final List<? extends BeanDescriptor<?>> _descriptors;
    private final boolean _buildSubmenus;

    public DescriptorMenuBuilder(Collection<? extends BeanDescriptor<?>> descriptors) {
        this(descriptors, true);
    }

    public DescriptorMenuBuilder(Collection<? extends BeanDescriptor<?>> descriptors, boolean buildSubmenus) {
        _descriptors = CollectionUtils2.sorted(descriptors, new DisplayNameComparator());
        _buildSubmenus = buildSubmenus;
    }

    public void addItemsToMenu(JMenu menu) {
        initialize(menu);
    }

    public void addItemsToPopupMenu(JPopupMenu menu) {
        initialize(menu);
    }

    private void initialize(final JComponent outerMenu) {
        if (!_buildSubmenus) {
            for (BeanDescriptor<?> descriptor : _descriptors) {
                final JMenuItem menuItem = createMenuItem(descriptor);
                outerMenu.add(menuItem);
            }
            return;
        }
        
        final Map<ComponentCategory, DescriptorMenu> descriptorMenus = new HashMap<ComponentCategory, DescriptorMenu>();
        
        // build sub menus
        {
            for (BeanDescriptor<?> descriptor : _descriptors) {
                final Set<ComponentCategory> componentCategories = descriptor.getComponentCategories();
                for (ComponentCategory componentCategory : componentCategories) {
                    DescriptorMenu menu = descriptorMenus.get(componentCategory);
                    if (menu == null) {
                        menu = new DescriptorMenu(componentCategory);
                        descriptorMenus.put(componentCategory, menu);
                    }
                    menu.addComponentClass(descriptor.getComponentClass());
                }
            }
        }

        {
            // place sub menus
            final List<DescriptorMenu> sortedMenus = CollectionUtils2.sorted(descriptorMenus.values());
            for (DescriptorMenu descriptorMenu : sortedMenus) {
                final int count = descriptorMenu.getComponentClassCount();
                if (count <= 1) {
                    // disregard categories with only a single component in
                    // them!
                    ComponentCategory category = descriptorMenu.getComponentCategory();
                    logger.info("Disregarding menu for category '{}' because of too few components ({})", category,
                            count);
                    descriptorMenus.remove(category);
                } else {
                    // add menu
                    outerMenu.add(descriptorMenu);
                }
            }
        }

        // place items that are not in any submenus
        {
            for (final BeanDescriptor<?> descriptor : _descriptors) {
                boolean placedInSubmenu = false;
                final Class<?> componentClass = descriptor.getComponentClass();
                JMenuItem menuItem = createMenuItem(descriptor);
                if (menuItem != null) {
                    for (DescriptorMenu descriptorMenu : descriptorMenus.values()) {
                        if (descriptorMenu.containsComponentClass(componentClass)) {
                            descriptorMenu.add(menuItem);
                            placedInSubmenu = true;

                            // create a new menu item (or else it will be moved
                            // instead of added.
                            menuItem = createMenuItem(descriptor);
                        }
                    }

                    if (!placedInSubmenu) {
                        // add menu item
                        outerMenu.add(menuItem);
                    }
                }
            }
        }

        // disregard WriteDataCategory
        final DescriptorMenu writeDataMenu = descriptorMenus.get(new WriteDataCategory());
        if (writeDataMenu != null) {
            outerMenu.remove(writeDataMenu);
        }
    }

    protected abstract JMenuItem createMenuItem(BeanDescriptor<?> descriptor);

}
