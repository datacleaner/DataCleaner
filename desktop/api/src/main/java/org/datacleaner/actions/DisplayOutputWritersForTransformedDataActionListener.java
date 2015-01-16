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
package org.datacleaner.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JComponent;

import org.datacleaner.api.Analyzer;
import org.datacleaner.api.InputColumn;
import org.datacleaner.data.MutableInputColumn;
import org.datacleaner.desktop.api.PrecedingComponentConsumer;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.job.builder.TransformerComponentBuilder;
import org.datacleaner.lifecycle.LifeCycleHelper;

/**
 * Action that displays output writers for a transformer's data.
 */
public class DisplayOutputWritersForTransformedDataActionListener extends DisplayOutputWritersAction implements
        ActionListener {

    private final TransformerComponentBuilder<?> _transformerJobBuilder;

    public DisplayOutputWritersForTransformedDataActionListener(TransformerComponentBuilder<?> transformerJobBuilder) {
        super(transformerJobBuilder.getAnalysisJobBuilder());
        _transformerJobBuilder = transformerJobBuilder;
    }

    @Override
    protected void configure(AnalysisJobBuilder analysisJobBuilder, AnalyzerComponentBuilder<?> analyzerJobBuilder) {
        Analyzer<?> analyzer = analyzerJobBuilder.getComponentInstance();
        if (analyzer instanceof PrecedingComponentConsumer) {
            LifeCycleHelper helper = new LifeCycleHelper(analysisJobBuilder.getConfiguration()
                    .getInjectionManager(null), null, true);
            helper.assignProvidedProperties(analyzerJobBuilder.getDescriptor(), analyzer);
            ((PrecedingComponentConsumer) analyzer).configureForTransformedData(analysisJobBuilder,
                    _transformerJobBuilder.getDescriptor());
        }

        if (analyzerJobBuilder.getDescriptor().getConfiguredPropertiesForInput().size() == 1) {
            List<InputColumn<?>> inputColumns = _transformerJobBuilder.getInputColumns();
            List<MutableInputColumn<?>> outputColumns = _transformerJobBuilder.getOutputColumns();
            analyzerJobBuilder.clearInputColumns();
            analyzerJobBuilder.addInputColumns(inputColumns);
            analyzerJobBuilder.addInputColumns(outputColumns);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JComponent component = (JComponent) e.getSource();
        showPopup(component);
    }

}
