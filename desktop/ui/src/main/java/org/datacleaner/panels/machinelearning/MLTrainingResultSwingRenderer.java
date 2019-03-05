/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Free Software Foundation, Inc.
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
package org.datacleaner.panels.machinelearning;

import javax.inject.Inject;
import javax.swing.JComponent;

import org.datacleaner.api.Provided;
import org.datacleaner.api.RendererBean;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.components.machinelearning.MLAnalyzerResult;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.result.Crosstab;
import org.datacleaner.result.CrosstabResult;
import org.datacleaner.result.renderer.AbstractRenderer;
import org.datacleaner.result.renderer.RendererFactory;
import org.datacleaner.result.renderer.SwingRenderingFormat;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.widgets.result.DefaultCrosstabResultSwingRenderer;
import org.jdesktop.swingx.VerticalLayout;

@RendererBean(SwingRenderingFormat.class)
public class MLTrainingResultSwingRenderer extends AbstractRenderer<MLAnalyzerResult, JComponent> {

    @Inject
    @Provided
    RendererFactory _rendererFactory;

    @Inject
    @Provided
    WindowContext _windowContext;

    @Override
    public JComponent render(MLAnalyzerResult result) {
        final DefaultCrosstabResultSwingRenderer crosstabRenderer =
                new DefaultCrosstabResultSwingRenderer(_windowContext, _rendererFactory);

        final DCPanel panel = new DCPanel();
        panel.setLayout(new VerticalLayout(WidgetUtils.BORDER_WIDE_WIDTH));
        
        if (result.getTrainedClassifier() != null) {
            panel.add(DCLabel.dark("MODEL TRAINED!"));
        }
        
        final Crosstab<Integer> trainedRecordsConfusionMatrix = result.getTrainedRecordsConfusionMatrix();
        if (trainedRecordsConfusionMatrix != null) {
            panel.add(DCLabel.dark("Confusion matrix (trained records)"));
            panel.add(crosstabRenderer.render(new CrosstabResult(trainedRecordsConfusionMatrix)));
        }

        final Crosstab<Integer> crossValidationConfusionMatrix = result.getCrossValidationConfusionMatrix();
        if (trainedRecordsConfusionMatrix != null) {
            panel.add(DCLabel.dark("Confusion matrix (cross-validation records)"));
            panel.add(crosstabRenderer.render(new CrosstabResult(crossValidationConfusionMatrix)));
        }
        return panel;
    }

}
