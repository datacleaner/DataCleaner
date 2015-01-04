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
package org.datacleaner.job.runner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.metamodel.MetaModelHelper;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;
import org.datacleaner.beans.api.Analyzer;
import org.datacleaner.beans.api.Filter;
import org.datacleaner.beans.api.Transformer;
import org.datacleaner.connection.Datastore;
import org.datacleaner.data.InputColumn;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.AnalyzerJob;
import org.datacleaner.job.ComponentRequirement;
import org.datacleaner.job.ConfigurableBeanJob;
import org.datacleaner.job.FilterJob;
import org.datacleaner.job.FilterOutcome;
import org.datacleaner.job.TransformerJob;
import org.datacleaner.job.concurrent.TaskRunner;
import org.datacleaner.lifecycle.LifeCycleHelper;
import org.datacleaner.util.SourceColumnFinder;

/**
 * Class which partitions a single {@link AnalysisJob}'s components into
 * {@link RowProcessingPublisher}s.
 */
public final class RowProcessingPublishers {

    private final AnalysisJob _analysisJob;
    private final AnalysisListener _analysisListener;
    private final TaskRunner _taskRunner;
    private final LifeCycleHelper _lifeCycleHelper;
    private final SourceColumnFinder _sourceColumnFinder;
    private final Map<Table, RowProcessingPublisher> _rowProcessingPublishers;

    public RowProcessingPublishers(AnalysisJob analysisJob, AnalysisListener analysisListener, TaskRunner taskRunner,
            LifeCycleHelper lifeCycleHelper, SourceColumnFinder sourceColumnFinder) {
        _analysisJob = analysisJob;
        _analysisListener = analysisListener;
        _taskRunner = taskRunner;
        _lifeCycleHelper = lifeCycleHelper;

        if (sourceColumnFinder == null) {
            _sourceColumnFinder = new SourceColumnFinder();
            _sourceColumnFinder.addSources(_analysisJob);
        } else {
            _sourceColumnFinder = sourceColumnFinder;
        }

        _rowProcessingPublishers = new HashMap<Table, RowProcessingPublisher>();

        initialize();
    }

    private void initialize() {
        for (FilterJob filterJob : _analysisJob.getFilterJobs()) {
            registerRowProcessingPublishers(filterJob);
        }
        for (TransformerJob transformerJob : _analysisJob.getTransformerJobs()) {
            registerRowProcessingPublishers(transformerJob);
        }
        for (AnalyzerJob analyzerJob : _analysisJob.getAnalyzerJobs()) {
            registerRowProcessingPublishers(analyzerJob);
        }

        for (RowProcessingPublisher publisher : _rowProcessingPublishers.values()) {
            publisher.initialize();
        }
    }

    public Column[] getPhysicalColumns(ConfigurableBeanJob<?> componentJob) {
        final Set<Column> physicalColumns = new HashSet<Column>();

        final InputColumn<?>[] inputColumns = componentJob.getInput();
        for (InputColumn<?> inputColumn : inputColumns) {
            physicalColumns.addAll(_sourceColumnFinder.findOriginatingColumns(inputColumn));
        }
        final ComponentRequirement requirement = componentJob.getComponentRequirement();
        if (requirement != null) {
            for (FilterOutcome filterOutcome : requirement.getProcessingDependencies()) {
                physicalColumns.addAll(_sourceColumnFinder.findOriginatingColumns(filterOutcome));
            }
        }

        final Column[] physicalColumnsArray = physicalColumns.toArray(new Column[physicalColumns.size()]);
        return physicalColumnsArray;
    }

    public Table[] getTables(ConfigurableBeanJob<?> componentJob) {
        return getTables(componentJob, null);
    }

    public Table[] getTables(ConfigurableBeanJob<?> componentJob, Column[] physicalColumns) {
        if (physicalColumns == null) {
            physicalColumns = getPhysicalColumns(componentJob);
        }
        final Table[] tables;
        if (physicalColumns.length == 0) {
            // if not dependent on any specific tables, make component available
            // for all tables
            Set<Table> allTables = new HashSet<Table>();
            Collection<InputColumn<?>> allSourceColumns = _analysisJob.getSourceColumns();
            for (InputColumn<?> inputColumn : allSourceColumns) {
                allTables.add(inputColumn.getPhysicalColumn().getTable());
            }
            tables = allTables.toArray(new Table[allTables.size()]);
        } else {
            tables = MetaModelHelper.getTables(physicalColumns);
        }

        if (tables.length > 1) {
            throw new IllegalStateException("Component has input columns from multiple tables: " + componentJob);
        }

        if (tables.length == 0) {
            throw new IllegalStateException("Component has no dependent tables: " + componentJob);
        }

        return tables;
    }

