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
import java.awt.Font;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JComponent;

import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.TruePredicate;
import org.eobjects.analyzer.beans.writers.WriteDataCategory;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.descriptors.BeanDescriptor;
import org.eobjects.analyzer.job.AnalysisJob;
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
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.HasAnalyzerResult;
import org.eobjects.analyzer.util.ReflectionUtils;
import org.eobjects.analyzer.util.SourceColumnFinder;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.GraphUtils;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.LabelUtils;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.metamodel.schema.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;

/**
 * Class capable of creating graphs that visualize {@link AnalysisJob}s or parts
 * of them.
 */
public final class VisualizeJobGraph {

	private static final String MORE_COLUMNS_VERTEX = "...";

	private static final ImageManager imageManager = ImageManager.getInstance();
	private static final Logger logger = LoggerFactory
			.getLogger(VisualizeJobGraph.class);

	private final DirectedGraph<Object, VisualizeJobLink> _graph;
	private final Set<Object> _highlighedVertexes;

	public VisualizeJobGraph() {
		this(new DirectedSparseGraph<Object, VisualizeJobLink>());
	}

	public VisualizeJobGraph(DirectedGraph<Object, VisualizeJobLink> graph) {
		_graph = graph;
		_highlighedVertexes = new HashSet<Object>();
	}

	public VisualizeJobGraph highlightVertex(Object vertex) {
		_highlighedVertexes.add(vertex);
		return this;
	}

	/**
	 * Builds a graph for a single {@link AbstractBeanJobBuilder} which
	 * comprises only it's immediate connections
	 * 
	 * @param beanJobBuilder
	 * @param displayColumns
	 * @param displayOutcomes
	 * @return
	 */
	public static JComponent create(
			final AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder) {
		final VisualizeJobGraph graph = new VisualizeJobGraph();

		final SourceColumnFinder sourceColumnFinder = new SourceColumnFinder();
		sourceColumnFinder.addSources(beanJobBuilder.getAnalysisJobBuilder());

		graph.addNodes(sourceColumnFinder, beanJobBuilder, false, true, 2);

		// add output outcomes
		if (beanJobBuilder instanceof OutcomeSourceJob) {
			Outcome[] outcomes = ((OutcomeSourceJob) beanJobBuilder)
					.getOutcomes();
			for (Outcome outcome : outcomes) {
				graph.addNodes(null, outcome, true, true, 0);
				graph.addEdge(beanJobBuilder, outcome);
			}
		}

		// add output columns
		final int maxColumns = 5;
		if (beanJobBuilder instanceof InputColumnSourceJob) {
			InputColumn<?>[] outputColumns = ((InputColumnSourceJob) beanJobBuilder)
					.getOutput();
			for (int i = 0; i < outputColumns.length; i++) {
				if (i == maxColumns) {
					graph.addNodes(sourceColumnFinder, MORE_COLUMNS_VERTEX,
							true, true, 0);
					graph.addEdge(beanJobBuilder, MORE_COLUMNS_VERTEX);
					graph.highlightVertex(MORE_COLUMNS_VERTEX);
					break;
				}
				InputColumn<?> outputColumn = outputColumns[i];
				graph.addNodes(null, outputColumn, true, true, 0);
				graph.addEdge(beanJobBuilder, outputColumn);
			}
		}

		BeanDescriptor<?> descriptor = beanJobBuilder.getDescriptor();
		if (descriptor.getComponentCategories().contains(
				new WriteDataCategory())) {
			logger.debug(
					"Not rendering analyzer result for {} because it is a data writer",
					descriptor);
		} else {
			final Class<?> componentClass = descriptor.getComponentClass();
			if (ReflectionUtils.is(componentClass, HasAnalyzerResult.class)) {

				// this approach is maybe a bit dodgy - not so error safe
				try {
					final Class<?> typeParameter = ReflectionUtils
							.getTypeParameter(componentClass,
									HasAnalyzerResult.class, 0);
					graph.addNodes(null, typeParameter, true, true, 0);
					graph.addEdge(beanJobBuilder, typeParameter);
				} catch (Exception e) {
					logger.warn(
							"Could not retrieve and present analyzer result type",
							e);
				}
			}
		}

		graph.highlightVertex(beanJobBuilder);
		return graph.renderGraph();
	}

