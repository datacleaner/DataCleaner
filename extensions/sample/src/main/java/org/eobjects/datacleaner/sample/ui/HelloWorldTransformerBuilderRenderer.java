/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.sample.ui;

import javax.inject.Inject;

import org.eobjects.analyzer.beans.api.Renderer;
import org.eobjects.analyzer.beans.api.RendererBean;
import org.eobjects.analyzer.beans.api.RendererPrecedence;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.guice.InjectorBuilder;
import org.eobjects.datacleaner.panels.ComponentJobBuilderRenderingFormat;
import org.eobjects.datacleaner.panels.TransformerJobBuilderPresenter;
import org.eobjects.datacleaner.sample.HelloWorldTransformer;
import org.eobjects.datacleaner.widgets.properties.PropertyWidgetFactory;

/**
 * A sample renderer which provides a presenter object for the configuration
 * panel of a transformer.
 * 
 * Renderers like this are optional, but allows for absolute control over the
 * User Interface which may be useful for certain types of extensions.
 * 
 * @author Kasper Sørensen
 */
@RendererBean(ComponentJobBuilderRenderingFormat.class)
public class HelloWorldTransformerBuilderRenderer implements
		Renderer<TransformerJobBuilder<?>, TransformerJobBuilderPresenter> {

	@Inject
	WindowContext windowContext;

	@Inject
	InjectorBuilder injectorBuilder;

	@Inject
	AnalyzerBeansConfiguration configuration;

	@Override
	public RendererPrecedence getPrecedence(TransformerJobBuilder<?> renderable) {
		if (renderable.getDescriptor().getComponentClass() == HelloWorldTransformer.class) {
			return RendererPrecedence.HIGHEST;
		}
		return RendererPrecedence.NOT_CAPABLE;
	}

	@Override
	public TransformerJobBuilderPresenter render(TransformerJobBuilder<?> tjb) {
		PropertyWidgetFactory propertyWidgetFactory = injectorBuilder.with(
				PropertyWidgetFactory.TYPELITERAL_BEAN_JOB_BUILDER, tjb).getInstance(PropertyWidgetFactory.class);
		return new HelloWorldTransformerPresenter(tjb, windowContext, propertyWidgetFactory, configuration);
	}
}
