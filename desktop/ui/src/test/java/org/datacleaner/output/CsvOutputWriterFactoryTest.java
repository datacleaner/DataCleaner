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
package org.datacleaner.output;

import java.io.File;

import org.apache.metamodel.DataContext;
import org.apache.metamodel.DataContextFactory;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.query.Query;
import org.apache.metamodel.schema.Table;
import org.datacleaner.output.csv.CsvOutputWriterFactory;

import junit.framework.TestCase;

public class CsvOutputWriterFactoryTest extends TestCase {

    public void testFullScenario() throws Exception {
        final OutputWriterScenarioHelper scenarioHelper = new OutputWriterScenarioHelper();

        final String filename = "target/test-output/csv-file1.txt";
        final OutputWriter writer = CsvOutputWriterFactory.getWriter(filename, scenarioHelper.getColumns());

        scenarioHelper.writeExampleData(writer);
        writer.close();

        final DataContext dc = DataContextFactory.createCsvDataContext(new File(filename));
        final Table table = dc.getDefaultSchema().getTables()[0];
        final Query q = dc.query().from(table).select(table.getColumns()).toQuery();
        final DataSet dataSet = dc.executeQuery(q);

        scenarioHelper.performAssertions(dataSet, false);
    }
}