	/**
	 * Builds a graph for a complete analysis job, based on an
	 * {@link AnalysisJobBuilder}.
	 * 
	 * @param analysisJobBuilder
	 * @param displayColumns
	 * @param displayOutcomes
	 * @return
	 */
	public static JComponent create(
			final AnalysisJobBuilder analysisJobBuilder,
			final boolean displayColumns, final boolean displayOutcomes) {
		final VisualizeJobGraph graph = new VisualizeJobGraph();

		final SourceColumnFinder sourceColumnFinder = new SourceColumnFinder();
		sourceColumnFinder.addSources(analysisJobBuilder);

		final List<TransformerJobBuilder<?>> tjbs = analysisJobBuilder
				.getTransformerJobBuilders();
		for (TransformerJobBuilder<?> tjb : tjbs) {
			graph.addNodes(sourceColumnFinder, tjb, displayColumns,
					displayOutcomes, -1);
		}

		final List<AnalyzerJobBuilder<?>> ajbs = analysisJobBuilder
				.getAnalyzerJobBuilders();
		for (AnalyzerJobBuilder<?> ajb : ajbs) {
			graph.addNodes(sourceColumnFinder, ajb, displayColumns,
					displayOutcomes, -1);
		}

		final List<FilterJobBuilder<?, ?>> fjbs = analysisJobBuilder
				.getFilterJobBuilders();
		for (FilterJobBuilder<?, ?> fjb : fjbs) {
			graph.addNodes(sourceColumnFinder, fjb, displayColumns,
					displayOutcomes, -1);
		}

		return graph.renderGraph();
	}

