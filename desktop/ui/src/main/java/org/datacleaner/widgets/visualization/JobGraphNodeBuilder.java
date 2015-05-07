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
package org.datacleaner.widgets.visualization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.metamodel.schema.Table;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.OutputDataStream;
import org.datacleaner.job.ComponentRequirement;
import org.datacleaner.job.FilterOutcome;
import org.datacleaner.job.HasComponentRequirement;
import org.datacleaner.job.HasFilterOutcomes;
import org.datacleaner.job.InputColumnSinkJob;
import org.datacleaner.job.InputColumnSourceJob;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.job.builder.FilterComponentBuilder;
import org.datacleaner.job.builder.TransformerComponentBuilder;
import org.datacleaner.util.SourceColumnFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;

/**
 * Object responsible for building the nodes (vertices and edges) of a
 * {@link JobGraph}.
 */
class JobGraphNodeBuilder {

    private static final Logger logger = LoggerFactory.getLogger(JobGraphNodeBuilder.class);

    private final AnalysisJobBuilder _analysisJobBuilder;

    public JobGraphNodeBuilder(AnalysisJobBuilder analysisJobBuilder) {
        _analysisJobBuilder = analysisJobBuilder;
    }

    public DirectedGraph<Object, JobGraphLink> buildGraph() {
        final DirectedGraph<Object, JobGraphLink> graph = new DirectedSparseGraph<Object, JobGraphLink>();
        buildGraph(graph, _analysisJobBuilder);
        return graph;
    }

    private void buildGraph(DirectedGraph<Object, JobGraphLink> graph, AnalysisJobBuilder analysisJobBuilder) {
        final SourceColumnFinder sourceColumnFinder = new SourceColumnFinder();
        sourceColumnFinder.addSources(analysisJobBuilder);

        final List<Table> sourceTables = analysisJobBuilder.getSourceTables();
        for (Table table : sourceTables) {
            addNodes(graph, sourceColumnFinder, table, -1);
        }

        final List<TransformerComponentBuilder<?>> tjbs = analysisJobBuilder.getTransformerComponentBuilders();
        for (TransformerComponentBuilder<?> tjb : tjbs) {
            addNodes(graph, sourceColumnFinder, tjb, -1);
        }

        final List<AnalyzerComponentBuilder<?>> ajbs = analysisJobBuilder.getAnalyzerComponentBuilders();
        for (AnalyzerComponentBuilder<?> ajb : ajbs) {
            addNodes(graph, sourceColumnFinder, ajb, -1);
        }

        final List<FilterComponentBuilder<?, ?>> fjbs = analysisJobBuilder.getFilterComponentBuilders();
        for (FilterComponentBuilder<?, ?> fjb : fjbs) {
            addNodes(graph, sourceColumnFinder, fjb, -1);
        }

        removeUnnecesaryEdges(graph, sourceColumnFinder);
    }

    /**
     * Removes links/edges that are "unnecesary". The rationale here is that we
     * do not want to show every possible link since it clutters readability.
     * 
     * We remove links that represent a direct edge between nodes that are also
     * indirectly linked. Not in "diamond" shaped scenarios, but only in
     * scenarios where a path from A->Z is already represented via A->B->Z or
     * A->B->C->Z etc.
     * 
     * @param graph
     * @param sourceColumnFinder
     */
    private void removeUnnecesaryEdges(DirectedGraph<Object, JobGraphLink> graph, SourceColumnFinder sourceColumnFinder) {
        // This loop is not very pretty but it ensures that we don't prematurely
        // stop looking for stuff to remove from the graph. The issue is that
        // with the current design, the removeUnnecesaryEdges method may remove
        // something which then should call for a re-evaluation of other edges
        // to be removed.
        boolean removedSomething = true;
        while (removedSomething) {
            removedSomething = removeUnnecesaryEdgesIfAny(graph, sourceColumnFinder);
        }
    }

