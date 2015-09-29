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
import java.util.Comparator;
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
        public void addComponentDescriptor(ComponentDescriptor<?> descriptor);

        /**
         * Will be called once for each category, in sorted order.
         * 
         * @param category
         */
        public void addCategory(ComponentCategory category);
    }

    private static final Logger logger = LoggerFactory.getLogger(DescriptorMenuBuilder.class);

    private final AnalysisJobBuilder _analysisJobBuilder;
    private final UsageLogger _usageLogger;
    private final Collection<? extends ComponentDescriptor<?>> _componentDescriptors;
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
        _componentDescriptors = Collections.unmodifiableCollection(componentDescriptors);
    }

    public DescriptorMenuBuilder(final AnalysisJobBuilder analysisJobBuilder, final UsageLogger usageLogger,
            final ComponentSuperCategory superCategory, final Point2D coordinate, final boolean buildSubmenus) {
        _analysisJobBuilder = analysisJobBuilder;
        _usageLogger = usageLogger;
        _coordinate = coordinate;
        _buildSubmenus = buildSubmenus;

        final DescriptorProvider descriptorProvider = analysisJobBuilder.getConfiguration().getEnvironment().getDescriptorProvider();
        final Collection<? extends ComponentDescriptor<?>> componentDescriptors = descriptorProvider
                .getComponentDescriptorsOfSuperCategory(superCategory);
        _componentDescriptors = Collections.unmodifiableCollection(componentDescriptors);
    }

    public void addItemsToMenu(JMenu menu) {
        initialize(menu);
    }

    public void addItemsToPopupMenu(JPopupMenu menu) {
        initialize(menu);
    }

    public static void createMenuStructure(final MenuCallback callback,
            Collection<? extends ComponentDescriptor<?>> componentDescriptors, boolean buildSubmenus) {
        final List<? extends ComponentDescriptor<?>> sortedComponentDescriptors = CollectionUtils2
                .sorted(componentDescriptors);
        final Collection<? extends ComponentDescriptor<?>> filteredDescriptors = CollectionUtils.filter(sortedComponentDescriptors,
                new DeprecatedComponentPredicate());

        final Map<ComponentCategory, List<Class<?>>> categories = new HashMap<>();


        // build sub menus
        {
            for (ComponentDescriptor<?> descriptor : filteredDescriptors) {
                final Set<ComponentCategory> componentCategories = descriptor.getComponentCategories();
                for (ComponentCategory componentCategory : componentCategories) {
                    List<Class<?>> categoryList = categories.get(componentCategory);
                    if (categoryList == null) {
                        categoryList = new ArrayList<Class<?>>();
                        categories.put(componentCategory, categoryList);
                    }
                    categoryList.add(descriptor.getComponentClass());
                }
            }
        }

        {
            // place sub menus
            final List<ComponentCategory> sortedCategories = CollectionUtils2.sorted(categories.keySet(),
                    new Comparator<ComponentCategory>() {
                        public int compare(ComponentCategory o1, ComponentCategory o2) {
                            return o1.getName().compareTo(o2.getName());
                        }
                    });

            for (ComponentCategory category : sortedCategories) {
                final int count = categories.get(category).size();
                if (count <= 1) {
                    // disregard categories with only a single component in
                    // them!
                    logger.info("Disregarding menu for category '{}' because of too few components ({})", category,
                            count);
                    categories.remove(category);
                } else {
                    // add menu
                    callback.addCategory(category);
                }
            }
        }

        for (ComponentDescriptor<?> descriptor : filteredDescriptors) {
            callback.addComponentDescriptor(descriptor);
        }

    }

    private void initialize(final JComponent outerMenu) {
        final Map<ComponentCategory, DescriptorMenu> descriptorMenus = new HashMap<ComponentCategory, DescriptorMenu>();

        MenuCallback callback = new MenuCallback() {
            @Override
            public void addCategory(ComponentCategory category) {
                DescriptorMenu menu = new DescriptorMenu(category);
                descriptorMenus.put(category, menu);
                outerMenu.add(menu);
            }

            @Override
            public void addComponentDescriptor(ComponentDescriptor<?> descriptor) {
                boolean placedInSubmenu = false;
                for (ComponentCategory category : descriptor.getComponentCategories()) {
                    if (descriptorMenus.containsKey(category)) {
                        placedInSubmenu = true;
                        JMenuItem menuItem = createMenuItem(descriptor);
                        descriptorMenus.get(category).add(menuItem);
                    }
                }

                if (!placedInSubmenu) {
                    outerMenu.add(createMenuItem(descriptor));
                }
            }
        };

        createMenuStructure(callback, _componentDescriptors, _buildSubmenus);
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
