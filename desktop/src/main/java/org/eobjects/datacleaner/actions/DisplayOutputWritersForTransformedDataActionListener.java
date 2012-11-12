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
package org.eobjects.datacleaner.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JComponent;

import org.eobjects.analyzer.beans.api.Analyzer;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.AnalyzerJobBuilder;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.eobjects.analyzer.lifecycle.LifeCycleHelper;
import org.eobjects.datacleaner.output.beans.AbstractOutputWriterAnalyzer;

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
		Analyzer<?> analyzer = analyzerJobBuilder.getConfigurableBean();
		if (analyzer instanceof AbstractOutputWriterAnalyzer) {
		    LifeCycleHelper helper = new LifeCycleHelper(analysisJobBuilder.getConfiguration().getInjectionManager(null), null);
	        helper.assignProvidedProperties(analyzerJobBuilder.getDescriptor(), analyzer);
			((AbstractOutputWriterAnalyzer) analyzer).configureForTransformedData(analysisJobBuilder,
					_transformerJobBuilder.getDescriptor());
		}
		List<InputColumn<?>> inputColumns = _transformerJobBuilder.getInputColumns();
		List<MutableInputColumn<?>> outputColumns = _transformerJobBuilder.getOutputColumns();
		analyzerJobBuilder.clearInputColumns();
		analyzerJobBuilder.addInputColumns(inputColumns);
		analyzerJobBuilder.addInputColumns(outputColumns);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JComponent component = (JComponent) e.getSource();
		showPopup(component);
	}

}
