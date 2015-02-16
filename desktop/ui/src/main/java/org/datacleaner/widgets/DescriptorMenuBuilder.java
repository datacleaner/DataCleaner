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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.apache.metamodel.util.CollectionUtils;
import org.apache.metamodel.util.ImmutableRef;
import org.apache.metamodel.util.Ref;
import org.datacleaner.api.ComponentCategory;
import org.datacleaner.api.ComponentSuperCategory;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.DescriptorProvider;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.user.UsageLogger;
import org.datacleaner.util.CollectionUtils2;
import org.datacleaner.util.DeprecatedComponentPredicate;
import org.datacleaner.util.DisplayNameComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builder object that can build menus with {@link ComponentDescriptor} items in
 * it. Click a {@link ComponentDescriptor} will add it to the job
 */
public final class DescriptorMenuBuilder {

    private static final Logger logger = LoggerFactory.getLogger(DescriptorMenuBuilder.class);

    private final AnalysisJobBuilder _analysisJobBuilder;
    private final UsageLogger _usageLogger;
    private final Ref<Collection<? extends ComponentDescriptor<?>>> _componentDescriptorsRef;
    private final boolean _buildSubmenus;
    private final Point2D _coordinate;

    public DescriptorMenuBuilder(final AnalysisJobBuilder analysisJobBuilder, final UsageLogger usageLogger,
            final Collection<? extends ComponentDescriptor<?>> descriptors, final Point2D coordinate) {
        this(analysisJobBuilder, usageLogger, descriptors, coordinate, true);
    }

    public DescriptorMenuBuilder(final AnalysisJobBuilder analysisJobBuilder, final UsageLogger usageLogger,
            final ComponentSuperCategory superCategory, final Point2D coordinate) {
        this(analysisJobBuilder, usageLogger, superCategory, coordinate, true);
    }

    public DescriptorMenuBuilder(final AnalysisJobBuilder analysisJobBuilder, final UsageLogger usageLogger,
            final Collection<? extends ComponentDescriptor<?>> descriptors, final Point2D coordinate,
            final boolean buildSubmenus) {
        final Collection<? extends ComponentDescriptor<?>> filteredDescriptors = CollectionUtils.filter(descriptors,
                new DeprecatedComponentPredicate());
        final List<ComponentDescriptor<?>> componentDescriptors = new ArrayList<>(filteredDescriptors);
        Collections.sort(componentDescriptors, new DisplayNameComparator());

        _analysisJobBuilder = analysisJobBuilder;
        _usageLogger = usageLogger;
        _coordinate = coordinate;
        _buildSubmenus = buildSubmenus;
        _componentDescriptorsRef = new ImmutableRef<Collection<? extends ComponentDescriptor<?>>>(componentDescriptors);
    }

    public DescriptorMenuBuilder(final AnalysisJobBuilder analysisJobBuilder, final UsageLogger usageLogger,
            final ComponentSuperCategory superCategory, final Point2D coordinate, final boolean buildSubmenus) {
        _analysisJobBuilder = analysisJobBuilder;
        _usageLogger = usageLogger;
        _coordinate = coordinate;
        _buildSubmenus = buildSubmenus;
        _componentDescriptorsRef = new Ref<Collection<? extends ComponentDescriptor<?>>>() {
            @Override
            public Collection<? extends ComponentDescriptor<?>> get() {
                final DescriptorProvider descriptorProvider = analysisJobBuilder.getConfiguration()
                        .getDescriptorProvider();
                final Collection<? extends ComponentDescriptor<?>> componentDescriptors = descriptorProvider
                        .getComponentDescriptorsOfSuperCategory(superCategory);
                return componentDescriptors;
            }
        };
    }

    public void addItemsToMenu(JMenu menu) {
        initialize(menu);
    }

    public void addItemsToPopupMenu(JPopupMenu menu) {
        initialize(menu);
    }

    private void initialize(final JComponent outerMenu) {
        final Collection<? extends ComponentDescriptor<?>> unsortedComponentDescriptors = _componentDescriptorsRef
                .get();
        final List<? extends ComponentDescriptor<?>> componentDescriptors = CollectionUtils2
                .sorted(unsortedComponentDescriptors);

        if (!_buildSubmenus) {
            for (ComponentDescriptor<?> descriptor : componentDescriptors) {
                final JMenuItem menuItem = createMenuItem(descriptor);
                outerMenu.add(menuItem);
            }
            return;
        }

        final Map<ComponentCategory, DescriptorMenu> descriptorMenus = new HashMap<ComponentCategory, DescriptorMenu>();

        // build sub menus
        {
            for (ComponentDescriptor<?> descriptor : componentDescriptors) {
                for (ComponentCategory componentCategory : descriptor.getComponentCategories()) {
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
            final List<? extends ComponentDescriptor<?>> sortedComponentDescriptors = CollectionUtils2
                    .sorted(componentDescriptors);
            for (final ComponentDescriptor<?> descriptor : sortedComponentDescriptors) {
                boolean placedInSubmenu = false;
                final Class<?> componentClass = descriptor.getComponentClass();
                JMenuItem menuItem = createMenuItem(descriptor);
                if (menuItem != null) {
                    List<DescriptorMenu> sortedMenusDescriptors = CollectionUtils2.sorted(descriptorMenus.values());
                    for (DescriptorMenu descriptorMenu : sortedMenusDescriptors) {
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
    }

    private JMenuItem createMenuItem(final ComponentDescriptor<?> descriptor) {
        final DescriptorMenuItem menuItem = new DescriptorMenuItem(_analysisJobBuilder, _coordinate, descriptor);
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                _usageLogger.logComponentUsage(descriptor);
            }
        });
        return menuItem;
    }

}
