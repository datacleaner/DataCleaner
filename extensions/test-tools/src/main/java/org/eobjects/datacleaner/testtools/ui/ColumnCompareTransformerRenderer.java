package org.eobjects.datacleaner.testtools.ui;

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
import org.eobjects.datacleaner.testtools.ColumnCompareTransformer;
import org.eobjects.datacleaner.widgets.properties.PropertyWidgetFactory;

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
