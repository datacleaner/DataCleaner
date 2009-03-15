/**
 *  This file is part of DataCleaner.
 *
 *  DataCleaner is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DataCleaner is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DataCleaner.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.eobjects.datacleaner.export;

import java.io.PrintWriter;
import java.util.List;

import dk.eobjects.datacleaner.profiler.IMatrix;
import dk.eobjects.datacleaner.profiler.IProfileResult;
import dk.eobjects.datacleaner.validator.IValidationRuleResult;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Table;

public class CsvResultExporter implements IResultExporter {

	public boolean isCollectiveResultsCapable() {
		return false;
	}

	public void writeProfileResultHeader(PrintWriter writer) {
		writer.print("\"table\",\"column\",\"measure\",\"value\"\n");
	}

	public void writeProfileResultFooter(PrintWriter writer) {
	}

	public void writeProfileResult(Table table, IProfileResult result,
			PrintWriter writer) {
		if (result.getError() != null) {
			result.getError().printStackTrace(writer);
		} else {
			IMatrix[] matrices = result.getMatrices();
			for (IMatrix matrix : matrices) {
				String[] rowNames = matrix.getRowNames();
				String[] columnNames = matrix.getColumnNames();
				for (String columnName : columnNames) {
					for (String rowName : rowNames) {
						Object value = matrix.getValue(rowName, columnName)
								.getValue();
						writer.print("\"");
						writer.print(table.getName());
						writer.print("\",\"");
						writer.print(columnName);
						writer.print("\",\"");
						writer.print(rowName);
						writer.print("\",\"");
						writer.print(safePrint(value));
						writer.print("\"\n");
					}
				}
			}
		}
	}

	private String safePrint(Object value) {
		if (value == null) {
			return "null";
		} else {
			String valueString = value.toString();
			if (valueString.indexOf('\"') != -1) {
				// TODO: Consider if there is a more gentle way
				// to ensure the CSV format (perhaps replace
				// with &quot;)
				valueString = valueString.replace('\"', '\'');
			}
			return valueString;
		}
	}

	public void writeValidationRuleResultHeader(PrintWriter writer) {
	}

	public void writeValidationRuleResultFooter(PrintWriter writer) {
	}

	public void writeValidationRuleResult(Table table,
			IValidationRuleResult result, PrintWriter writer) {
		if (result.getError() != null) {
			result.getError().printStackTrace(writer);
		} else {
			Column[] columns = result.getEvaluatedColumns();
			for (int i = 0; i < columns.length; i++) {
				if (i != 0) {
					writer.print(',');
				}
				writer.print("\"");
				writer.write(columns[i].getName());
				writer.print("\"");
			}
			writer.write('\n');
			List<Row> rows = result.getUnvalidatedRows();
			for (Row row : rows) {
				Object[] values = row.getValues();
				for (int i = 0; i < values.length; i++) {
					if (i != 0) {
						writer.print(',');
					}
					writer.print("\"");
					writer.print(safePrint(values[i]));
					writer.print("\"");
				}
				writer.write('\n');
			}
		}
	}
}
