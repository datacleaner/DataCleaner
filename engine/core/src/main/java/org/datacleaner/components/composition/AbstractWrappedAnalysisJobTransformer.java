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
package org.datacleaner.components.composition;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;

import org.datacleaner.api.Close;
import org.datacleaner.api.Initialize;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.OutputRowCollector;
import org.datacleaner.api.Provided;
import org.datacleaner.api.Transformer;
import org.datacleaner.configuration.AnalyzerBeansConfiguration;
import org.datacleaner.data.MockInputRow;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.runner.ConsumeRowHandler;

/**
 * An abstract transformer that wraps another {@link AnalysisJob}'s
 * transformation section and applies it as a single transformation.
 * 
 * This class is abstract since the logic of how the wrapped {@link AnalysisJob}
 * is configured/set might differ. Some implementations might simply have a
 * hardcoded job configured, others might dynamically load it from a file.
 */
public abstract class AbstractWrappedAnalysisJobTransformer implements Transformer {

    @Inject
    @Provided
    AnalyzerBeansConfiguration _analyzerBeansConfiguration;

    @Inject
    @Provided
    OutputRowCollector _outputRowCollector;

    private AnalysisJob _wrappedAnalysisJob;
    private ConsumeRowHandler _consumeRowHandler;
    private Map<InputColumn<?>, InputColumn<?>> _inputColumnConversion;
    private List<InputColumn<?>> _outputColumns;

    /**
     * Provides the analysis job to wrap.
     * 
     * @return
     */
    protected abstract AnalysisJob createWrappedAnalysisJob();

    /**
     * Provides the conversion map for input columns. Keys are expected to be
     * columns of the parent/owning job, and values are expected to be columns
     * of the embedded/wrapped job.
     * 
     * @param wrappedAnalysisJob
     * 
     * @return
     */
    protected abstract Map<InputColumn<?>, InputColumn<?>> getInputColumnConversion(AnalysisJob wrappedAnalysisJob);

    @Initialize
    public void init() {
        if (!reInitialize(_wrappedAnalysisJob, _outputColumns)) {
            return;
        }

        _wrappedAnalysisJob = createWrappedAnalysisJob();
        if (_wrappedAnalysisJob == null) {
            throw new IllegalStateException("Wrapped AnalysisJob cannot be null");
        }

        final ConsumeRowHandler.Configuration configuration = new ConsumeRowHandler.Configuration();
        configuration.includeAnalyzers = false;

        _consumeRowHandler = new ConsumeRowHandler(_wrappedAnalysisJob, _analyzerBeansConfiguration, configuration);
        _inputColumnConversion = getInputColumnConversion(_wrappedAnalysisJob);
        _outputColumns = _consumeRowHandler.getOutputColumns();
    }

    /**
     * Determines if the transformer should reinitialize it's
     * {@link ConsumeRowHandler}, output columns etc. based on a set of existing
     * values.
     * 
     * The default implementation returns false when non-null values are
     * available
     * 
     * @param wrappedAnalysisJob
     * @param outputColumns
     * @return
     */
    protected boolean reInitialize(AnalysisJob wrappedAnalysisJob, List<InputColumn<?>> outputColumns) {
        if (wrappedAnalysisJob != null && outputColumns != null && !outputColumns.isEmpty()) {
            return false;
        }
        return true;
    }

    @Close
    public void close() {
        _consumeRowHandler = null;
        _inputColumnConversion = null;
        _outputColumns = null;
    }

    @Override
    public OutputColumns getOutputColumns() {
        init();
        
        final int size = _outputColumns.size();
        final String[] names = new String[size];
        final Class<?>[] types = new Class[size];
        for (int i = 0; i < size; i++) {
            InputColumn<?> outputColumn = _outputColumns.get(0);
            names[i] = outputColumn.getName();
            types[i] = outputColumn.getDataType();
        }
        return new OutputColumns(names, types);
    }

    @Override
    public Object[] transform(final InputRow parentInputRow) {
        final MockInputRow wrappedInputRow = new MockInputRow(parentInputRow.getId());
        final Set<Entry<InputColumn<?>, InputColumn<?>>> conversionEntries = _inputColumnConversion.entrySet();
        for (final Entry<InputColumn<?>, InputColumn<?>> conversionEntry : conversionEntries) {
            final InputColumn<?> parentColumn = conversionEntry.getKey();
            final Object value = parentInputRow.getValue(parentColumn);
            final InputColumn<?> wrappedColumn = conversionEntry.getValue();
            wrappedInputRow.put(wrappedColumn, value);
        }

        final List<InputRow> outputRows = _consumeRowHandler.consumeRow(wrappedInputRow).getRows();
        for (InputRow wrappedOutputRow : outputRows) {
            final Object[] outputValues = convertToOutputValues(wrappedOutputRow);
            _outputRowCollector.putValues(outputValues);
        }
        return null;
    }

    private Object[] convertToOutputValues(InputRow wrappedOutputRow) {
        Object[] result = new Object[_outputColumns.size()];
        for (int i = 0; i < result.length; i++) {
            InputColumn<?> outputColumn = _outputColumns.get(i);
            Object value = wrappedOutputRow.getValue(outputColumn);
            result[i] = value;
        }
        return result;
    }
    
    public AnalyzerBeansConfiguration getAnalyzerBeansConfiguration() {
        return _analyzerBeansConfiguration;
    }
    
    public OutputRowCollector getOutputRowCollector() {
        return _outputRowCollector;
    }
}
