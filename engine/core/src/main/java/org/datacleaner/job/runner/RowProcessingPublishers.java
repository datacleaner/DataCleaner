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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.metamodel.MetaModelHelper;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.CollectionUtils;
import org.datacleaner.api.Analyzer;
import org.datacleaner.api.Filter;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.Transformer;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.AnalyzerJob;
import org.datacleaner.job.ComponentJob;
import org.datacleaner.job.ComponentRequirement;
import org.datacleaner.job.FilterJob;
import org.datacleaner.job.FilterOutcome;
import org.datacleaner.job.OutputDataStreamJob;
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
    private final Map<RowProcessingStream, RowProcessingPublisher> _rowProcessingPublishers;

    public RowProcessingPublishers(AnalysisJob analysisJob, AnalysisListener analysisListener, TaskRunner taskRunner,
            LifeCycleHelper lifeCycleHelper, SourceColumnFinder sourceColumnFinder) {
        _analysisJob = analysisJob;
        _analysisListener = analysisListener;
        _taskRunner = taskRunner;
        _lifeCycleHelper = lifeCycleHelper;

        // note that insertion and extraction order consistency is important
        // since OutputDataStreamJobs should be initialized after their parent
        // jobs. For this reason we use a LinkedHashMap and not a regular
        // HashMap.
        _rowProcessingPublishers = new LinkedHashMap<RowProcessingStream, RowProcessingPublisher>();

        registerAll();
    }

    private void registerAll() {
        registerJob(_analysisJob);

        for (RowProcessingPublisher publisher : _rowProcessingPublishers.values()) {
            publisher.onAllConsumersRegistered();
        }
    }

    private void registerJob(final AnalysisJob job) {
        final SourceColumnFinder sourceColumnFinder = new SourceColumnFinder();
        sourceColumnFinder.addSources(job);

        for (ComponentJob componentJob : getAllComponents(job)) {
            registerRowProcessingPublishers(sourceColumnFinder, job, componentJob);
        }
    }

    private void registerJob(final AnalysisJob job, final RowProcessingStream dataStream,
            final RowProcessingPublisher parentPublisher) {
        final SourceColumnFinder sourceColumnFinder = new SourceColumnFinder();
        sourceColumnFinder.addSources(job);

        for (ComponentJob componentJob : getAllComponents(job)) {
            registerRowProcessingPublishers(sourceColumnFinder, job, dataStream, componentJob, parentPublisher);
        }
    }

    public static Collection<ComponentJob> getAllComponents(AnalysisJob job) {
        return CollectionUtils.<ComponentJob> concat(false, job.getFilterJobs(), job.getTransformerJobs(),
                job.getAnalyzerJobs());
    }

    private void registerOutputDataStream(final RowProcessingPublisher parentPublisher,
            final RowProcessingConsumer publishingConsumer, OutputDataStreamJob outputDataStreamJob) {

        // first initialize the nested job like any other set of components
        final RowProcessingStream dataStream = RowProcessingStream.ofOutputDataStream(outputDataStreamJob);

        registerJob(outputDataStreamJob.getJob(), dataStream, parentPublisher);

        // then we wire the publisher for this output data stream to a
        // OutputRowCollector which will get injected via the
        // HasOutputDataStreams interface.
        final RowProcessingPublisher publisherForOutputDataStream = getRowProcessingPublisher(dataStream);

        publishingConsumer.registerOutputDataStream(outputDataStreamJob, publisherForOutputDataStream);
    }

    public Column[] getPhysicalColumns(SourceColumnFinder sourceColumnFinder, ComponentJob componentJob) {
        final Set<Column> physicalColumns = new HashSet<Column>();

        final InputColumn<?>[] inputColumns = componentJob.getInput();
        for (InputColumn<?> inputColumn : inputColumns) {
            physicalColumns.addAll(sourceColumnFinder.findOriginatingColumns(inputColumn));
        }
        final ComponentRequirement requirement = componentJob.getComponentRequirement();
        if (requirement != null) {
            for (FilterOutcome filterOutcome : requirement.getProcessingDependencies()) {
                physicalColumns.addAll(sourceColumnFinder.findOriginatingColumns(filterOutcome));
            }
        }

        final Column[] physicalColumnsArray = physicalColumns.toArray(new Column[physicalColumns.size()]);
        return physicalColumnsArray;
    }

    @Deprecated
    public Table[] getTables(ComponentJob componentJob) {
        RowProcessingPublisher publisher = getRowProcessingPublisher(componentJob);
        return getTables(publisher.getSourceColumnFinder(), componentJob, null);
    }
    
    private RowProcessingPublisher getRowProcessingPublisher(ComponentJob componentJob) {
        final Collection<RowProcessingPublisher> publishers = _rowProcessingPublishers.values();
        for (RowProcessingPublisher rowProcessingPublisher : publishers) {
            if (rowProcessingPublisher.containsComponentJob(componentJob)) {
                return rowProcessingPublisher;
            }
        }
        return null;
    }

    
    public Table[] getTables(SourceColumnFinder sourceColumnFinder, ComponentJob componentJob) {
        return getTables(sourceColumnFinder, componentJob, null);
    }

    public Table[] getTables(SourceColumnFinder sourceColumnFinder, ComponentJob componentJob, Column[] physicalColumns) {
        if (physicalColumns == null) {
            physicalColumns = getPhysicalColumns(sourceColumnFinder, componentJob);
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

    private void registerRowProcessingPublishers(final SourceColumnFinder sourceColumnFinder, final AnalysisJob job,
            final ComponentJob componentJob) {
        final Column[] physicalColumns = getPhysicalColumns(sourceColumnFinder, componentJob);
        final Table[] tables = getTables(sourceColumnFinder, componentJob, physicalColumns);

        for (Table table : tables) {
            final RowProcessingStream dataStream = RowProcessingStream.ofSourceTable(job, table);
            registerRowProcessingPublishers(sourceColumnFinder, job, dataStream, componentJob, null);
        }
    }

    private void registerRowProcessingPublishers(final SourceColumnFinder sourceColumnFinder, final AnalysisJob job,
            final RowProcessingStream dataStream, final ComponentJob componentJob,
            final RowProcessingPublisher parentPublisher) {
        RowProcessingPublisher rowPublisher = _rowProcessingPublishers.get(dataStream);
        if (rowPublisher == null) {
            if (parentPublisher == null) {
                rowPublisher = new RowProcessingPublisher(this, dataStream, _taskRunner);
            } else {
                rowPublisher = new RowProcessingPublisher(parentPublisher, dataStream);
            }
            rowPublisher.addPrimaryKeysIfSourced();
            _rowProcessingPublishers.put(dataStream, rowPublisher);
        }

        // register the physical columns needed by this job
        final Column[] physicalColumns = getPhysicalColumns(sourceColumnFinder, componentJob);
        rowPublisher.addPhysicalColumns(physicalColumns);

        // find which input columns (both physical or virtual) are needed by
        // this per-table instance
        final InputColumn<?>[] localInputColumns = getLocalInputColumns(sourceColumnFinder, dataStream.getTable(),
                componentJob.getInput());

        if (componentJob instanceof AnalyzerJob) {
            final AnalyzerJob analyzerJob = (AnalyzerJob) componentJob;
            final Analyzer<?> analyzer = analyzerJob.getDescriptor().newInstance();
            rowPublisher.registerAnalyzer(analyzer, analyzerJob, localInputColumns);
        } else if (componentJob instanceof TransformerJob) {
            final TransformerJob transformerJob = (TransformerJob) componentJob;
            final Transformer transformer = transformerJob.getDescriptor().newInstance();
            rowPublisher.registerTransformer(transformer, transformerJob, localInputColumns);
        } else if (componentJob instanceof FilterJob) {
            final FilterJob filterJob = (FilterJob) componentJob;
            final Filter<?> filter = filterJob.getDescriptor().newInstance();
            rowPublisher.registerFilter(filter, filterJob, localInputColumns);
        } else {
            throw new UnsupportedOperationException("Unsupported component job type: " + componentJob);
        }

        final OutputDataStreamJob[] outputDataStreamJobs = componentJob.getOutputDataStreamJobs();
        if (outputDataStreamJobs.length > 0) {
            final RowProcessingConsumer consumer = rowPublisher.getConsumer(componentJob);
            for (final OutputDataStreamJob outputDataStreamJob : outputDataStreamJobs) {
                registerOutputDataStream(rowPublisher, consumer, outputDataStreamJob);
            }
        }
    }

    private InputColumn<?>[] getLocalInputColumns(final SourceColumnFinder sourceColumnFinder, final Table table,
            final InputColumn<?>[] inputColumns) {
        if (table == null || inputColumns == null || inputColumns.length == 0) {
            return new InputColumn<?>[0];
        }
        List<InputColumn<?>> result = new ArrayList<InputColumn<?>>();
        for (InputColumn<?> inputColumn : inputColumns) {
            Set<Column> sourcePhysicalColumns = sourceColumnFinder.findOriginatingColumns(inputColumn);
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

    public RowProcessingPublisher getRowProcessingPublisher(RowProcessingStream stream) {
        return _rowProcessingPublishers.get(stream);
    }

    public RowProcessingStream getStream(Table table) {
        final Set<RowProcessingStream> dataStreams = _rowProcessingPublishers.keySet();
        for (RowProcessingStream stream : dataStreams) {
            // first try with object equality because tables may be equal in
            // some corner cases
            if (stream.getTable() == table) {
                return stream;
            }
        }

        for (RowProcessingStream stream : dataStreams) {
            if (table.equals(stream.getTable())) {
                return stream;
            }
        }

        return null;
    }

    /**
     * 
     * @param table
     * @return
     * 
     * @deprecated use {@link #getRowProcessingPublisher(RowProcessingStream)}
     *             instead
     */
    @Deprecated
    public RowProcessingPublisher getRowProcessingPublisher(Table table) {
        RowProcessingStream stream = getStream(table);
        return getRowProcessingPublisher(stream);
    }

    public Collection<RowProcessingPublisher> getRowProcessingPublishers() {
        return _rowProcessingPublishers.values();
    }

    public RowProcessingStream[] getStreams() {
        final Set<RowProcessingStream> streams = _rowProcessingPublishers.keySet();
        return streams.toArray(new RowProcessingStream[streams.size()]);
    }

    /**
     * 
     * @return
     * 
     * @deprecated use {@link #getStreams()} instead
     */
    @Deprecated
    public Table[] getTables() {
        final RowProcessingStream[] streams = getStreams();
        final Table[] tables = new Table[streams.length];
        for (int i = 0; i < tables.length; i++) {
            tables[i] = streams[i].getTable();
        }
        return tables;
    }

    public AnalysisJobMetrics getAnalysisJobMetrics() {
        return new AnalysisJobMetricsImpl(_analysisJob, this);
    }

    protected AnalysisListener getAnalysisListener() {
        return _analysisListener;
    }

    protected LifeCycleHelper getLifeCycleHelper() {
        return _lifeCycleHelper;
    }
}
