package org.datacleaner.spark;

import java.util.List;

import org.apache.metamodel.csv.CsvConfiguration;
import org.apache.spark.api.java.function.Function;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;

public final class InputRowMapper implements Function<String, InputRow> {
    private static final long serialVersionUID = 1L;

    private final List<InputColumn<?>> _inputColumns;
    private final CsvConfiguration _csvConfiguration;

    public InputRowMapper(final List<InputColumn<?>> inputColumns, final CsvConfiguration csvConfiguration) {
        _inputColumns = inputColumns;
        _csvConfiguration = csvConfiguration;
    }

    @Override
    public InputRow call(String csvLine) throws Exception {
        InputRow inputRow = CsvParser.prepareInputRow(_inputColumns,
                _csvConfiguration, csvLine);
        return inputRow;
    }
}