    private void registerRowProcessingPublishers(ConfigurableBeanJob<?> componentJob) {
        final Column[] physicalColumns = getPhysicalColumns(componentJob);
        final Table[] tables = getTables(componentJob, physicalColumns);

        for (Table table : tables) {
            RowProcessingPublisher rowPublisher = _rowProcessingPublishers.get(table);
            if (rowPublisher == null) {
                rowPublisher = new RowProcessingPublisher(this, table);
                rowPublisher.addPrimaryKeysIfSourced();
                _rowProcessingPublishers.put(table, rowPublisher);
            }

            // register the physical columns needed by this job
            rowPublisher.addPhysicalColumns(physicalColumns);

            // find which input columns (both physical or virtual) are needed by
            // this per-table instance
            final InputColumn<?>[] localInputColumns = getLocalInputColumns(table, componentJob.getInput());

            if (componentJob instanceof AnalyzerJob) {
                final AnalyzerJob analyzerJob = (AnalyzerJob) componentJob;
                final Analyzer<?> analyzer = analyzerJob.getDescriptor().newInstance();
                rowPublisher.addAnalyzerBean(analyzer, analyzerJob, localInputColumns);
            } else if (componentJob instanceof TransformerJob) {
                final TransformerJob transformerJob = (TransformerJob) componentJob;
                final Transformer<?> transformer = transformerJob.getDescriptor().newInstance();
                rowPublisher.addTransformerBean(transformer, transformerJob, localInputColumns);
            } else if (componentJob instanceof FilterJob) {
                final FilterJob filterJob = (FilterJob) componentJob;
                final Filter<?> filter = filterJob.getDescriptor().newInstance();
                rowPublisher.addFilterBean(filter, filterJob, localInputColumns);
            } else {
                throw new UnsupportedOperationException("Unsupported component job type: " + componentJob);
            }
        }
    }

    private InputColumn<?>[] getLocalInputColumns(Table table, InputColumn<?>[] inputColumns) {
        if (table == null || inputColumns == null || inputColumns.length == 0) {
            return new InputColumn<?>[0];
        }
        List<InputColumn<?>> result = new ArrayList<InputColumn<?>>();
        for (InputColumn<?> inputColumn : inputColumns) {
            Set<Column> sourcePhysicalColumns = _sourceColumnFinder.findOriginatingColumns(inputColumn);
            for (Column physicalColumn : sourcePhysicalColumns) {
                if (table.equals(physicalColumn.getTable())) {
                    result.add(inputColumn);
                    break;
                }
            }
        }
        return result.toArray(new InputColumn<?>[result.size()]);
    }

    public int size() {
        return _rowProcessingPublishers.size();
    }

    public RowProcessingPublisher getRowProcessingPublisher(Table table) {
        return _rowProcessingPublishers.get(table);
    }

    public Collection<RowProcessingPublisher> getRowProcessingPublishers() {
        return _rowProcessingPublishers.values();
    }

    public Table[] getTables() {
        Set<Table> tables = _rowProcessingPublishers.keySet();
        return tables.toArray(new Table[tables.size()]);
    }

    public AnalysisJobMetrics getAnalysisJobMetrics() {
        return new AnalysisJobMetricsImpl(_analysisJob, this);
    }

    public SourceColumnFinder getSourceColumnFinder() {
        return _sourceColumnFinder;
    }

    protected AnalysisJob getAnalysisJob() {
        return _analysisJob;
    }

    protected AnalysisListener getAnalysisListener() {
        return _analysisListener;
    }

    protected LifeCycleHelper getLifeCycleHelper() {
        return _lifeCycleHelper;
    }

    protected TaskRunner getTaskRunner() {
        return _taskRunner;
    }

    public Datastore getDatastore() {
        return _analysisJob.getDatastore();
    }
}
