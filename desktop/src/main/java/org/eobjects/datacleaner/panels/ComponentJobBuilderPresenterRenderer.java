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
package org.eobjects.datacleaner.panels;

import javax.inject.Inject;

import org.eobjects.analyzer.beans.api.Provided;
import org.eobjects.analyzer.beans.api.Renderer;
import org.eobjects.analyzer.beans.api.RendererBean;
import org.eobjects.analyzer.beans.api.RendererPrecedence;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.AnalyzerJobBuilder;
import org.eobjects.analyzer.job.builder.FilterJobBuilder;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.guice.InjectorBuilder;
import org.eobjects.datacleaner.widgets.properties.PropertyWidgetFactory;

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
