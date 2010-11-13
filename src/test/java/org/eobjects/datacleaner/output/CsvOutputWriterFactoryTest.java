/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
