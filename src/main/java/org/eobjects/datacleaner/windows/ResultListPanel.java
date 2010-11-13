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
package org.eobjects.datacleaner.windows;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JTextArea;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.job.FilterJob;
import org.eobjects.analyzer.job.FilterOutcome;
import org.eobjects.analyzer.job.MergeInput;
import org.eobjects.analyzer.job.MergedOutcome;
import org.eobjects.analyzer.job.MergedOutcomeJob;
import org.eobjects.analyzer.job.Outcome;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.renderer.Renderer;
import org.eobjects.analyzer.result.renderer.RendererFactory;
import org.eobjects.analyzer.result.renderer.SwingRenderingFormat;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.builder.WidgetFactory;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

public class ResultListPanel extends DCPanel {

	private static final long serialVersionUID = 1L;

	private final RendererFactory _rendererFactory;
	private final JXTaskPaneContainer _taskPaneContainer;

	public ResultListPanel(RendererFactory rendererFactory) {
		super(WidgetUtils.BG_COLOR_BRIGHT, WidgetUtils.BG_COLOR_BRIGHTEST);
		_rendererFactory = rendererFactory;
		setLayout(new BorderLayout());
		_taskPaneContainer = WidgetFactory.createTaskPaneContainer();
		add(WidgetUtils.scrolleable(_taskPaneContainer), BorderLayout.CENTER);
	}

	public void addResult(AnalyzerJob analyzerJob, AnalyzerResult result) {
		Renderer<? super AnalyzerResult, ? extends JComponent> renderer = _rendererFactory.getRenderer(result,
				SwingRenderingFormat.class);
		JComponent component;
		if (renderer == null) {
			component = new JTextArea(result.toString());
		} else {
			component = renderer.render(result);
		}
		JXTaskPane taskPane = new JXTaskPane();

		StringBuilder sb = new StringBuilder();
		sb.append(analyzerJob.getDescriptor().getDisplayName());

		InputColumn<?>[] input = analyzerJob.getInput();
		if (input.length > 0) {
			sb.append(" (");
			if (input.length < 4) {
				for (int i = 0; i < input.length; i++) {
					if (i != 0) {
						sb.append(',');
					}
					sb.append(input[i].getName());
				}
			} else {
				sb.append(input.length);
				sb.append(" columns");
			}
			sb.append(")");
		}

		Outcome req = analyzerJob.getRequirement();
		if (req != null) {
			sb.append(" (");
			appendRequirement(sb, req);
			sb.append(")");
		}

		taskPane.setTitle(sb.toString());
		taskPane.add(component);

		synchronized (this) {
			_taskPaneContainer.add(taskPane);
		}
	}

	private void appendRequirement(StringBuilder sb, Outcome req) {
		if (req instanceof FilterOutcome) {
			FilterJob filterJob = ((FilterOutcome) req).getFilterJob();
			Enum<?> category = ((FilterOutcome) req).getCategory();

			sb.append(filterJob.getDescriptor().getDisplayName());
			sb.append("=");
			sb.append(category);
		} else if (req instanceof MergedOutcome) {
			sb.append('[');
			MergedOutcomeJob mergedOutcomeJob = ((MergedOutcome) req).getMergedOutcomeJob();

			MergeInput[] mergeInputs = mergedOutcomeJob.getMergeInputs();
			for (int i = 0; i < mergeInputs.length; i++) {
				if (i != 0) {
					sb.append(',');
				}
				MergeInput mergeInput = mergeInputs[i];
				Outcome outcome = mergeInput.getOutcome();
				appendRequirement(sb, outcome);
			}
			sb.append(']');
		} else {
			// should not happen
			sb.append(req.toString());
		}
	}
}
