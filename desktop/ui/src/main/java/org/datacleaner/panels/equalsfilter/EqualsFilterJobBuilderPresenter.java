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
package org.datacleaner.panels.equalsfilter;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.datacleaner.beans.filter.EqualsFilter;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.descriptors.FilterDescriptor;
import org.datacleaner.job.builder.FilterComponentBuilder;
import org.datacleaner.panels.ConfiguredPropertyTaskPane;
import org.datacleaner.panels.FilterComponentBuilderPanel;
import org.datacleaner.panels.FilterComponentBuilderPresenter;
import org.datacleaner.util.IconUtils;
import org.datacleaner.widgets.properties.PropertyWidgetFactory;

/**
 * Specialized {@link FilterComponentBuilderPresenter} for the {@link EqualsFilter}.
 */
class EqualsFilterComponentBuilderPresenter extends FilterComponentBuilderPanel {

    private static final long serialVersionUID = 1L;

    public EqualsFilterComponentBuilderPresenter(FilterComponentBuilder<?, ?> filterJobBuilder, WindowContext windowContext,
            PropertyWidgetFactory propertyWidgetFactory) {
        super(filterJobBuilder, windowContext, propertyWidgetFactory);
    }

    @Override
    protected List<ConfiguredPropertyTaskPane> createPropertyTaskPanes() {
        final FilterDescriptor<?, ?> descriptor = getComponentBuilder().getDescriptor();
        final List<ConfiguredPropertyDescriptor> configuredProperties = new ArrayList<ConfiguredPropertyDescriptor>(
                new TreeSet<ConfiguredPropertyDescriptor>(descriptor.getConfiguredProperties()));

        // create a single task pane
        final List<ConfiguredPropertyTaskPane> result = new ArrayList<ConfiguredPropertyTaskPane>();
        result.add(new ConfiguredPropertyTaskPane("Required properties", IconUtils.MODEL_COLUMN, configuredProperties));

        return result;
    }
}
