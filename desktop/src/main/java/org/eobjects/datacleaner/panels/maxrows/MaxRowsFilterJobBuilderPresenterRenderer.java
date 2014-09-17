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
package org.eobjects.datacleaner.panels.maxrows;

import javax.inject.Inject;

import org.eobjects.analyzer.beans.api.Renderer;
import org.eobjects.analyzer.beans.api.RendererBean;
import org.eobjects.analyzer.beans.api.RendererPrecedence;
import org.eobjects.analyzer.beans.filter.MaxRowsFilter;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.job.builder.FilterJobBuilder;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.guice.InjectorBuilder;
import org.eobjects.datacleaner.panels.ComponentJobBuilderRenderingFormat;
import org.eobjects.datacleaner.panels.FilterJobBuilderPresenter;
import org.eobjects.datacleaner.widgets.properties.PropertyWidgetFactory;

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
    InjectorBuilder injectorBuilder;

    @Override
    public RendererPrecedence getPrecedence(FilterJobBuilder<MaxRowsFilter, MaxRowsFilter.Category> fjb) {
        if (MaxRowsFilterShortcutPanel.isFilter(fjb)) {
            return RendererPrecedence.HIGHEST;
        }
        return RendererPrecedence.NOT_CAPABLE;
    }

    @Override
    public FilterJobBuilderPresenter render(FilterJobBuilder<MaxRowsFilter, MaxRowsFilter.Category> fjb) {
        final PropertyWidgetFactory propertyWidgetFactory = injectorBuilder.with(
                PropertyWidgetFactory.TYPELITERAL_BEAN_JOB_BUILDER, fjb).getInstance(PropertyWidgetFactory.class);

        return new MaxRowsFilterShortcutJobBuilderPanel(fjb, windowContext, propertyWidgetFactory);
    }

}
