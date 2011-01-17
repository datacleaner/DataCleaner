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
package org.eobjects.datacleaner.panels;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.Icon;

import org.apache.commons.collections15.Transformer;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.job.InputColumnSinkJob;
import org.eobjects.analyzer.job.InputColumnSourceJob;
import org.eobjects.analyzer.job.Outcome;
import org.eobjects.analyzer.job.OutcomeSinkJob;
import org.eobjects.analyzer.job.OutcomeSourceJob;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.AnalyzerJobBuilder;
import org.eobjects.analyzer.job.builder.FilterJobBuilder;
import org.eobjects.analyzer.job.builder.RowProcessingAnalyzerJobBuilder;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.eobjects.analyzer.util.SourceColumnFinder;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.LabelUtils;

import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;

public class VisualizeExecutionFlowPanel extends DCPanel {

	private static final long serialVersionUID = 1L;

	private int edgeNumber = 0;

	public VisualizeExecutionFlowPanel(AnalysisJobBuilder analysisJobBuilder) {
		super();
		final Graph<Object, Integer> g = new SparseMultigraph<Object, Integer>();

		final SourceColumnFinder scf = new SourceColumnFinder();
		scf.addSources(analysisJobBuilder);

		final List<TransformerJobBuilder<?>> tjbs = analysisJobBuilder.getTransformerJobBuilders();
		for (TransformerJobBuilder<?> tjb : tjbs) {
			addGraphNodes(g, scf, tjb);
		}

		final List<AnalyzerJobBuilder<?>> ajbs = analysisJobBuilder.getAnalyzerJobBuilders();
		for (AnalyzerJobBuilder<?> ajb : ajbs) {
			if (ajb instanceof RowProcessingAnalyzerJobBuilder) {
				RowProcessingAnalyzerJobBuilder<?> rpajb = (RowProcessingAnalyzerJobBuilder<?>) ajb;
				addGraphNodes(g, scf, rpajb);
			} else {
				// TODO: Add support for explorers
			}
		}

		List<FilterJobBuilder<?, ?>> fjbs = analysisJobBuilder.getFilterJobBuilders();
		for (FilterJobBuilder<?, ?> fjb : fjbs) {
			addGraphNodes(g, scf, fjb);
		}

		if (g.getVertexCount() == 0) {
			g.addVertex("No components in job");
		}

		final Layout<Object, Integer> layout = new ISOMLayout<Object, Integer>(g);

		final VisualizationViewer<Object, Integer> bvs = new VisualizationViewer<Object, Integer>(layout);

		final RenderContext<Object, Integer> renderContext = bvs.getRenderContext();

		final DefaultModalGraphMouse<Object, Integer> gm = new DefaultModalGraphMouse<Object, Integer>();
		gm.setMode(ModalGraphMouse.Mode.PICKING);
		bvs.setGraphMouse(gm);

		// render labels
		renderContext.setVertexLabelTransformer(new Transformer<Object, String>() {
			@Override
			public String transform(Object obj) {
				if (obj instanceof InputColumn) {
					return ((InputColumn<?>) obj).getName();
				}
				if (obj instanceof AbstractBeanJobBuilder) {
					return LabelUtils.getLabel((AbstractBeanJobBuilder<?, ?, ?>) obj);
				}
				return obj.toString();
			}
		});

		// render icons
		renderContext.setVertexIconTransformer(new Transformer<Object, Icon>() {

			@Override
			public Icon transform(Object obj) {
				if (obj instanceof InputColumn) {
					return ImageManager.getInstance().getImageIcon("images/model/column.png", IconUtils.ICON_SIZE_SMALL);
				}
				if (obj instanceof AbstractBeanJobBuilder) {
					return IconUtils.getDescriptorIcon(((AbstractBeanJobBuilder<?, ?, ?>) obj).getDescriptor());
				}
				return ImageManager.getInstance().getImageIcon("images/status/error.png");
			}
		});

		setLayout(new BorderLayout());
		add(bvs, BorderLayout.CENTER);
	}

	private void addGraphNodes(Graph<Object, Integer> g, SourceColumnFinder scf, Object item) {
		if (!g.containsVertex(item)) {
			g.addVertex(item);
			if (item instanceof InputColumnSinkJob) {
				InputColumn<?>[] inputColumns = ((InputColumnSinkJob) item).getInput();
				for (InputColumn<?> inputColumn : inputColumns) {
					addGraphNodes(g, scf, inputColumn);
					g.addEdge(++edgeNumber, inputColumn, item);
				}
			}

			if (item instanceof OutcomeSinkJob) {
				Outcome[] requirements = ((OutcomeSinkJob) item).getRequirements();
				if (requirements != null && requirements.length > 0) {
					for (Outcome req : requirements) {
						OutcomeSourceJob source = scf.findOutcomeSource(req);
						if (source != null) {
							addGraphNodes(g, scf, source);
							g.addEdge(++edgeNumber, source, item);
						}
					}
				}
			}

			if (item instanceof InputColumn) {
				InputColumn<?> inputColumn = (InputColumn<?>) item;
				if (inputColumn.isVirtualColumn()) {
					InputColumnSourceJob source = scf.findInputColumnSource(inputColumn);
					if (source != null) {
						addGraphNodes(g, scf, source);
						g.addEdge(++edgeNumber, source, item);
					}
				}
			}
		}
	}
}
