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
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import dk.eobjects.datacleaner.profiler.IMatrix;
import dk.eobjects.datacleaner.profiler.IProfileDescriptor;
import dk.eobjects.datacleaner.profiler.IProfileResult;
import dk.eobjects.datacleaner.profiler.MatrixValue;
import dk.eobjects.datacleaner.validator.IValidationRuleDescriptor;
import dk.eobjects.datacleaner.validator.IValidationRuleResult;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.query.SelectItem;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Schema;
import dk.eobjects.metamodel.schema.Table;
import dk.eobjects.metamodel.util.ToStringComparator;

public class XmlResultExporter implements IResultExporter {

	public boolean isCollectiveResultsCapable() {
		return true;
	}

	private void writeXmlHeader(PrintWriter writer) {
		writer.print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
	}

	private void writeHeader(PrintWriter writer) {
		writeXmlHeader(writer);
		writer
				.print("<datacleanerResults xmlns=\"http://datacleaner.eobjects.org/1.5/results\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n");
	}

	private void writeFooter(PrintWriter writer) {
		writer.print("</datacleanerResults>");
		writer.flush();
	}

	public void writeProfileResultHeader(PrintWriter writer) {
		writeHeader(writer);
	}

	public void writeProfileResultFooter(PrintWriter writer) {
		writeFooter(writer);
	}

	public void writeProfileResult(Table table, IProfileResult result,
			PrintWriter writer) {
		writer.print("<profileResult>\n");

		writer.print(" <jobConfiguration>\n");
		IProfileDescriptor descriptor = result.getDescriptor();
		writer.print("  <profile displayName=\""
				+ safePrint(descriptor.getDisplayName()) + "\" className=\""
				+ safePrint(descriptor.getProfileClass()) + "\" />\n");
		writeTable("  ", table, writer);

		writer.print("  <profiledColumns>\n");
		Column[] columns = result.getColumns();
		for (Column column : columns) {
			writeColumn("   ", column, writer);
		}
		writer.print("  </profiledColumns>\n");

		Map<String, String> properties = result.getProperties();
		writeProperties("  ", writer, properties);
		writer.print(" </jobConfiguration>\n");

		writer.print(" <result>\n");
		Exception error = result.getError();
		if (error == null) {
			IMatrix[] matrices = result.getMatrices();
			for (IMatrix matrix : matrices) {
				writer.print("  <matrix>\n");
				String[] columnNames = matrix.getColumnNames();
				String[] rowNames = matrix.getRowNames();
				for (int i = 0; i < columnNames.length; i++) {
					String columnName = columnNames[i];
					for (int j = 0; j < rowNames.length; j++) {
						String rowName = rowNames[j];
						MatrixValue value = matrix
								.getValue(rowName, columnName);
						writer.print("   <measure columnName=\"" + columnName
								+ "\" columnIndex=\"" + i + "\" rowName=\""
								+ rowName + "\" rowIndex=\"" + j
								+ "\" value=\"" + safePrint(value.getValue())
								+ "\" />\n");
					}
				}
				writer.print("  </matrix>\n");
				writer.flush();
			}
		} else {
			writer.print("  <error>\n");
			StringWriter stringWriter = new StringWriter();
			error.printStackTrace(new PrintWriter(stringWriter));
			writer.print(safePrint(stringWriter.toString()));
			writer.print("  </error>\n");
		}
		writer.print(" </result>\n");
		writer.print("</profileResult>\n");
		writer.flush();
	}

	private void writeProperties(String prefix, PrintWriter writer,
			Map<String, String> properties) {
		writer.print(prefix + "<properties>\n");

		// Sort the properties by key to make result deterministic
		ArrayList<Entry<String, String>> entries = new ArrayList<Entry<String, String>>(
				properties.entrySet());
		Collections.sort(entries, new Comparator<Entry<String, String>>() {
			public int compare(Entry<String, String> o1,
					Entry<String, String> o2) {
				String key1 = o1.getKey();
				String key2 = o2.getKey();
				return ToStringComparator.getComparator().compare(key1, key2);
			}
		});

		for (Entry<String, String> entry : entries) {
			writer.print(prefix + " <property name=\""
					+ safePrint(entry.getKey()) + "\" value=\""
					+ safePrint(entry.getValue()) + "\" />\n");
		}
		writer.print(prefix + "</properties>\n");
	}

