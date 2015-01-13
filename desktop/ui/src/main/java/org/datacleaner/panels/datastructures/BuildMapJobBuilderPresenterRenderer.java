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
package org.datacleaner.panels.datastructures;

import javax.inject.Inject;

import org.datacleaner.api.Renderer;
import org.datacleaner.api.RendererBean;
import org.datacleaner.api.RendererPrecedence;
import org.datacleaner.beans.datastructures.BuildMapTransformer;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.configuration.AnalyzerBeansConfiguration;
import org.datacleaner.guice.DCModule;
import org.datacleaner.job.builder.TransformerJobBuilder;
import org.datacleaner.panels.ComponentJobBuilderRenderingFormat;
import org.datacleaner.panels.TransformerJobBuilderPresenter;
import org.datacleaner.widgets.properties.PropertyWidgetFactory;

/**
 * Specialized {@link Renderer} for a {@link TransformerJobBuilder} for the
 * {@link BuildMapTransformer}.
 */
@RendererBean(ComponentJobBuilderRenderingFormat.class)
public class BuildMapJobBuilderPresenterRenderer implements
        Renderer<TransformerJobBuilder<BuildMapTransformer>, TransformerJobBuilderPresenter> {

    @Inject
    WindowContext windowContext;

    @Inject
    AnalyzerBeansConfiguration configuration;

    @Inject
    DCModule dcModule;

    @Override
    public RendererPrecedence getPrecedence(TransformerJobBuilder<BuildMapTransformer> tjb) {
        if (tjb.getDescriptor().getComponentClass() == BuildMapTransformer.class) {
            return RendererPrecedence.HIGH;
        }
        return RendererPrecedence.NOT_CAPABLE;
    }

    @Override
    public TransformerJobBuilderPresenter render(TransformerJobBuilder<BuildMapTransformer> tjb) {
        final PropertyWidgetFactory propertyWidgetFactory = dcModule.createChildInjectorForComponent(tjb).getInstance(
                PropertyWidgetFactory.class);

        return new BuildMapJobBuilderPresenter(tjb, windowContext, propertyWidgetFactory, configuration);
    }

}
