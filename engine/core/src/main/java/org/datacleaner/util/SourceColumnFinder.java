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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
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
 * Helper class for traversing dependencies between virtual and physical
 * columns.
 */
public class SourceColumnFinder {

    private static final String LOG_MESSAGE_RECURSIVE_TRAVERSAL = "Ending traversal of object graph because the same originating objects are appearing recursively";

    private static final Logger logger = LoggerFactory.getLogger(SourceColumnFinder.class);

    private Set<InputColumnSinkJob> _inputColumnSinks = new HashSet<InputColumnSinkJob>();
    private Set<InputColumnSourceJob> _inputColumnSources = new LinkedHashSet<InputColumnSourceJob>();
    private Set<HasFilterOutcomes> _outcomeSources = new HashSet<HasFilterOutcomes>();
    private Set<HasComponentRequirement> _outcomeSinks = new HashSet<HasComponentRequirement>();

    private void addSources(Object... sources) {
        for (Object source : sources) {
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

    private void addSources(Collection<?> sources) {
        addSources(sources.toArray());
    }

    public void addSources(AnalysisJobBuilder job) {
        addSources(new SourceColumns(job.getSourceColumns()));
        addSources(job.getFilterJobBuilders());
        addSources(job.getTransformerJobBuilders());
        addSources(job.getAnalyzerJobBuilders());
    }

    public void addSources(AnalysisJob job) {
        addSources(new SourceColumns(job.getSourceColumns()));
        addSources(job.getFilterJobs());
        addSources(job.getTransformerJobs());
        addSources(job.getAnalyzerJobs());
    }

    public List<InputColumn<?>> findInputColumns(final Class<?> dataType) {
        final List<InputColumn<?>> result = new ArrayList<InputColumn<?>>();
        for (InputColumnSourceJob source : _inputColumnSources) {
            InputColumn<?>[] outputColumns = source.getOutput();
            for (InputColumn<?> col : outputColumns) {
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
    public Set<Object> findAllSourceJobs(Object job) {
        final Set<Object> result = new HashSet<Object>();
        findAllSourceJobs(job, result);
        return result;
    }

    private void findAllSourceJobs(Object job, Set<Object> result) {
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
                HasFilterOutcomes source = findOutcomeSource(outcome);
                if (source != null) {
                    final boolean added = result.add(source);
                    if (added) {
                        findAllSourceJobs(source, result);
                    }
                }
            }
        }
    }

    public InputColumnSourceJob findInputColumnSource(InputColumn<?> inputColumn) {
        if (inputColumn instanceof ExpressionBasedInputColumn) {
            return null;
        }
        for (InputColumnSourceJob source : _inputColumnSources) {
            InputColumn<?>[] output = source.getOutput();
            for (InputColumn<?> column : output) {
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

    public Set<Column> findOriginatingColumns(FilterOutcome requirement) {
        HasFilterOutcomes source = findOutcomeSource(requirement);

        HashSet<Column> result = new HashSet<Column>();
        findOriginatingColumnsOfSource(source, result);
        return result;
    }

    public Table findOriginatingTable(FilterOutcome requirement) {
        return findOriginatingTable(requirement, new HashSet<Object>());
    }

    private Table findOriginatingTable(FilterOutcome requirement, Set<Object> resolvedSet) {
        HasFilterOutcomes source = findOutcomeSource(requirement);
        if (!resolvedSet.add(source)) {
            logger.debug(LOG_MESSAGE_RECURSIVE_TRAVERSAL);
            return null;
        }
        return findOriginatingTableOfSource(source, resolvedSet);
    }

    public Table findOriginatingTable(InputColumn<?> inputColumn) {
        return findOriginatingTable(inputColumn, new HashSet<Object>());
    }

    private Table findOriginatingTable(InputColumn<?> inputColumn, Set<Object> resolvedSet) {
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

    private Table findOriginatingTableOfSource(Object source, Set<Object> resolvedSet) {
        final Set<Table> result = new TreeSet<Table>();
        if (source instanceof InputColumnSinkJob) {
            InputColumn<?>[] input = ((InputColumnSinkJob) source).getInput();
            if (input != null) {
                for (InputColumn<?> col : input) {
                    if (col == null) {
                        logger.warn("InputColumn sink had a null-column element!");
                    } else {
                        Table table = findOriginatingTable(col, resolvedSet);
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
            for (FilterOutcome outcome : requirements) {
                Table table = findOriginatingTable(outcome, resolvedSet);
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
        StringBuilder sb = new StringBuilder();
        for (Table table : result) {
            if (sb.length() != 0) {
                sb.append(", ");
            }
            sb.append(table.getName());
        }
        throw new IllegalStateException("Multiple originating tables (" + sb + ") found for source: " + source);
    }

    private void findOriginatingColumnsOfInputColumn(InputColumn<?> inputColumn, Set<Column> result) {
        if (inputColumn == null) {
            return;
        }
        if (inputColumn.isPhysicalColumn()) {
            result.add(inputColumn.getPhysicalColumn());
        } else {
            InputColumnSourceJob source = findInputColumnSource(inputColumn);
            findOriginatingColumnsOfSource(source, result);
        }
    }

    private void findOriginatingColumnsOfOutcome(FilterOutcome requirement, Set<Column> result) {
        final HasFilterOutcomes source = findOutcomeSource(requirement);
        findOriginatingColumnsOfSource(source, result);
    }

    private void findOriginatingColumnsOfSource(Object source, Set<Column> result) {
        if (source == null) {
            return;
        }
        if (source instanceof InputColumnSinkJob) {
            InputColumn<?>[] input = ((InputColumnSinkJob) source).getInput();
            if (input != null) {
                for (InputColumn<?> inputColumn : input) {
                    findOriginatingColumnsOfInputColumn(inputColumn, result);
                }
            }
        }
        if (source instanceof HasComponentRequirement) {
            final HasComponentRequirement hasComponentRequirement = (HasComponentRequirement) source;
            final ComponentRequirement componentRequirement = hasComponentRequirement.getComponentRequirement();
            final Collection<FilterOutcome> requirements = getProcessingDependencies(componentRequirement);
            for (FilterOutcome outcome : requirements) {
                findOriginatingColumnsOfOutcome(outcome, result);
            }
        }
    }

    private Collection<FilterOutcome> getProcessingDependencies(ComponentRequirement componentRequirement) {
        if (componentRequirement == null) {
            return Collections.emptyList();
        }
        final Collection<FilterOutcome> processingDependencies = componentRequirement.getProcessingDependencies();
        if (processingDependencies == null) {
            return Collections.emptyList();
        }
        return processingDependencies;
    }

    public Set<Column> findOriginatingColumns(InputColumn<?> inputColumn) {
        Set<Column> result = new HashSet<Column>();

        // TODO: Detect cyclic dependencies between transformers (A depends on
        // B, B depends on A)

        findOriginatingColumnsOfInputColumn(inputColumn, result);
        return result;
    }
}
