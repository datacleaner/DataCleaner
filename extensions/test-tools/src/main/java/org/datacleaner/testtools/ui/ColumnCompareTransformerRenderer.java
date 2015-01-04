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
package org.datacleaner.testtools.ui;

import javax.inject.Inject;

import org.datacleaner.beans.api.Renderer;
import org.datacleaner.beans.api.RendererBean;
import org.datacleaner.beans.api.RendererPrecedence;
import org.datacleaner.configuration.AnalyzerBeansConfiguration;
import org.datacleaner.job.builder.TransformerJobBuilder;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.guice.InjectorBuilder;
import org.datacleaner.panels.ComponentJobBuilderRenderingFormat;
import org.datacleaner.panels.TransformerJobBuilderPresenter;
import org.datacleaner.testtools.ColumnCompareTransformer;
import org.datacleaner.widgets.properties.PropertyWidgetFactory;

@RendererBean(ComponentJobBuilderRenderingFormat.class)
public class ColumnCompareTransformerRenderer implements
        Renderer<TransformerJobBuilder<?>, TransformerJobBuilderPresenter> {

    @Inject
    WindowContext windowContext;

    @Inject
    InjectorBuilder injectorBuilder;

    @Inject
    AnalyzerBeansConfiguration configuration;

    @Override
    public TransformerJobBuilderPresenter render(TransformerJobBuilder<?> tjb) {
        final PropertyWidgetFactory propertyWidgetFactory = injectorBuilder
                .with(PropertyWidgetFactory.TYPELITERAL_BEAN_JOB_BUILDER, tjb)
                .getInstance(PropertyWidgetFactory.class);
        return new ColumnCompareTransformerPresenter(tjb, windowContext,
                propertyWidgetFactory, configuration);
    }

    @Override
    public RendererPrecedence getPrecedence(TransformerJobBuilder<?> tjb) {
        Class<?> componentClass = tjb.getDescriptor()
                .getComponentClass();
        if (componentClass == ColumnCompareTransformer.class) {
            return RendererPrecedence.HIGH;
        }
        return RendererPrecedence.NOT_CAPABLE;
    }

}