	public void writeValidationRuleResultHeader(PrintWriter writer) {
		writeHeader(writer);
	}

	public void writeValidationRuleResultFooter(PrintWriter writer) {
		writeFooter(writer);
	}

	public void writeValidationRuleResult(Table table,
			IValidationRuleResult result, PrintWriter writer) {
		writer.print("<validationRuleResult>\n");
		writer.print(" <jobConfiguration>\n");
		IValidationRuleDescriptor descriptor = result.getDescriptor();
		writer.print("  <validationRule displayName=\""
				+ safePrint(descriptor.getDisplayName()) + "\" className=\""
				+ safePrint(descriptor.getValidationRuleClass()) + "\" />\n");
		writeTable("  ", table, writer);

		writer.print("  <evaluatedColumns>\n");
		Column[] columns = result.getEvaluatedColumns();
		for (Column column : columns) {
			writeColumn("   ", column, writer);
		}
		writer.print("  </evaluatedColumns>\n");

		Map<String, String> properties = result.getProperties();
		writeProperties("  ", writer, properties);
		writer.print(" </jobConfiguration>\n");

		Exception error = result.getError();
		if (error == null) {
			List<Row> unvalidatedRows = result.getUnvalidatedRows();
			if (unvalidatedRows == null || unvalidatedRows.isEmpty()) {
				writer.print(" <result validated=\"true\" />\n");
			} else {
				writer.print(" <result validated=\"false\">\n");
				boolean firstRow = true;
				for (Row row : unvalidatedRows) {
					if (firstRow) {
						writer.print("  <rowHeader>");
						SelectItem[] selectItems = row.getSelectItems();
						for (SelectItem selectItem : selectItems) {
							writer.print("<column>"
									+ safePrint(selectItem
											.getSuperQueryAlias(false))
									+ "</column>");
						}
						writer.print("  </rowHeader>\n");
						firstRow = false;
					}
					writer.print("  <invalidRow>");
					Object[] values = row.getValues();
					for (Object value : values) {
						writer.print("<value>" + safePrint(value) + "</value>");
					}
					writer.print("  </invalidRow>\n");
					writer.flush();
				}
				writer.print(" </result>\n");
			}
		} else {
			writer.print(" <result>\n");
			writer.print("  <error>\n");
			StringWriter stringWriter = new StringWriter();
			error.printStackTrace(new PrintWriter(stringWriter));
			writer.print(safePrint(stringWriter.toString()));
			writer.print("  </error>\n");
			writer.print(" </result>\n");
		}

		writer.print("</validationRuleResult>\n");
		writer.flush();
	}

	public static String safePrint(Object value) {
		if (value != null) {
			String str = value.toString();
			str = str.replaceAll("\\&", "&amp;");
			str = str.replaceAll("<", "&lt;");
			str = str.replaceAll(">", "&gt;");
			str = str.replaceAll("\"", "&quot;");
			str = str.replaceAll("\'", "&apos;");
			return str;
		}
		return "&lt;null&gt;";
	}

	private void writeTable(String prefix, Table table, PrintWriter writer) {
		writer.print(prefix);
		writer.print("<table name=\"" + safePrint(table.getName()) + "\"");
		Schema schema = table.getSchema();
		if (schema != null && schema.getName() != null) {
			writer.print(" schema=\"" + safePrint(schema.getName()) + "\"");
		}
		writer.print(" />\n");
	}

	private void writeColumn(String prefix, Column column, PrintWriter writer) {
		writer.print(prefix);
		writer.print("<column name=\"" + safePrint(column.getName()) + "\"");
		Table table = column.getTable();
		if (table != null && table.getName() != null) {
			writer.print(" table=\"" + safePrint(table.getName()) + "\"");
			Schema schema = table.getSchema();
			if (schema != null && schema.getName() != null) {
				writer.print(" schema=\"" + safePrint(schema.getName()) + "\"");
			}
		}
		writer.print(" />\n");
	}
}
