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

import dk.eobjects.datacleaner.profiler.IMatrix;
import dk.eobjects.datacleaner.profiler.IProfileResult;
import dk.eobjects.datacleaner.validator.IValidationRuleResult;
import dk.eobjects.metamodel.schema.Table;

public class HtmlResultExporter implements IResultExporter {

	private Table currentTable;
	private int currentId;

	public boolean isCollectiveResultsCapable() {
		return true;
	}

	public void writeProfileResultHeader(PrintWriter writer) {
		writer.println("<html>");
		writer.println("<head>");
		writer.println("<title>DataCleaner profiler results</title>");
		writer.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />");
		writer.println("<style type=\"text/css\">");
		writer.println("	@import \"http://o.aolcdn.com/dojo/1.3/dojox/grid/resources/nihiloGrid.css\";");
		writer.println("	@import \"http://o.aolcdn.com/dojo/1.3/dojox/grid/resources/Grid.css\";");
		writer.println("	h1 { margin-left: 5px; }");
		writer.println("	h2 { margin-left: 10px; }");
		writer.println("	.gridContainer { margin-left: 20px; }");
		writer.println("</style>");
		writer.println("<script type=\"text/javascript\" src=\"http://o.aolcdn.com/dojo/1.3/dojo/dojo.xd.js\"");
		writer.println("	djConfig=\"parseOnLoad: true\"></script>");
		writer.println("<script type=\"text/javascript\">");
		writer.println("	dojo.require(\"dojox.grid.DataGrid\");");
		writer.println("	dojo.require(\"dojox.data.HtmlStore\");");
		writer.println("	dojo.require(\"dojo.parser\");");
		writer.println("</script>");
		writer.println("</head>");
		writer.println("<body class=\"niholo\">");
		writer
				.println("<a href=\"http://datacleaner.eobjects.org\" target=\"_blank\"><img src=\"http://datacleaner.eobjects.org/resources/icon_datacleaner.png\" alt=\"Created with DataCleaner\" style=\"float: right; border: none;\" /></a>");
	}

	public void writeProfileResult(Table table, IProfileResult result, PrintWriter writer) {
		if (table != currentTable) {
			currentTable = table;
			writer.println("<h1>Results for table: " + safePrint(table.getName()) + "</h1>");
		}
		IMatrix[] matrices = result.getMatrices();

		// Per-result html
		for (int i = 0; i < matrices.length; i++) {
			currentId++;
			if (matrices.length == 1) {
				writer.println("<h2>" + safePrint(result.getDescriptor().getDisplayName()) + "</h2>");
			} else {
				writer.println("<h2>" + safePrint(result.getDescriptor().getDisplayName()) + " #" + (i + 1) + "</h2>");
			}
			IMatrix matrix = matrices[i];

			// Creates a HTML table containing the data for the matrix
			writer.println("<table id=\"dataTable" + currentId + "\" style=\"visibility: hidden; display: none;\">");
			writer.println("	<thead>");
			writer.print("	<tr><th>_</th>");
			String[] columnNames = matrix.getColumnNames();
			for (int j = 0; j < columnNames.length; j++) {
				String columnName = columnNames[j];
				writer.print("<th>" + safePrint(columnName) + "</th>");
			}
			writer.println("</tr>");
			writer.println("	</thead>");
			writer.println("	<tbody>");
			String[] rowNames = matrix.getRowNames();
			for (String rowName : rowNames) {
				writer.print("	<tr><td>" + rowName + "</td>");
				for (String columnName : columnNames) {
					Object value = matrix.getValue(rowName, columnName).getValue();
					writer.print("<td>" + safePrint(value) + "</td>");
				}
				writer.println("</tr>");
			}
			writer.println("	</tbody>");
			writer.println("</table>");

			// Creates a dojo grid for the table
			writer.println("<div dojoType=\"dojox.data.HtmlStore\" dataId=\"dataTable" + currentId
					+ "\" jsId=\"gridStore" + currentId + "\"></div>");
			writer.println("<div class=\"gridContainer\"><table dojoType=\"dojox.grid.DataGrid\"");
			writer.println("	store=\"gridStore" + currentId + "\" autoHeight=\"" + (matrix.getRowNames().length + 1)
					+ "\" columnReordering=\"true\">");
			writer.println("<thead>");
			writer.print("	<tr><th>_</th>");
			for (int j = 0; j < columnNames.length; j++) {
				String columnName = columnNames[j];
				writer.print("<th>" + safePrint(columnName) + "</th>");
			}
			writer.println("</thead>");
			writer.println("</table></div>");
		}
	}

	private String safePrint(Object value) {
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

	public void writeProfileResultFooter(PrintWriter writer) {
		writer.println("</body>");
		writer.println("</html>");
	}

	public void writeValidationRuleResultHeader(PrintWriter writer) {
		throw new IllegalStateException("HTML export has not been implemented for the Validator (yet)!");
	}

	public void writeValidationRuleResult(Table table, IValidationRuleResult result, PrintWriter writer) {
		throw new IllegalStateException("HTML export has not been implemented for the Validator (yet)!");
	}

	public void writeValidationRuleResultFooter(PrintWriter writer) {
		throw new IllegalStateException("HTML export has not been implemented for the Validator (yet)!");
	}
}
