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
package org.eobjects.datacleaner.widgets.visualization;

import java.awt.Dimension;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JComponent;

import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.TruePredicate;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.job.FilterOutcome;
import org.eobjects.analyzer.job.InputColumnSinkJob;
import org.eobjects.analyzer.job.InputColumnSourceJob;
import org.eobjects.analyzer.job.Outcome;
import org.eobjects.analyzer.job.OutcomeSinkJob;
import org.eobjects.analyzer.job.OutcomeSourceJob;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.AnalyzerJobBuilder;
import org.eobjects.analyzer.job.builder.FilterJobBuilder;
import org.eobjects.analyzer.job.builder.MergedOutcomeJobBuilder;
import org.eobjects.analyzer.job.builder.RowProcessingAnalyzerJobBuilder;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.eobjects.analyzer.util.SourceColumnFinder;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.LabelUtils;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;

public final class VisualizeJobGraph {

	private static final long serialVersionUID = 1L;
	private static final ImageManager imageManager = ImageManager.getInstance();

	private VisualizeJobGraph() {
		// prevent instantiation
	}

	public static JComponent create(AnalysisJobBuilder analysisJobBuilder) {
		final DirectedGraph<Object, VisualizeJobLink> g = new DirectedSparseGraph<Object, VisualizeJobLink>();

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

		final List<FilterJobBuilder<?, ?>> fjbs = analysisJobBuilder.getFilterJobBuilders();
		for (FilterJobBuilder<?, ?> fjb : fjbs) {
			addGraphNodes(g, scf, fjb);
		}

		if (g.getVertexCount() == 0) {
			g.addVertex("No components in job");
		}

		final VisualizeJobLayoutTransformer layoutTransformer = new VisualizeJobLayoutTransformer(g);
		final Layout<Object, VisualizeJobLink> layout = new StaticLayout<Object, VisualizeJobLink>(g, layoutTransformer);

		final Dimension preferredSize = layoutTransformer.getPreferredSize();
		final VisualizationViewer<Object, VisualizeJobLink> bvs = new VisualizationViewer<Object, VisualizeJobLink>(layout,
				preferredSize);

		final RenderContext<Object, VisualizeJobLink> renderContext = bvs.getRenderContext();

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
				if (obj instanceof FilterOutcome) {
					return ((FilterOutcome) obj).getCategory().name();
				}
				if (obj instanceof MergedOutcomeJobBuilder) {
					return LabelUtils.getLabel((MergedOutcomeJobBuilder) obj);
				}
				return obj.toString();
			}
		});

		// render arrows
		final Predicate<Context<Graph<Object, VisualizeJobLink>, VisualizeJobLink>> edgeArrowPredicate = TruePredicate
				.getInstance();
		renderContext.setEdgeArrowPredicate(edgeArrowPredicate);

		// render icons
		renderContext.setVertexIconTransformer(new Transformer<Object, Icon>() {

			@Override
			public Icon transform(Object obj) {
				if (obj instanceof InputColumn) {
					return imageManager.getImageIcon("images/model/column.png", IconUtils.ICON_SIZE_SMALL);
				}
				if (obj instanceof AbstractBeanJobBuilder) {
					return IconUtils.getDescriptorIcon(((AbstractBeanJobBuilder<?, ?, ?>) obj).getDescriptor());
				}
				if (obj instanceof FilterOutcome) {
					return imageManager.getImageIcon("images/component-types/filter-outcome.png", IconUtils.ICON_SIZE_SMALL);
				}
				if (obj instanceof MergedOutcomeJobBuilder) {
					return imageManager.getImageIcon("images/component-types/merged-outcome.png", IconUtils.ICON_SIZE_SMALL);
				}
				return imageManager.getImageIcon("images/status/error.png");
			}
		});

		return bvs;
	}

	private static void addGraphNodes(Graph<Object, VisualizeJobLink> g, SourceColumnFinder scf, Object item) {
		if (!g.containsVertex(item)) {
			g.addVertex(item);
			if (item instanceof InputColumnSinkJob) {
				InputColumn<?>[] inputColumns = ((InputColumnSinkJob) item).getInput();
				for (InputColumn<?> inputColumn : inputColumns) {
					addGraphNodes(g, scf, inputColumn);
					addEdge(g, inputColumn, item);
				}
			}

			if (item instanceof FilterOutcome) {
				OutcomeSourceJob source = scf.findOutcomeSource((FilterOutcome) item);
				if (source != null) {
					addGraphNodes(g, scf, source);
					addEdge(g, source, item);
				}
			}

			if (item instanceof OutcomeSinkJob) {
				Outcome[] requirements = ((OutcomeSinkJob) item).getRequirements();
				if (requirements != null && requirements.length > 0) {
					for (Outcome req : requirements) {
						addGraphNodes(g, scf, req);
						addEdge(g, req, item);
					}
				}
			}

			if (item instanceof InputColumn) {
				InputColumn<?> inputColumn = (InputColumn<?>) item;
				if (inputColumn.isVirtualColumn()) {
					InputColumnSourceJob source = scf.findInputColumnSource(inputColumn);
					if (source != null) {
						addGraphNodes(g, scf, source);
						addEdge(g, source, item);
					}
				}
			}
		}
	}

	private static void addEdge(Graph<Object, VisualizeJobLink> g, Object from, Object to) {
		VisualizeJobLink link = new VisualizeJobLink(from, to);
		if (!g.containsEdge(link)) {
			g.addEdge(link, from, to, EdgeType.DIRECTED);
		}
	}
}
