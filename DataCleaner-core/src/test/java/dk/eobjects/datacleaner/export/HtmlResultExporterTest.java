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

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;

import dk.eobjects.datacleaner.profiler.IProfileResult;
import dk.eobjects.datacleaner.profiler.ProfileManagerTest;
import dk.eobjects.datacleaner.profiler.trivial.NumberAnalysisProfile;
import dk.eobjects.datacleaner.testware.DataCleanerTestCase;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.query.SelectItem;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.ColumnType;
import dk.eobjects.metamodel.schema.Table;
import dk.eobjects.metamodel.util.FileHelper;

public class HtmlResultExporterTest extends DataCleanerTestCase {

	public void testGenerateHtml() throws Exception {
		ProfileManagerTest.initProfileManager();
		
		File outputFile = getTestResourceAsFile("html_exporter_output.html");
		File benchmarkFile = getTestResourceAsFile("html_exporter_bench.html");
		PrintWriter writer = new PrintWriter(FileHelper.getWriter(outputFile));

		HtmlResultExporter exporter = new HtmlResultExporter();
		exporter.writeProfileResultHeader(writer);

		Table table = new Table("fooTable");
		Column col1 = new Column("fooColumn", ColumnType.BIGINT, table, 0, false);
		Column col2 = new Column("barColumn", ColumnType.DOUBLE, table, 1, false);
		Column[] columns = new Column[] { col1, col2 };
		SelectItem[] selectItems = new SelectItem[] { new SelectItem(col1), new SelectItem(col2) };

		NumberAnalysisProfile profile = new NumberAnalysisProfile();
		profile.setProperties(new HashMap<String, String>());
		profile.initialize(columns);
		profile.process(new Row(selectItems, new Object[] { 4, 2.4 }), 1);
		profile.process(new Row(selectItems, new Object[] { 6, 2 }), 1);
		profile.process(new Row(selectItems, new Object[] { 8, 0.0 }), 1);
		IProfileResult result = profile.getResult();
		
		exporter.writeProfileResult(table, result, writer);

		exporter.writeProfileResultFooter(writer);
		writer.close();

		assertEqualsFile(benchmarkFile, outputFile);
	}
}
