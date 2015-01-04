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
package org.datacleaner.job;

import java.util.Collection;
import java.util.List;

import org.apache.metamodel.util.BaseObject;
import org.datacleaner.connection.Datastore;
import org.datacleaner.data.InputColumn;

import com.google.common.collect.ImmutableList;

public final class ImmutableAnalysisJob extends BaseObject implements AnalysisJob {

    private final Datastore _datastore;
    private final List<InputColumn<?>> _sourceColumns;
    private final List<TransformerJob> _transformerJobs;
    private final List<AnalyzerJob> _analyzerJobs;
    private final List<FilterJob> _filterJobs;
    private final AnalysisJobMetadata _metadata;

    /**
     * Creates an AnalysisJob
     * 
     * @param datastore
     * @param sourceColumns
     * @param filterJobs
     * @param transformerJobs
     * @param analyzerJobs
     * @deprecated use
     *             {@link #ImmutableAnalysisJob(AnalysisJobMetadata, Datastore, Collection, Collection, Collection, Collection)}
     *             instead
     */
    @Deprecated
    public ImmutableAnalysisJob(Datastore datastore, Collection<? extends InputColumn<?>> sourceColumns,
            Collection<FilterJob> filterJobs, Collection<TransformerJob> transformerJobs,
            Collection<AnalyzerJob> analyzerJobs) {
        this(AnalysisJobMetadata.EMPTY_METADATA, datastore, sourceColumns, filterJobs, transformerJobs, analyzerJobs);
    }

    /**
     * Creates an AnalysisJob
     * 
     * @param metadata
     * @param datastore
     * @param sourceColumns
     * @param filterJobs
     * @param transformerJobs
     * @param analyzerJobs
     */
    public ImmutableAnalysisJob(AnalysisJobMetadata metadata, Datastore datastore,
            Collection<? extends InputColumn<?>> sourceColumns, Collection<FilterJob> filterJobs,
            Collection<TransformerJob> transformerJobs, Collection<AnalyzerJob> analyzerJobs) {
        _metadata = metadata;
        _datastore = datastore;
        _sourceColumns = ImmutableList.copyOf(sourceColumns);
        _transformerJobs = ImmutableList.copyOf(transformerJobs);
        _analyzerJobs = ImmutableList.copyOf(analyzerJobs);
        _filterJobs = ImmutableList.copyOf(filterJobs);
    }

    @Override
    protected void decorateIdentity(List<Object> identifiers) {
        identifiers.add(_datastore);
        identifiers.add(_sourceColumns);
        identifiers.add(_transformerJobs);
        identifiers.add(_analyzerJobs);
        identifiers.add(_filterJobs);
    }

    @Override
    public AnalysisJobMetadata getMetadata() {
        if (_metadata == null) {
            return AnalysisJobMetadata.EMPTY_METADATA;
        }
        return _metadata;
    }

    @Override
    public Datastore getDatastore() {
        return _datastore;
    }

    @Override
    public List<InputColumn<?>> getSourceColumns() {
        return _sourceColumns;
    }

    @Override
    public List<TransformerJob> getTransformerJobs() {
        return _transformerJobs;
    }

    @Override
    public List<AnalyzerJob> getAnalyzerJobs() {
        return _analyzerJobs;
    }

    @Override
    public List<FilterJob> getFilterJobs() {
        return _filterJobs;
    }

    @Override
    public String toString() {
        return "ImmutableAnalysisJob[sourceColumns=" + _sourceColumns.size() + ",filterJobs=" + _filterJobs.size()
                + ",transformerJobs=" + _transformerJobs.size() + ",analyzerJobs=" + _analyzerJobs.size() + "]";
    }

}
