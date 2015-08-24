package org.datacleaner.spark;

import java.util.List;

import org.apache.metamodel.schema.Table;
import org.apache.spark.api.java.function.Function;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.data.MockInputRow;

/**
 * Mapper function that changes takes Object arrays representing physical
 * records into the format of an {@link InputRow}.
 * 
 * Currently this is implemented very strictly by just investigating the column
 * indices of the job's source columns.
 * 
 * It is assumed that the job is based on a single source {@link Table}.
 */
public class ValuesToInputRowMapper implements Function<Object[], InputRow> {

    private static final long serialVersionUID = 1L;

    private final SparkJobContext _sparkJobContext;

    public ValuesToInputRowMapper(SparkJobContext sparkJobContext) {
        _sparkJobContext = sparkJobContext;
    }

    @Override
    public InputRow call(Object[] values) throws Exception {
        final MockInputRow inputRow = new MockInputRow();
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
