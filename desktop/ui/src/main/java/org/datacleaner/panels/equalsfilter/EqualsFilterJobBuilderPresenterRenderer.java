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

import javax.inject.Inject;

import org.datacleaner.api.Renderer;
import org.datacleaner.api.RendererBean;
import org.datacleaner.api.RendererPrecedence;
import org.datacleaner.beans.filter.EqualsFilter;
import org.datacleaner.beans.filter.ValidationCategory;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.guice.DCModule;
import org.datacleaner.job.builder.FilterJobBuilder;
import org.datacleaner.panels.ComponentJobBuilderRenderingFormat;
import org.datacleaner.panels.FilterJobBuilderPresenter;
import org.datacleaner.widgets.properties.PropertyWidgetFactory;

/**
 * Specialized {@link Renderer} for a {@link FilterJobBuilderPresenter} for
 * {@link EqualsFilter}.
 */
@RendererBean(ComponentJobBuilderRenderingFormat.class)
public class EqualsFilterJobBuilderPresenterRenderer implements
        Renderer<FilterJobBuilder<EqualsFilter, ValidationCategory>, FilterJobBuilderPresenter> {

    @Inject
    WindowContext windowContext;

    @Inject
    DCModule dcModule;

    @Override
    public RendererPrecedence getPrecedence(FilterJobBuilder<EqualsFilter, ValidationCategory> fjb) {
        if (fjb.getDescriptor().getComponentClass() == EqualsFilter.class) {
            return RendererPrecedence.HIGH;
        }
        return RendererPrecedence.NOT_CAPABLE;
    }

    @Override
    public FilterJobBuilderPresenter render(FilterJobBuilder<EqualsFilter, ValidationCategory> fjb) {
        final PropertyWidgetFactory propertyWidgetFactory = dcModule.createChildInjectorForComponent(fjb).getInstance(
                PropertyWidgetFactory.class);

        return new EqualsFilterJobBuilderPresenter(fjb, windowContext, propertyWidgetFactory);
    }

}
