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

import org.datacleaner.beans.api.Provided;
import org.datacleaner.beans.api.Renderer;
import org.datacleaner.beans.api.RendererBean;
import org.datacleaner.beans.api.RendererPrecedence;
import org.datacleaner.configuration.AnalyzerBeansConfiguration;
import org.datacleaner.job.builder.AbstractBeanJobBuilder;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerJobBuilder;
import org.datacleaner.job.builder.FilterJobBuilder;
import org.datacleaner.job.builder.TransformerJobBuilder;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.guice.InjectorBuilder;
import org.datacleaner.widgets.properties.PropertyWidgetFactory;

/**
 * Renders/creates the default panels that present component job builders.
 */
@RendererBean(ComponentJobBuilderRenderingFormat.class)
public class ComponentJobBuilderPresenterRenderer implements
        Renderer<AbstractBeanJobBuilder<?, ?, ?>, ComponentJobBuilderPresenter> {

    @Inject
    @Provided
    WindowContext windowContext;

    @Inject
    @Provided
    AnalyzerBeansConfiguration configuration;

    @Inject
    @Provided
    InjectorBuilder injectorBuilder;

    @Override
    public RendererPrecedence getPrecedence(AbstractBeanJobBuilder<?, ?, ?> renderable) {
        return RendererPrecedence.LOW;
    }

    @Override
    public ComponentJobBuilderPresenter render(AbstractBeanJobBuilder<?, ?, ?> renderable) {
        final AnalysisJobBuilder analysisJobBuilder = renderable.getAnalysisJobBuilder();

        final PropertyWidgetFactory propertyWidgetFactory = injectorBuilder
                .with(PropertyWidgetFactory.TYPELITERAL_BEAN_JOB_BUILDER, renderable)
                .with(AnalysisJobBuilder.class, analysisJobBuilder).getInstance(PropertyWidgetFactory.class);

        if (renderable instanceof FilterJobBuilder) {
            FilterJobBuilder<?, ?> fjb = (FilterJobBuilder<?, ?>) renderable;
            return new FilterJobBuilderPanel(fjb, windowContext, propertyWidgetFactory);
        } else if (renderable instanceof TransformerJobBuilder) {
            TransformerJobBuilder<?> tjb = (TransformerJobBuilder<?>) renderable;
            return new TransformerJobBuilderPanel(tjb, windowContext, propertyWidgetFactory, configuration);
        } else if (renderable instanceof AnalyzerJobBuilder) {
            AnalyzerJobBuilder<?> ajb = (AnalyzerJobBuilder<?>) renderable;
            return new AnalyzerJobBuilderPanel(ajb, propertyWidgetFactory);
        }
        throw new UnsupportedOperationException();
    }

}