	public JComponent renderGraph() {
		final int vertexCount = _graph.getVertexCount();
		if (vertexCount == 0) {
			_graph.addVertex("No components in job");
		}
		logger.debug("Rendering graph with {} vertices", vertexCount);

		final VisualizeJobLayoutTransformer layoutTransformer = new VisualizeJobLayoutTransformer(
				_graph);
		final Dimension preferredSize = layoutTransformer.getPreferredSize();
		final StaticLayout<Object, VisualizeJobLink> layout = new StaticLayout<Object, VisualizeJobLink>(
				_graph, layoutTransformer, preferredSize);

		Collection<Object> vertices = _graph.getVertices();
		for (Object vertex : vertices) {
			// manually initialize all vertices
			layout.transform(vertex);
		}
		if (!layoutTransformer.isTransformed()) {
			throw new IllegalStateException(
					"Layout transformer was never invoked!");
		}

		final VisualizationViewer<Object, VisualizeJobLink> visualizationViewer = new VisualizationViewer<Object, VisualizeJobLink>(
				layout);
		visualizationViewer.setSize(preferredSize);
		GraphUtils.applyStyles(visualizationViewer);

		final RenderContext<Object, VisualizeJobLink> renderContext = visualizationViewer
				.getRenderContext();

		// render fonts (some may be highlighted)
		renderContext.setVertexFontTransformer(new Transformer<Object, Font>() {

			private final Font normalFont = WidgetUtils.FONT_SMALL;
			private final Font highlighedFont = normalFont
					.deriveFont(Font.BOLD);

			@Override
			public Font transform(Object vertex) {
				if (_highlighedVertexes.contains(vertex)) {
					return highlighedFont;
				}
				return normalFont;
			}
		});

		// render labels
		renderContext
				.setVertexLabelTransformer(new Transformer<Object, String>() {
					@Override
					public String transform(Object obj) {
						if (obj instanceof InputColumn) {
							return ((InputColumn<?>) obj).getName();
						}
						if (obj instanceof AbstractBeanJobBuilder) {
							return LabelUtils
									.getLabel((AbstractBeanJobBuilder<?, ?, ?>) obj);
						}
						if (obj instanceof FilterOutcome) {
							return ((FilterOutcome) obj).getCategory().name();
						}
						if (obj instanceof MergedOutcomeJobBuilder) {
							return LabelUtils
									.getLabel((MergedOutcomeJobBuilder) obj);
						}
						if (obj instanceof Table) {
							return ((Table) obj).getName();
						}
						if (obj instanceof Class) {
							Class<?> cls = (Class<?>) obj;
							if (ReflectionUtils.is(cls, AnalyzerResult.class)) {
								return "Analyzer result";
							}
							return cls.getSimpleName();
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
				if (obj == MORE_COLUMNS_VERTEX || obj instanceof InputColumn) {
					return imageManager.getImageIcon("images/model/column.png",
							IconUtils.ICON_SIZE_SMALL);
				}
				if (obj instanceof AbstractBeanJobBuilder) {
					return IconUtils
							.getDescriptorIcon(((AbstractBeanJobBuilder<?, ?, ?>) obj)
									.getDescriptor());
				}
				if (obj instanceof FilterOutcome) {
					return imageManager.getImageIcon(
							"images/component-types/filter-outcome.png",
							IconUtils.ICON_SIZE_SMALL);
				}
				if (obj instanceof MergedOutcomeJobBuilder) {
					return imageManager.getImageIcon(
							"images/component-types/merged-outcome.png",
							IconUtils.ICON_SIZE_SMALL);
				}
				if (obj instanceof Table) {
					return imageManager.getImageIcon("images/model/table.png",
							IconUtils.ICON_SIZE_MEDIUM);
				}
				if (obj instanceof Class) {
					Class<?> cls = (Class<?>) obj;
					if (ReflectionUtils.is(cls, AnalyzerResult.class)) {
						return imageManager.getImageIcon(
								"images/model/result.png",
								IconUtils.ICON_SIZE_MEDIUM);
					}
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

	private void addNodes(SourceColumnFinder scf, Object item,
			boolean displayColumns, boolean displayFilterOutcomes,
			int recurseCount) {
		if (!displayColumns && item instanceof InputColumn) {
			return;
		} else if (!displayFilterOutcomes && item instanceof FilterOutcome) {
			return;
		}
		if (!_graph.containsVertex(item)) {
			_graph.addVertex(item);

			if (recurseCount == 0) {
				return;
			}

			// decrement recurseCount
			recurseCount--;

			if (item instanceof InputColumnSinkJob) {
				InputColumn<?>[] inputColumns = ((InputColumnSinkJob) item)
						.getInput();
				for (InputColumn<?> inputColumn : inputColumns) {
					if (displayColumns) {
						// add the column itself
						addNodes(scf, inputColumn, displayColumns,
								displayFilterOutcomes, recurseCount);
						addEdge(inputColumn, item);
					} else {
						// add the origin of the column
						if (inputColumn.isVirtualColumn()) {
							InputColumnSourceJob source = scf
									.findInputColumnSource(inputColumn);
							if (source != null) {
								addNodes(scf, source, displayColumns,
										displayFilterOutcomes, recurseCount);
								addEdge(source, item);
							}
						}

						if (inputColumn.isPhysicalColumn()) {
							Table table = inputColumn.getPhysicalColumn()
									.getTable();
							if (table != null) {
								addNodes(scf, table, displayColumns,
										displayFilterOutcomes, recurseCount);
								addEdge(table, item);
							}
						}
					}
				}
			}

			if (item instanceof FilterOutcome) {
				OutcomeSourceJob source = scf
						.findOutcomeSource((FilterOutcome) item);
				if (source != null) {
					addNodes(scf, source, displayColumns,
							displayFilterOutcomes, recurseCount);
					addEdge(source, item);
				}
			}

			if (item instanceof OutcomeSinkJob) {
				Outcome[] requirements = ((OutcomeSinkJob) item)
						.getRequirements();
				if (requirements != null && requirements.length > 0) {
					for (Outcome req : requirements) {
						if (displayFilterOutcomes) {
							// add the filter outcome itself
							addNodes(scf, req, displayColumns,
									displayFilterOutcomes, recurseCount);
							addEdge(req, item);
						} else {
							// add the origin of the filter outcome
							OutcomeSourceJob source = scf
									.findOutcomeSource(req);
							if (source != null) {
								addNodes(scf, source, displayColumns,
										displayFilterOutcomes, recurseCount);
								addEdge(source, item);
							}
						}
					}
				}
			}

			if (item instanceof InputColumn) {
				InputColumn<?> inputColumn = (InputColumn<?>) item;
				if (inputColumn.isVirtualColumn()) {
					InputColumnSourceJob source = scf
							.findInputColumnSource(inputColumn);
					if (source != null) {
						addNodes(scf, source, displayColumns,
								displayFilterOutcomes, recurseCount);
						addEdge(source, item);
					}
				}

				if (inputColumn.isPhysicalColumn()) {
					Table table = inputColumn.getPhysicalColumn().getTable();
					if (table != null) {
						addNodes(scf, table, displayColumns,
								displayFilterOutcomes, recurseCount);
						addEdge(table, item);
					}
				}
			}
		}
	}

	private void addEdge(Object from, Object to) {
		VisualizeJobLink link = new VisualizeJobLink(from, to);
		if (!_graph.containsEdge(link)) {
			_graph.addEdge(link, from, to, EdgeType.DIRECTED);
		}
	}
}
