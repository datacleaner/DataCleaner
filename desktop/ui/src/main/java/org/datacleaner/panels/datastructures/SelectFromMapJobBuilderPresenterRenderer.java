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
import org.datacleaner.beans.datastructures.SelectFromMapTransformer;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.configuration.AnalyzerBeansConfiguration;
import org.datacleaner.guice.DCModule;
import org.datacleaner.job.builder.TransformerComponentBuilder;
import org.datacleaner.panels.ComponentBuilderPresenterRenderingFormat;
import org.datacleaner.panels.TransformerComponentBuilderPresenter;
import org.datacleaner.widgets.properties.PropertyWidgetFactory;

/**
 * Specialized {@link Renderer} for a {@link TransformerComponentBuilder} for the
 * {@link SelectFromMapTransformer}.
 * 
 * @author Kasper Sørensen
 */
@RendererBean(ComponentBuilderPresenterRenderingFormat.class)
public class SelectFromMapJobBuilderPresenterRenderer implements
        Renderer<TransformerComponentBuilder<SelectFromMapTransformer>, TransformerComponentBuilderPresenter> {

    @Inject
    WindowContext windowContext;

    @Inject
    AnalyzerBeansConfiguration configuration;

    @Inject
    DCModule dcModule;

    @Override
    public RendererPrecedence getPrecedence(TransformerComponentBuilder<SelectFromMapTransformer> tjb) {
        if (tjb.getDescriptor().getComponentClass() == SelectFromMapTransformer.class) {
            return RendererPrecedence.HIGH;
        }
        return RendererPrecedence.NOT_CAPABLE;
    }

    @Override
    public TransformerComponentBuilderPresenter render(TransformerComponentBuilder<SelectFromMapTransformer> tjb) {
        final PropertyWidgetFactory propertyWidgetFactory = dcModule.createChildInjectorForComponent(tjb).getInstance(
                PropertyWidgetFactory.class);

        return new SelectFromMapJobBuilderPresenter(tjb, windowContext, propertyWidgetFactory, configuration);
    }

}
