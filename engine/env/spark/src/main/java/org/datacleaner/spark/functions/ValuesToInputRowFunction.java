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
package org.datacleaner.spark.functions;

import java.util.List;

import org.apache.metamodel.schema.Table;
import org.apache.spark.api.java.function.Function;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.data.MockInputRow;
import org.datacleaner.spark.SparkJobContext;

import scala.Tuple2;

/**
 * Mapper function that changes takes Object arrays representing physical
 * records into the format of an {@link InputRow}.
 * 
 * Currently this is implemented very strictly by just investigating the column
 * indices of the job's source columns.
 * 
 * It is assumed that the job is based on a single source {@link Table}.
 */
public class ValuesToInputRowFunction implements Function<Tuple2<Object[], Long>, InputRow> {

    private static final long serialVersionUID = 1L;

    private final SparkJobContext _sparkJobContext;

    public ValuesToInputRowFunction(SparkJobContext sparkJobContext) {
        _sparkJobContext = sparkJobContext;
    }

    @Override
    public InputRow call(Tuple2<Object[], Long> tuple) throws Exception {
        final Object[] values = tuple._1;
        final Long rowNumber = tuple._2;
        
        final MockInputRow inputRow = new MockInputRow(rowNumber.intValue());
        final List<InputColumn<?>> sourceColumns = _sparkJobContext.getAnalysisJob().getSourceColumns();
        for (InputColumn<?> sourceColumn : sourceColumns) {
            assert sourceColumn.isPhysicalColumn();
            final int columnIndex = sourceColumn.getPhysicalColumn().getColumnNumber();
            final Object value = values[columnIndex];
            inputRow.put(sourceColumn, value);
        }
        return inputRow;
    }

}