    /**
     * Runs a single check through the graph to remove unnecesary edges (see
     * {@link #removeUnnecesaryEdges(DirectedGraph, SourceColumnFinder)} if any
     * are found.
     * 
     * @param graph
     * @param sourceColumnFinder
     * 
     * @return whether or not any edges were removed
     */
    private boolean removeUnnecesaryEdgesIfAny(final DirectedGraph<Object, JobGraphLink> graph,
            final SourceColumnFinder sourceColumnFinder) {
        final Collection<JobGraphLink> allLinks = graph.getEdges();
        final List<JobGraphLink> linksToRemove = new ArrayList<>();
        for (JobGraphLink link : allLinks) {
            boolean removeable = true;

            if (link.getRequirement() != null) {
                // only links without requirements are candidates
                // for removal
                removeable = false;
            }

            if (removeable) {
                final Object toVertex = link.getTo();
                final Collection<JobGraphLink> edgesGoingIn = graph.getInEdges(toVertex);
                if (edgesGoingIn.size() <= 1) {
                    // if this is the only edge going in, there is no
                    // special interest
                    removeable = false;
                }

                if (removeable) {
                    // check if these links represents a "shortcut" path that
                    // can be left out
                    for (JobGraphLink edgeGoingIn : edgesGoingIn) {
                        if (edgeGoingIn != link) {
                            if (!isEdgeShortcutFor(graph, link, edgeGoingIn)) {
                                removeable = false;
                                break;
                            }
                        }
                    }

                    if (removeable) {
                        logger.debug("Removing unnecesary JobGraphLink: {}", link);
                        linksToRemove.add(link);
                    }
                }
            }
        }

        for (JobGraphLink link : linksToRemove) {
            graph.removeEdge(link);
        }

        return !linksToRemove.isEmpty();
    }

    private boolean isEdgeShortcutFor(DirectedGraph<Object, JobGraphLink> graph, JobGraphLink potentialShortcut,
            JobGraphLink otherEdge) {
        return isEdgeShortcutFor(graph, potentialShortcut, otherEdge, new HashSet<JobGraphLink>());
    }

    private boolean isEdgeShortcutFor(DirectedGraph<Object, JobGraphLink> graph, JobGraphLink potentialShortcut,
            JobGraphLink otherEdge, Set<JobGraphLink> checkedEdges) {
        if (otherEdge == null) {
            return false;
        }

        final Object from = potentialShortcut.getFrom();
        final Object otherFrom = otherEdge.getFrom();
        if (from == otherFrom) {
            return true;
        }

        final Collection<JobGraphLink> inEdges = graph.getInEdges(otherFrom);
        if (inEdges.isEmpty()) {
            // this could be improved since also scenarios with +1 inEdges
            // could be analyzed
            return false;
        }

        for (JobGraphLink inEdge : inEdges) {
            if (checkedEdges.contains(inEdge)) {
                // skip
            } else {
                // prevent recursive nightmares - see issue #326
                checkedEdges.add(inEdge);
                if (!isEdgeShortcutFor(graph, potentialShortcut, inEdge, checkedEdges)) {
                    return false;
                }
            }
        }

        return true;
    }

