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

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.apache.metamodel.util.CollectionUtils;
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

    /**
     * Used to get the menu structure. This will be called with all categories
     * first, then all component descriptors.
     *
     */
    public interface MenuCallback {
        /**
         * Will be called once for each descriptor, in sorted order. Always
         * called after {@link #addCategory(ComponentCategory)}, so categories
         * will exist when called.
         *
         * @param descriptor
         */
        void addComponentDescriptor(ComponentDescriptor<?> descriptor);

        /**
         * Will be called once for each category, in sorted order.
         *
         * @param category
         */
        void addCategory(ComponentCategory category);
    }

    private static final Logger logger = LoggerFactory.getLogger(DescriptorMenuBuilder.class);

    private final AnalysisJobBuilder _analysisJobBuilder;
    private final UsageLogger _usageLogger;
    private final Collection<? extends ComponentDescriptor<?>> _componentDescriptors;
    private final Point2D _coordinate;

    public DescriptorMenuBuilder(final AnalysisJobBuilder analysisJobBuilder, final UsageLogger usageLogger,
            final Collection<? extends ComponentDescriptor<?>> descriptors, final Point2D coordinate) {
        final Collection<? extends ComponentDescriptor<?>> filteredDescriptors =
                CollectionUtils.filter(descriptors, new DeprecatedComponentPredicate());
        final List<ComponentDescriptor<?>> componentDescriptors = new ArrayList<>(filteredDescriptors);
        Collections.sort(componentDescriptors, new DisplayNameComparator());

        _analysisJobBuilder = analysisJobBuilder;
        _usageLogger = usageLogger;
        _coordinate = coordinate;
        _componentDescriptors = Collections.unmodifiableCollection(componentDescriptors);
    }

    public DescriptorMenuBuilder(final AnalysisJobBuilder analysisJobBuilder, final UsageLogger usageLogger,
            final ComponentSuperCategory superCategory, final Point2D coordinate) {
        _analysisJobBuilder = analysisJobBuilder;
        _usageLogger = usageLogger;
        _coordinate = coordinate;

        final DescriptorProvider descriptorProvider =
                analysisJobBuilder.getConfiguration().getEnvironment().getDescriptorProvider();
        final Collection<? extends ComponentDescriptor<?>> componentDescriptors =
                descriptorProvider.getComponentDescriptorsOfSuperCategory(superCategory);
        _componentDescriptors = Collections.unmodifiableCollection(componentDescriptors);
    }

    public static void createMenuStructure(final MenuCallback callback,
            final Collection<? extends ComponentDescriptor<?>> componentDescriptors) {
        final Collection<? extends ComponentDescriptor<?>> filteredDescriptors =
                CollectionUtils.filter(componentDescriptors, new DeprecatedComponentPredicate());

        final Map<ComponentCategory, List<Class<?>>> categories = new HashMap<>();
        buildSubMenus(categories, filteredDescriptors);
        placeSubMenus(categories, callback);

        for (final ComponentDescriptor<?> descriptor : filteredDescriptors) {
            callback.addComponentDescriptor(descriptor);
        }
    }

    private static void buildSubMenus(final Map<ComponentCategory, List<Class<?>>> categories,
            final Collection<? extends ComponentDescriptor<?>> componentDescriptors) {
        for (final ComponentDescriptor<?> descriptor : componentDescriptors) {
            final Set<ComponentCategory> componentCategories = descriptor.getComponentCategories();

            for (final ComponentCategory componentCategory : componentCategories) {
                List<Class<?>> categoryList = categories.get(componentCategory);

                if (categoryList == null) {
                    categoryList = new ArrayList<>();
                    categories.put(componentCategory, categoryList);
                }

                categoryList.add(descriptor.getComponentClass());
            }
        }
    }

    private static void placeSubMenus(final Map<ComponentCategory, List<Class<?>>> categories,
            final MenuCallback callback) {
        final List<ComponentCategory> sortedCategories =
                CollectionUtils2.sorted(categories.keySet(), (o1, o2) -> o1.getName().compareTo(o2.getName()));

        for (final ComponentCategory category : sortedCategories) {
            final int count = categories.get(category).size();

            if (count == 0) {
                logger.info("Disregarding menu for category '{}' because of no components", category);
                categories.remove(category);
            } else {
                callback.addCategory(category);
            }
        }
    }

    public void addItemsToMenu(final JMenu menu) {
        initialize(menu);
    }

    public void addItemsToPopupMenu(final JPopupMenu menu) {
        initialize(menu);
    }

    private void initialize(final JComponent outerMenu) {
        final Map<ComponentCategory, DescriptorMenu> descriptorMenus = new HashMap<>();

        final MenuCallback callback = new MenuCallback() {
            @Override
            public void addCategory(final ComponentCategory category) {
                final DescriptorMenu menu = new DescriptorMenu(category);
                descriptorMenus.put(category, menu);
                outerMenu.add(menu);
            }

            @Override
            public void addComponentDescriptor(final ComponentDescriptor<?> descriptor) {
                boolean placedInSubmenu = false;
                for (final ComponentCategory category : descriptor.getComponentCategories()) {
                    if (descriptorMenus.containsKey(category)) {
                        placedInSubmenu = true;
                        final JMenuItem menuItem = createMenuItem(descriptor);
                        descriptorMenus.get(category).add(menuItem);
                    }
                }

                if (!placedInSubmenu) {
                    outerMenu.add(createMenuItem(descriptor));
                }
            }
        };

        createMenuStructure(callback, _componentDescriptors);
    }

    private JMenuItem createMenuItem(final ComponentDescriptor<?> descriptor) {
        final DescriptorMenuItem menuItem = new DescriptorMenuItem(_analysisJobBuilder, _coordinate, descriptor);
        menuItem.addActionListener(e -> _usageLogger.logComponentUsage(descriptor));
        return menuItem;
    }
}
