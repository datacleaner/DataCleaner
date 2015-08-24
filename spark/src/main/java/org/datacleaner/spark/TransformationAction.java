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
package org.datacleaner.spark;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.spark.api.java.function.FlatMapFunction;
import org.datacleaner.api.InputRow;
import org.datacleaner.job.runner.ConsumeRowHandler;
import org.datacleaner.job.runner.ConsumeRowResult;

public final class TransformationAction extends AbstractSparkDataCleanerAction implements FlatMapFunction<Iterator<InputRow>, InputRow> {
    
    private static final long serialVersionUID = 1L;
    
    private transient ConsumeRowHandler _consumeRowHandler;

    public TransformationAction(final SparkDataCleanerContext sparkDataCleanerContext) {
        super(sparkDataCleanerContext);
    }

    @Override
    public List<InputRow> call(Iterator<InputRow> inputRowIterator) throws Exception {
        List<InputRow> transformedInputRows = new ArrayList<>(); 
        while (inputRowIterator.hasNext()) {
            InputRow inputRow = inputRowIterator.next();
            ConsumeRowResult consumeRowResult = getConsumeRowHandler().consumeRow(inputRow);
            transformedInputRows.addAll(consumeRowResult.getRows());
        }
        return transformedInputRows;
    }
    
    private ConsumeRowHandler getConsumeRowHandler() {
        if (_consumeRowHandler == null) {
            ConsumeRowHandler.Configuration configuration = new ConsumeRowHandler.Configuration();
            configuration.includeAnalyzers = false;
            _consumeRowHandler = new ConsumeRowHandler(getAnalysisJob(), getDataCleanerConfiguration(),
                    configuration);
        }
        return _consumeRowHandler;
    }

}