    private void addNodes(final DirectedGraph<Object, JobGraphLink> graph, final SourceColumnFinder scf,
            final Object item, int recurseCount) {
        if (item == null) {
            throw new IllegalArgumentException("Node item cannot be null");
        }

        if (item instanceof InputColumn) {
            return;
        } else if (item instanceof FilterOutcome) {
            return;
        }
        if (!graph.containsVertex(item)) {
            graph.addVertex(item);

            if (recurseCount == 0) {
                return;
            }

            // decrement recurseCount
            recurseCount--;

            if (item instanceof InputColumnSinkJob) {
                InputColumn<?>[] inputColumns = ((InputColumnSinkJob) item).getInput();
                for (InputColumn<?> inputColumn : inputColumns) {
                    // add the origin of the column
                    if (inputColumn.isVirtualColumn()) {
                        InputColumnSourceJob source = scf.findInputColumnSource(inputColumn);
                        if (source != null) {
                            addNodes(graph, scf, source, recurseCount);
                            addEdge(graph, source, item, null, null);
                        }
                    }

                    if (inputColumn.isPhysicalColumn()) {
                        Table table = inputColumn.getPhysicalColumn().getTable();
                        if (table != null) {
                            addNodes(graph, scf, table, recurseCount);
                            addEdge(graph, table, item, null, null);
                        }
                    }
                }
            }

            if (item instanceof ComponentBuilder) {
                final ComponentBuilder componentBuilder = (ComponentBuilder) item;
                final List<OutputDataStream> streams = componentBuilder.getOutputDataStreams();
                if (streams != null && !streams.isEmpty()) {
                    addStreams(graph, componentBuilder, streams);
                }
            }

            if (item instanceof FilterOutcome) {
                final HasFilterOutcomes source = scf.findOutcomeSource((FilterOutcome) item);
                if (source != null) {
                    addNodes(graph, scf, source, recurseCount);
                    addEdge(graph, source, item, null, null);
                }
            }

            if (item instanceof HasComponentRequirement) {
                final HasComponentRequirement hasComponentRequirement = (HasComponentRequirement) item;
                final Collection<FilterOutcome> filterOutcomes = getProcessingDependencyFilterOutcomes(hasComponentRequirement);
                for (final FilterOutcome filterOutcome : filterOutcomes) {
                    // add the origin of the filter outcome
                    final HasFilterOutcomes source = scf.findOutcomeSource(filterOutcome);
                    if (source != null) {
                        addNodes(graph, scf, source, recurseCount);
                        addEdge(graph, source, item, hasComponentRequirement.getComponentRequirement(), filterOutcome);
                    }
                }
            }

            if (item instanceof InputColumn) {
                InputColumn<?> inputColumn = (InputColumn<?>) item;
                if (inputColumn.isVirtualColumn()) {
                    InputColumnSourceJob source = scf.findInputColumnSource(inputColumn);
                    if (source != null) {
                        addNodes(graph, scf, source, recurseCount);
                        addEdge(graph, source, item, null, null);
                    }
                }

                if (inputColumn.isPhysicalColumn()) {
                    final Table table = inputColumn.getPhysicalColumn().getTable();
                    if (table != null) {
                        addNodes(graph, scf, table, recurseCount);
                        addEdge(graph, table, item, null, null);
                    }
                }
            }
        }
    }

    private void addStreams(final DirectedGraph<Object, JobGraphLink> graph, final ComponentBuilder componentBuilder,
            final List<OutputDataStream> streams) {
        for (final OutputDataStream stream : streams) {
            final AnalysisJobBuilder jobBuilder = componentBuilder.getOutputDataStreamJobBuilder(stream);

            // there is always just 1 source table in output streams
            final Table sourceTable = jobBuilder.getSourceTables().get(0);

            graph.addVertex(sourceTable);
            addEdge(graph, componentBuilder, sourceTable, null, null);
            buildGraph(graph, jobBuilder);
        }
    }

    private Collection<FilterOutcome> getProcessingDependencyFilterOutcomes(HasComponentRequirement item) {
        final ComponentRequirement componentRequirement = item.getComponentRequirement();
        if (componentRequirement == null) {
            return Collections.emptyList();
        }
        return componentRequirement.getProcessingDependencies();
    }

    private void addEdge(DirectedGraph<Object, JobGraphLink> graph, Object from, Object to,
            ComponentRequirement requirement, FilterOutcome filterOutcome) {
        final JobGraphLink link = new JobGraphLink(from, to, requirement, filterOutcome);
        if (!graph.containsEdge(link)) {
            graph.addEdge(link, from, to, EdgeType.DIRECTED);
        }
    }
}
