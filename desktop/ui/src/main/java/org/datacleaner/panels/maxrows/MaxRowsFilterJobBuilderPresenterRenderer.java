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
package org.datacleaner.panels.maxrows;

import javax.inject.Inject;

import org.datacleaner.api.Renderer;
import org.datacleaner.api.RendererBean;
import org.datacleaner.api.RendererPrecedence;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.components.maxrows.MaxRowsFilter;
import org.datacleaner.configuration.AnalyzerBeansConfiguration;
import org.datacleaner.guice.DCModule;
import org.datacleaner.job.builder.FilterJobBuilder;
import org.datacleaner.panels.ComponentJobBuilderRenderingFormat;
import org.datacleaner.panels.FilterJobBuilderPresenter;
import org.datacleaner.widgets.properties.PropertyWidgetFactory;

/**
 * Specialized {@link Renderer} for a {@link FilterJobBuilderPresenter} for
 * {@link MaxRowsFilter} when toggled using the
 * {@link MaxRowsFilterShortcutPanel}.
 * 
 * @author Kasper Sørensen
 */
@RendererBean(ComponentJobBuilderRenderingFormat.class)
public class MaxRowsFilterJobBuilderPresenterRenderer implements
        Renderer<FilterJobBuilder<MaxRowsFilter, MaxRowsFilter.Category>, FilterJobBuilderPresenter> {

    @Inject
    WindowContext windowContext;

    @Inject
    AnalyzerBeansConfiguration configuration;

    @Inject
    DCModule dcModule;

    @Override
    public RendererPrecedence getPrecedence(FilterJobBuilder<MaxRowsFilter, MaxRowsFilter.Category> fjb) {
        if (MaxRowsFilterShortcutPanel.isFilter(fjb)) {
            return RendererPrecedence.HIGHEST;
        }
        return RendererPrecedence.NOT_CAPABLE;
    }

    @Override
    public FilterJobBuilderPresenter render(FilterJobBuilder<MaxRowsFilter, MaxRowsFilter.Category> fjb) {
        final PropertyWidgetFactory propertyWidgetFactory = dcModule.createChildInjectorForComponent(fjb).getInstance(PropertyWidgetFactory.class);

        return new MaxRowsFilterShortcutJobBuilderPanel(fjb, windowContext, propertyWidgetFactory);
    }

}
