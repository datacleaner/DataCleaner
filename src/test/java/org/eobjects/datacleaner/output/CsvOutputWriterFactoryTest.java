package org.eobjects.datacleaner.output;

import java.io.File;

import junit.framework.TestCase;

import org.eobjects.datacleaner.output.csv.CsvOutputWriterFactory;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.DataContextFactory;
import dk.eobjects.metamodel.data.DataSet;
import dk.eobjects.metamodel.query.Query;
import dk.eobjects.metamodel.schema.Table;

public class CsvOutputWriterFactoryTest extends TestCase {

	public void testFullScenario() throws Exception {
		OutputWriterScenarioHelper scenarioHelper = new OutputWriterScenarioHelper();

		final String filename = "target/test-output/csv-file1.txt";
		OutputWriter writer = CsvOutputWriterFactory.getWriter(filename, scenarioHelper.getColumns());

		scenarioHelper.writeExampleData(writer);
		writer.close();
		
		DataContext dc = DataContextFactory.createCsvDataContext(new File(filename));
		Table table = dc.getDefaultSchema().getTables()[0];
		Query q = dc.query().from(table).select(table.getColumns()).toQuery();
		DataSet dataSet = dc.executeQuery(q);
		
		scenarioHelper.performAssertions(dataSet, false);
	}
}
