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
package org.datacleaner.panels;

import java.awt.Image;

import org.datacleaner.api.Analyzer;
import org.datacleaner.job.builder.AnalyzerChangeListener;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.util.ImageManager;
import org.datacleaner.widgets.properties.PropertyWidgetFactory;

/**
 * Specialization of {@link AbstractComponentBuilderPanel} for {@link Analyzer}s.
 */
public class AnalyzerComponentBuilderPanel extends AbstractComponentBuilderPanel implements AnalyzerComponentBuilderPresenter,
        AnalyzerChangeListener {

    private static final long serialVersionUID = 1L;

    private static final ImageManager imageManager = ImageManager.get();
    private static final Image WATERMARK_IMAGE = imageManager.getImage("images/window/analyzer-tab-background.png");

    private final AnalyzerComponentBuilder<?> _analyzerJobBuilder;

    public AnalyzerComponentBuilderPanel(AnalyzerComponentBuilder<?> analyzerJobBuilder, PropertyWidgetFactory propertyWidgetFactory) {
        this(WATERMARK_IMAGE, 95, 95, analyzerJobBuilder, propertyWidgetFactory);
    }

    public AnalyzerComponentBuilderPanel(Image watermarkImage, int watermarkHorizontalPosition,
            int watermarkVerticalPosition, AnalyzerComponentBuilder<?> analyzerJobBuilder,
            PropertyWidgetFactory propertyWidgetFactory) {
        super(watermarkImage, watermarkHorizontalPosition, watermarkVerticalPosition, analyzerJobBuilder,
                propertyWidgetFactory);
        _analyzerJobBuilder = analyzerJobBuilder;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        _analyzerJobBuilder.addChangeListener(this);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        _analyzerJobBuilder.removeChangeListener(this);
    }

    @Override
    public AnalyzerComponentBuilder<?> getComponentBuilder() {
        return _analyzerJobBuilder;
    }

    @Override
    public void onAdd(AnalyzerComponentBuilder<?> ajb) {
    }

    @Override
    public void onConfigurationChanged(AnalyzerComponentBuilder<?> ajb) {
        onConfigurationChanged();
    }

    @Override
    public void onRemove(AnalyzerComponentBuilder<?> ajb) {
    }

    @Override
    public void onRequirementChanged(AnalyzerComponentBuilder<?> ajb) {
    }
}
