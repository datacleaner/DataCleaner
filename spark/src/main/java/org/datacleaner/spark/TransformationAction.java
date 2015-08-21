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

import java.util.List;

import org.apache.spark.api.java.function.FlatMapFunction;
import org.datacleaner.api.InputRow;
import org.datacleaner.job.runner.ConsumeRowHandler;
import org.datacleaner.job.runner.ConsumeRowResult;

public final class TransformationAction extends AbstractSparkDataCleanerAction implements FlatMapFunction<InputRow, InputRow> {
    private static final long serialVersionUID = 1L;

    public TransformationAction(final SparkDataCleanerContext sparkDataCleanerContext) {
        super(sparkDataCleanerContext);
    }

    @Override
    public List<InputRow> call(InputRow inputRow) throws Exception {
        ConsumeRowHandler.Configuration configuration = new ConsumeRowHandler.Configuration();
        configuration.includeAnalyzers = false;
        ConsumeRowHandler consumeRowHandler = new ConsumeRowHandler(getAnalysisJob(), getDataCleanerConfiguration(),
                configuration);
        ConsumeRowResult consumeRowResult = consumeRowHandler.consumeRow(inputRow);
        return consumeRowResult.getRows();
    }

}