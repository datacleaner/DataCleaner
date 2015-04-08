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
package org.datacleaner.panels;

import javax.inject.Inject;

import org.datacleaner.api.Provided;
import org.datacleaner.api.Renderer;
import org.datacleaner.api.RendererBean;
import org.datacleaner.api.RendererPrecedence;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.guice.DCModule;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.job.builder.FilterComponentBuilder;
import org.datacleaner.job.builder.TransformerComponentBuilder;
import org.datacleaner.widgets.properties.PropertyWidgetFactory;

/**
 * Renders/creates the default panels that present component job builders.
 */
@RendererBean(ComponentBuilderPresenterRenderingFormat.class)
public class ComponentJobBuilderPresenterRenderer implements
        Renderer<ComponentBuilder, ComponentBuilderPresenter> {

    @Inject
    @Provided
    WindowContext windowContext;

    @Inject
    @Provided
    DataCleanerConfiguration configuration;

    @Inject
    @Provided
    DCModule dcModule;

    @Override
    public RendererPrecedence getPrecedence(ComponentBuilder renderable) {
        return RendererPrecedence.LOW;
    }

    @Override
    public ComponentBuilderPresenter render(ComponentBuilder renderable) {
        final PropertyWidgetFactory propertyWidgetFactory = dcModule.createChildInjectorForComponent(renderable)
                .getInstance(PropertyWidgetFactory.class);

        if (renderable instanceof FilterComponentBuilder) {
            FilterComponentBuilder<?, ?> fjb = (FilterComponentBuilder<?, ?>) renderable;
            return new FilterComponentBuilderPanel(fjb, windowContext, propertyWidgetFactory);
        } else if (renderable instanceof TransformerComponentBuilder) {
            TransformerComponentBuilder<?> tjb = (TransformerComponentBuilder<?>) renderable;
            return new TransformerComponentBuilderPanel(tjb, windowContext, propertyWidgetFactory, configuration);
        } else if (renderable instanceof AnalyzerComponentBuilder) {
            AnalyzerComponentBuilder<?> ajb = (AnalyzerComponentBuilder<?>) renderable;
            return new AnalyzerComponentBuilderPanel(ajb, propertyWidgetFactory);
        }
        throw new UnsupportedOperationException();
    }

}
