/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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

import org.eobjects.analyzer.beans.api.Renderer;
import org.eobjects.analyzer.beans.api.RendererBean;
import org.eobjects.analyzer.beans.api.RendererPrecedence;
import org.eobjects.analyzer.beans.transform.DatastoreLookupTransformer;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.guice.InjectorBuilder;
import org.eobjects.datacleaner.widgets.properties.PropertyWidgetFactory;

/**
 * Specialized {@link Renderer} for a {@link TransformerJobBuilderPresenter} for
 * {@link DatastoreLookupTransformer}.
 * 
 * @author Kasper Sørensen
 */
@RendererBean(ComponentJobBuilderRenderingFormat.class)
public class DatastoreLookupTransformerJobBuilderPresenterRenderer implements
		Renderer<TransformerJobBuilder<DatastoreLookupTransformer>, TransformerJobBuilderPresenter> {

	@Inject
	WindowContext windowContext;

	@Inject
	AnalyzerBeansConfiguration configuration;

	@Inject
	InjectorBuilder injectorBuilder;

	@Override
	public RendererPrecedence getPrecedence(TransformerJobBuilder<DatastoreLookupTransformer> tjb) {
		if (tjb.getDescriptor().getComponentClass() == DatastoreLookupTransformer.class) {
			return RendererPrecedence.HIGH;
		}
		return RendererPrecedence.NOT_CAPABLE;
	}

	@Override
	public TransformerJobBuilderPresenter render(TransformerJobBuilder<DatastoreLookupTransformer> tjb) {
		final PropertyWidgetFactory propertyWidgetFactory = injectorBuilder.with(
				PropertyWidgetFactory.TYPELITERAL_BEAN_JOB_BUILDER, tjb).getInstance(PropertyWidgetFactory.class);

		return new DatastoreLookupTransformerJobBuilderPresenter(tjb, windowContext, propertyWidgetFactory, configuration);
	}

}
