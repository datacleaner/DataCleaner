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
import org.datacleaner.widgets.properties.PropertyWidgetFactory;

/**
 * Specialization of {@link AbstractComponentBuilderPanel} for {@link Analyzer}s.
 */
public class AnalyzerComponentBuilderPanel extends AbstractComponentBuilderPanel implements AnalyzerComponentBuilderPresenter,
        AnalyzerChangeListener {

    private static final long serialVersionUID = 1L;

    private final AnalyzerComponentBuilder<?> _analyzerComponentBuilder;

    public AnalyzerComponentBuilderPanel(AnalyzerComponentBuilder<?> analyzerJobBuilder, PropertyWidgetFactory propertyWidgetFactory) {
        this(null, 95, 95, analyzerJobBuilder, propertyWidgetFactory);
    }

    public AnalyzerComponentBuilderPanel(Image watermarkImage, int watermarkHorizontalPosition,
            int watermarkVerticalPosition, AnalyzerComponentBuilder<?> analyzerComponentBuilder,
            PropertyWidgetFactory propertyWidgetFactory) {
        super(watermarkImage, watermarkHorizontalPosition, watermarkVerticalPosition, analyzerComponentBuilder,
                propertyWidgetFactory);
        _analyzerComponentBuilder = analyzerComponentBuilder;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        _analyzerComponentBuilder.addChangeListener(this);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        _analyzerComponentBuilder.removeChangeListener(this);
    }

    @Override
    public AnalyzerComponentBuilder<?> getComponentBuilder() {
        return _analyzerComponentBuilder;
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
