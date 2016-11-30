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
package org.datacleaner.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;
import org.datacleaner.api.ExpressionBasedInputColumn;
import org.datacleaner.api.InputColumn;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.ComponentRequirement;
import org.datacleaner.job.FilterOutcome;
import org.datacleaner.job.HasComponentRequirement;
import org.datacleaner.job.HasFilterOutcomes;
import org.datacleaner.job.InputColumnSinkJob;
import org.datacleaner.job.InputColumnSourceJob;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.SourceColumns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for traversing dependencies between virtual and physical columns.
 *
 * For performance reasons this class stores found sources in an internal cache. As there is no mechanism to
 * invalidate or refresh this cache, instances of this class should not be assigned to fields of other
 * classes.
 */
public class SourceColumnFinder {

    private static final String LOG_MESSAGE_RECURSIVE_TRAVERSAL =
            "Ending traversal of object graph because the same originating objects are appearing recursively";

    private static final Logger logger = LoggerFactory.getLogger(SourceColumnFinder.class);
    private final Map<InputColumn<?>, Set<Column>> originatingColumnsOfInputColumnCache = new HashMap<>();
    private final Map<Object, Set<Column>> originatingColumnsOfSourceCache = new HashMap<>();
    private Set<InputColumnSinkJob> _inputColumnSinks = new HashSet<>();
    private Set<InputColumnSourceJob> _inputColumnSources = new LinkedHashSet<>();
    private Set<HasFilterOutcomes> _outcomeSources = new HashSet<>();
    private Set<HasComponentRequirement> _outcomeSinks = new HashSet<>();

    private void addSources(final Object... sources) {
        for (final Object source : sources) {
            if (source instanceof InputColumnSinkJob) {
                _inputColumnSinks.add((InputColumnSinkJob) source);
            }
            if (source instanceof InputColumnSourceJob) {
                _inputColumnSources.add((InputColumnSourceJob) source);
            }
            if (source instanceof HasFilterOutcomes) {
                _outcomeSources.add((HasFilterOutcomes) source);
            }
            if (source instanceof HasComponentRequirement) {
                _outcomeSinks.add((HasComponentRequirement) source);
            }
        }
    }

    private void addSources(final Collection<?> sources) {
        addSources(sources.toArray());
    }

    public void addSources(final AnalysisJobBuilder job) {
        addSources(new SourceColumns(job.getSourceColumns()));
        addSources(job.getFilterComponentBuilders());
        addSources(job.getTransformerComponentBuilders());
        addSources(job.getAnalyzerComponentBuilders());
    }

    public void addSources(final AnalysisJob job) {
        addSources(new SourceColumns(job.getSourceColumns()));
        addSources(job.getFilterJobs());
        addSources(job.getTransformerJobs());
        addSources(job.getAnalyzerJobs());
    }

