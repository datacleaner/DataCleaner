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

import org.datacleaner.beans.api.Analyzer;
import org.datacleaner.data.InputColumn;
import org.datacleaner.data.MutableInputColumn;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerJobBuilder;
import org.datacleaner.job.builder.TransformerJobBuilder;
import org.datacleaner.lifecycle.LifeCycleHelper;
import org.datacleaner.extension.output.AbstractOutputWriterAnalyzer;

/**
 * Action that displays output writers for a transformer's data.
 * 
 * @author Kasper Sørensen
 */
public class DisplayOutputWritersForTransformedDataActionListener extends DisplayOutputWritersAction implements
		ActionListener {

	private final TransformerJobBuilder<?> _transformerJobBuilder;

	public DisplayOutputWritersForTransformedDataActionListener(TransformerJobBuilder<?> transformerJobBuilder) {
		super(transformerJobBuilder.getAnalysisJobBuilder());
		_transformerJobBuilder = transformerJobBuilder;
	}

	@Override
	protected void configure(AnalysisJobBuilder analysisJobBuilder, AnalyzerJobBuilder<?> analyzerJobBuilder) {
		Analyzer<?> analyzer = analyzerJobBuilder.getComponentInstance();
		if (analyzer instanceof AbstractOutputWriterAnalyzer) {
		    LifeCycleHelper helper = new LifeCycleHelper(analysisJobBuilder.getConfiguration().getInjectionManager(null), null, true);
	        helper.assignProvidedProperties(analyzerJobBuilder.getDescriptor(), analyzer);
			((AbstractOutputWriterAnalyzer) analyzer).configureForTransformedData(analysisJobBuilder,
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
