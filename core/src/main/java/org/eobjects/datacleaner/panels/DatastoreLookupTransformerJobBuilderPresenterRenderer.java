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
