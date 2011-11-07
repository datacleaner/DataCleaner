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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Collection;
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
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.eobjects.analyzer.util.SourceColumnFinder;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.GraphUtils;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.LabelUtils;
import org.eobjects.metamodel.schema.Table;

import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;

public final class VisualizeJobGraph {

	private static final ImageManager imageManager = ImageManager.getInstance();

	private VisualizeJobGraph() {
		// prevent instantiation
	}

	public static JComponent create(final AnalysisJobBuilder analysisJobBuilder, final boolean displayColumns,
			final boolean displayOutcomes) {
		final DirectedGraph<Object, VisualizeJobLink> graph = new DirectedSparseGraph<Object, VisualizeJobLink>();

		final SourceColumnFinder sourceColumnFinder = new SourceColumnFinder();
		sourceColumnFinder.addSources(analysisJobBuilder);
		
		final List<TransformerJobBuilder<?>> tjbs = analysisJobBuilder.getTransformerJobBuilders();
		for (TransformerJobBuilder<?> tjb : tjbs) {
			addGraphNodes(graph, sourceColumnFinder, tjb, displayColumns, displayOutcomes);
		}

		final List<AnalyzerJobBuilder<?>> ajbs = analysisJobBuilder.getAnalyzerJobBuilders();
		for (AnalyzerJobBuilder<?> ajb : ajbs) {
			addGraphNodes(graph, sourceColumnFinder, ajb, displayColumns, displayOutcomes);
		}

		final List<FilterJobBuilder<?, ?>> fjbs = analysisJobBuilder.getFilterJobBuilders();
		for (FilterJobBuilder<?, ?> fjb : fjbs) {
			addGraphNodes(graph, sourceColumnFinder, fjb, displayColumns, displayOutcomes);
		}

		if (graph.getVertexCount() == 0) {
			graph.addVertex("No components in job");
		}

		final VisualizeJobLayoutTransformer layoutTransformer = new VisualizeJobLayoutTransformer(graph);
		final Dimension preferredSize = layoutTransformer.getPreferredSize();
		final StaticLayout<Object, VisualizeJobLink> layout = new StaticLayout<Object, VisualizeJobLink>(graph,
				layoutTransformer, preferredSize);

		Collection<Object> vertices = graph.getVertices();
		for (Object vertex : vertices) {
			// manually initialize all vertices
			layout.transform(vertex);
		}
		if (!layoutTransformer.isTransformed()) {
			throw new IllegalStateException("Layout transformer was never invoked!");
		}

		final VisualizationViewer<Object, VisualizeJobLink> visualizationViewer = new VisualizationViewer<Object, VisualizeJobLink>(
				layout);
		visualizationViewer.setSize(preferredSize);
		GraphUtils.applyStyles(visualizationViewer);

		final RenderContext<Object, VisualizeJobLink> renderContext = visualizationViewer.getRenderContext();

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
				if (obj instanceof Table) {
					return ((Table) obj).getName();
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
				if (obj instanceof Table) {
					return imageManager.getImageIcon("images/model/table.png", IconUtils.ICON_SIZE_MEDIUM);
				}
				return imageManager.getImageIcon(IconUtils.STATUS_ERROR);
			}
		});

		DCPanel panel = new DCPanel();
		panel.setPreferredSize(preferredSize);
		panel.setLayout(new BorderLayout());
		panel.add(visualizationViewer, BorderLayout.CENTER);
		return panel;
	}

	private static void addGraphNodes(Graph<Object, VisualizeJobLink> g, SourceColumnFinder scf, Object item,
			boolean displayColumns, boolean displayFilterOutcomes) {
		if (!displayColumns && item instanceof InputColumn) {
			return;
		} else if (!displayFilterOutcomes && item instanceof FilterOutcome) {
			return;
		}
		if (!g.containsVertex(item)) {

			g.addVertex(item);

			if (item instanceof InputColumnSinkJob) {
				InputColumn<?>[] inputColumns = ((InputColumnSinkJob) item).getInput();
				for (InputColumn<?> inputColumn : inputColumns) {
					if (displayColumns) {
						// add the column itself
						addGraphNodes(g, scf, inputColumn, displayColumns, displayFilterOutcomes);
						addEdge(g, inputColumn, item);
					} else {
						// add the origin of the column
						if (inputColumn.isVirtualColumn()) {
							InputColumnSourceJob source = scf.findInputColumnSource(inputColumn);
							if (source != null) {
								addGraphNodes(g, scf, source, displayColumns, displayFilterOutcomes);
								addEdge(g, source, item);
							}
						}
						
						if (inputColumn.isPhysicalColumn()) {
							Table table = inputColumn.getPhysicalColumn().getTable();
							if (table != null) {
								addGraphNodes(g, scf, table, displayColumns, displayFilterOutcomes);
								addEdge(g, table, item);
							}
						}
					}
				}
			}

			if (item instanceof FilterOutcome) {
				OutcomeSourceJob source = scf.findOutcomeSource((FilterOutcome) item);
				if (source != null) {
					addGraphNodes(g, scf, source, displayColumns, displayFilterOutcomes);
					addEdge(g, source, item);
				}
			}

			if (item instanceof OutcomeSinkJob) {
				Outcome[] requirements = ((OutcomeSinkJob) item).getRequirements();
				if (requirements != null && requirements.length > 0) {
					for (Outcome req : requirements) {
						if (displayFilterOutcomes) {
							// add the filter outcome itself
							addGraphNodes(g, scf, req, displayColumns, displayFilterOutcomes);
							addEdge(g, req, item);
						} else {
							// add the origin of the filter outcome
							OutcomeSourceJob source = scf.findOutcomeSource(req);
							if (source != null) {
								addGraphNodes(g, scf, source, displayColumns, displayFilterOutcomes);
								addEdge(g, source, item);
							}
						}
					}
				}
			}

			if (item instanceof InputColumn) {
				InputColumn<?> inputColumn = (InputColumn<?>) item;
				if (inputColumn.isVirtualColumn()) {
					InputColumnSourceJob source = scf.findInputColumnSource(inputColumn);
					if (source != null) {
						addGraphNodes(g, scf, source, displayColumns, displayFilterOutcomes);
						addEdge(g, source, item);
					}
				}

				if (inputColumn.isPhysicalColumn()) {
					Table table = inputColumn.getPhysicalColumn().getTable();
					if (table != null) {
						addGraphNodes(g, scf, table, displayColumns, displayFilterOutcomes);
						addEdge(g, table, item);
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