    public List<InputColumn<?>> findInputColumns(final Class<?> dataType) {
        final List<InputColumn<?>> result = new ArrayList<>();
        for (final InputColumnSourceJob source : _inputColumnSources) {
            final InputColumn<?>[] outputColumns = source.getOutput();
            for (final InputColumn<?> col : outputColumns) {
                final Class<?> columnDataType = col.getDataType();
                if (dataType == null || columnDataType == null) {
                    result.add(col);
                } else {
                    if (ReflectionUtils.is(columnDataType, dataType)) {
                        result.add(col);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Finds all source jobs/components for a particular job/component. This
     * method uses {@link Object} as types because input and output can be quite
     * polymorphic. Typically {@link InputColumnSinkJob},
     * {@link InputColumnSourceJob}, {@link HasComponentRequirement} and
     * {@link OutcomeSourceJob} implementations are used.
     *
     * @param job
     *            typically some {@link InputColumnSinkJob}
     * @return a list of jobs/components that are a source of this job.
     */
    public Set<Object> findAllSourceJobs(final Object job) {
        final Set<Object> result = new HashSet<>();
        findAllSourceJobs(job, result);
        return result;
    }

    private void findAllSourceJobs(final Object job, final Set<Object> result) {
        if (job == null) {
            return;
        }

        if (job instanceof InputColumnSinkJob) {
            final InputColumn<?>[] inputColumns = ((InputColumnSinkJob) job).getInput();
            for (final InputColumn<?> inputColumn : inputColumns) {
                final InputColumnSourceJob source = findInputColumnSource(inputColumn);
                if (source != null) {
                    final boolean added = result.add(source);
                    if (added) {
                        findAllSourceJobs(source, result);
                    }
                }
            }
        }

        if (job instanceof HasComponentRequirement) {
            final HasComponentRequirement hasComponentRequirement = (HasComponentRequirement) job;
            final ComponentRequirement requirement = hasComponentRequirement.getComponentRequirement();
            findAllSourceJobs(requirement, result);
        }

        if (job instanceof ComponentRequirement) {
            final Collection<FilterOutcome> requirements = getProcessingDependencies((ComponentRequirement) job);
            for (final FilterOutcome outcome : requirements) {
                final HasFilterOutcomes source = findOutcomeSource(outcome);
                if (source != null) {
                    final boolean added = result.add(source);
                    if (added) {
                        findAllSourceJobs(source, result);
                    }
                }
            }
        }
    }

    public InputColumnSourceJob findInputColumnSource(final InputColumn<?> inputColumn) {
        if (inputColumn instanceof ExpressionBasedInputColumn) {
            return null;
        }
        for (final InputColumnSourceJob source : _inputColumnSources) {
            final InputColumn<?>[] output = source.getOutput();
            for (final InputColumn<?> column : output) {
                if (inputColumn.equals(column)) {
                    return source;
                }
            }
        }
        return null;
    }

    public HasFilterOutcomes findOutcomeSource(final FilterOutcome requirement) {
        for (final HasFilterOutcomes source : _outcomeSources) {
            final Collection<FilterOutcome> outcomes = source.getFilterOutcomes();
            for (final FilterOutcome outcome : outcomes) {
                if (requirement.equals(outcome)) {
                    return source;
                }
            }
        }
        return null;
    }

    public Set<Column> findOriginatingColumns(final FilterOutcome requirement) {
        final HasFilterOutcomes source = findOutcomeSource(requirement);

        return findOriginatingColumnsOfSource(source);
    }

    public Table findOriginatingTable(final FilterOutcome requirement) {
        return findOriginatingTable(requirement, new HashSet<>());
    }

    private Table findOriginatingTable(final FilterOutcome requirement, final Set<Object> resolvedSet) {
        final HasFilterOutcomes source = findOutcomeSource(requirement);
        if (!resolvedSet.add(source)) {
            logger.debug(LOG_MESSAGE_RECURSIVE_TRAVERSAL);
            return null;
        }
        return findOriginatingTableOfSource(source, resolvedSet);
    }

    public Table findOriginatingTable(final InputColumn<?> inputColumn) {
        return findOriginatingTable(inputColumn, new HashSet<>());
    }

    private Table findOriginatingTable(final InputColumn<?> inputColumn, final Set<Object> resolvedSet) {
        if (!resolvedSet.add(inputColumn)) {
            logger.debug(LOG_MESSAGE_RECURSIVE_TRAVERSAL);
            return null;
        }

        if (inputColumn == null) {
            logger.warn("InputColumn was null, no originating table found");
            return null;
        }
        if (inputColumn.isPhysicalColumn()) {
            return inputColumn.getPhysicalColumn().getTable();
        }

        final InputColumnSourceJob inputColumnSource = findInputColumnSource(inputColumn);
        if (!resolvedSet.add(inputColumnSource)) {
            logger.debug(LOG_MESSAGE_RECURSIVE_TRAVERSAL);
            return null;
        }

        return findOriginatingTableOfSource(inputColumnSource, resolvedSet);
    }

    private Table findOriginatingTableOfSource(final Object source, final Set<Object> resolvedSet) {
        final Set<Table> result = new TreeSet<>();
        if (source instanceof InputColumnSinkJob) {
            final InputColumn<?>[] input = ((InputColumnSinkJob) source).getInput();
            if (input != null) {
                for (final InputColumn<?> col : input) {
                    if (col == null) {
                        logger.warn("InputColumn sink had a null-column element!");
                    } else {
                        final Table table = findOriginatingTable(col, resolvedSet);
                        if (table != null) {
                            result.add(table);
                        }
                    }
                }
            }
        }
        if (source instanceof HasComponentRequirement) {
            final HasComponentRequirement hasComponentRequirement = (HasComponentRequirement) source;
            final ComponentRequirement componentRequirement = hasComponentRequirement.getComponentRequirement();
            final Collection<FilterOutcome> requirements = getProcessingDependencies(componentRequirement);
            for (final FilterOutcome outcome : requirements) {
                final Table table = findOriginatingTable(outcome, resolvedSet);
                if (table != null) {
                    result.add(table);
                }
            }
        }

        if (result.isEmpty()) {
            return null;
        }
        if (result.size() == 1) {
            return result.iterator().next();
        }
        final StringBuilder sb = new StringBuilder();
        for (final Table table : result) {
            if (sb.length() != 0) {
                sb.append(", ");
            }
            sb.append(table.getName());
        }
        throw new IllegalStateException("Multiple originating tables (" + sb + ") found for source: " + source);
    }

    private Set<Column> findOriginatingColumnsOfInputColumn(final InputColumn<?> inputColumn) {
        final Set<Column> cachedOriginatingColumns = originatingColumnsOfInputColumnCache.get(inputColumn);
        if (cachedOriginatingColumns != null) {
            return cachedOriginatingColumns;
        }

        final Set<Column> originatingColumns = new HashSet<>();

        if (inputColumn != null) {
            if (inputColumn.isPhysicalColumn()) {
                originatingColumns.add(inputColumn.getPhysicalColumn());
            } else {
                final InputColumnSourceJob source = findInputColumnSource(inputColumn);

                originatingColumns.addAll(findOriginatingColumnsOfSource(source));
            }
        }

        originatingColumnsOfInputColumnCache.put(inputColumn, originatingColumns);

        return originatingColumns;
    }

    private Set<Column> findOriginatingColumnsOfOutcome(final FilterOutcome requirement) {
        final HasFilterOutcomes source = findOutcomeSource(requirement);
        return findOriginatingColumnsOfSource(source);
    }

    private Set<Column> findOriginatingColumnsOfSource(final Object source) {
        final Set<Column> cachedOriginatingColumns = originatingColumnsOfSourceCache.get(source);
        if (cachedOriginatingColumns != null) {
            return cachedOriginatingColumns;
        }

        final Set<Column> originatingColumns = new HashSet<>();

        if (source != null) {
            if (source instanceof InputColumnSinkJob) {
                final InputColumn<?>[] input = ((InputColumnSinkJob) source).getInput();
                if (input != null) {

                    for (final InputColumn<?> inputColumn : input) {
                        originatingColumns.addAll(findOriginatingColumnsOfInputColumn(inputColumn));
                    }
                }
            }
            if (source instanceof HasComponentRequirement) {
                final HasComponentRequirement hasComponentRequirement = (HasComponentRequirement) source;
                final ComponentRequirement componentRequirement = hasComponentRequirement.getComponentRequirement();
                final Collection<FilterOutcome> requirements = getProcessingDependencies(componentRequirement);
                for (final FilterOutcome outcome : requirements) {
                    originatingColumns.addAll(findOriginatingColumnsOfOutcome(outcome));
                }
            }
        }

        originatingColumnsOfSourceCache.put(source, originatingColumns);

        return originatingColumns;
    }

    private Collection<FilterOutcome> getProcessingDependencies(final ComponentRequirement componentRequirement) {
        if (componentRequirement == null) {
            return Collections.emptyList();
        }
        final Collection<FilterOutcome> processingDependencies = componentRequirement.getProcessingDependencies();
        if (processingDependencies == null) {
            return Collections.emptyList();
        }
        return processingDependencies;
    }

    public Set<Column> findOriginatingColumns(final InputColumn<?> inputColumn) {
        // TODO: Detect cyclic dependencies between transformers (A depends on
        // B, B depends on A)

        return findOriginatingColumnsOfInputColumn(inputColumn);
    }
}
