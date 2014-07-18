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
package org.eobjects.datacleaner.panels;

import java.awt.Image;

import org.eobjects.analyzer.beans.api.Analyzer;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.job.builder.AnalyzerJobBuilder;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.widgets.properties.MultipleInputColumnsPropertyWidget;
import org.eobjects.datacleaner.widgets.properties.PropertyWidget;
import org.eobjects.datacleaner.widgets.properties.PropertyWidgetFactory;

/**
 * Specialization of {@link AbstractJobBuilderPanel} for {@link Analyzer}s.
 */
public class AnalyzerJobBuilderPanel extends AbstractJobBuilderPanel implements AnalyzerJobBuilderPresenter {

    private static final long serialVersionUID = 1L;

    private static final ImageManager imageManager = ImageManager.get();
    private static final Image WATERMARK_IMAGE = imageManager.getImage("images/window/analyzer-tab-background.png");

    private final AnalyzerJobBuilder<?> _analyzerJobBuilder;

    public AnalyzerJobBuilderPanel(AnalyzerJobBuilder<?> analyzerJobBuilder, PropertyWidgetFactory propertyWidgetFactory) {
        this(analyzerJobBuilder, true, propertyWidgetFactory);
    }

    public AnalyzerJobBuilderPanel(AnalyzerJobBuilder<?> analyzerJobBuilder, boolean displayRequirementButton,
            PropertyWidgetFactory propertyWidgetFactory) {
        this(WATERMARK_IMAGE, 95, 95, analyzerJobBuilder, displayRequirementButton, propertyWidgetFactory);
    }

    public AnalyzerJobBuilderPanel(Image watermarkImage, int watermarkHorizontalPosition,
            int watermarkVerticalPosition, AnalyzerJobBuilder<?> analyzerJobBuilder, boolean displayRequirementButton,
            PropertyWidgetFactory propertyWidgetFactory) {
        super(watermarkImage, watermarkHorizontalPosition, watermarkVerticalPosition, analyzerJobBuilder,
                propertyWidgetFactory, displayRequirementButton);
        _analyzerJobBuilder = analyzerJobBuilder;
    }

    @Override
    protected PropertyWidget<?> createPropertyWidget(AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
            ConfiguredPropertyDescriptor propertyDescriptor) {
        if (_analyzerJobBuilder.isMultipleJobsSupported()) {
            if (_analyzerJobBuilder.isMultipleJobsDeterminedBy(propertyDescriptor)) {
                MultipleInputColumnsPropertyWidget propertyWidget = new MultipleInputColumnsPropertyWidget(
                        beanJobBuilder, propertyDescriptor);
                return propertyWidget;
            }
        }
        return super.createPropertyWidget(beanJobBuilder, propertyDescriptor);
    }

    @Override
    public AnalyzerJobBuilder<?> getJobBuilder() {
        return _analyzerJobBuilder;
    }
}
