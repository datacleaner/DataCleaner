package org.eobjects.datacleaner.testtools.ui;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.panels.TransformerJobBuilderPanel;
import org.eobjects.datacleaner.panels.TransformerJobBuilderPresenter;
import org.eobjects.datacleaner.widgets.properties.MultipleMappedInputColumnsPropertyWidget;
import org.eobjects.datacleaner.widgets.properties.PropertyWidget;
import org.eobjects.datacleaner.widgets.properties.PropertyWidgetFactory;


public class ColumnCompareTransformerPresenter extends TransformerJobBuilderPanel implements TransformerJobBuilderPresenter {

    private static final long serialVersionUID = 1L;

    private final ConfiguredPropertyDescriptor _testColumnsProperty;
    private final ConfiguredPropertyDescriptor _referenceColumnsProperty;

    private final MultipleMappedInputColumnsPropertyWidget
        _testColumnsPropertyWidget;

    public ColumnCompareTransformerPresenter(
            TransformerJobBuilder<?> transformerJobBuilder,
            WindowContext windowContext,
            PropertyWidgetFactory propertyWidgetFactory,
            AnalyzerBeansConfiguration configuration) {
        super(transformerJobBuilder, windowContext, propertyWidgetFactory,
                configuration);

        _testColumnsProperty = transformerJobBuilder.getDescriptor()
                .getConfiguredProperty("Test columns");
        _referenceColumnsProperty = transformerJobBuilder.getDescriptor()
                .getConfiguredProperty("Reference columns");

        _testColumnsPropertyWidget = new MultipleMappedInputColumnsPropertyWidget(
                transformerJobBuilder, _testColumnsProperty,
                _referenceColumnsProperty);
    }

    @Override
    protected PropertyWidget<?> createPropertyWidget(
            AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
            ConfiguredPropertyDescriptor propertyDescriptor) {
        if (propertyDescriptor == _testColumnsProperty) {
            return _testColumnsPropertyWidget;
        } else if (propertyDescriptor == _referenceColumnsProperty) {
            return _testColumnsPropertyWidget
                    .getMappedInputColumnsPropertyWidget();
        } else {
            return super.createPropertyWidget(beanJobBuilder,
                    propertyDescriptor);
        }
    }
}
