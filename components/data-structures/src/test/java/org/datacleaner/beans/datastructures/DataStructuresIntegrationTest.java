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
package org.datacleaner.beans.datastructures;

import java.util.List;
import java.util.Map;

import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.InputRow;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.connection.DatastoreCatalogImpl;
import org.datacleaner.data.MutableInputColumn;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.TransformerComponentBuilder;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.job.runner.AnalysisRunnerImpl;
import org.datacleaner.result.ListResult;
import org.datacleaner.test.MockAnalyzer;
import org.datacleaner.test.TestHelper;

import junit.framework.TestCase;

public class DataStructuresIntegrationTest extends TestCase {

    public void testBuildAndExtractFromStructures() throws Throwable {
        final Datastore datastore = TestHelper.createSampleDatabaseDatastore("orderdb");
        final DatastoreCatalog datastoreCatalog = new DatastoreCatalogImpl(datastore);
        final DataCleanerConfigurationImpl configuration =
                new DataCleanerConfigurationImpl().withDatastoreCatalog(datastoreCatalog);

        final AnalysisJobBuilder ajb = new AnalysisJobBuilder(configuration);
        ajb.setDatastore("orderdb");

        ajb.addSourceColumns("PUBLIC.EMPLOYEES.EMPLOYEENUMBER", "PUBLIC.EMPLOYEES.EMAIL");

        final MutableInputColumn<?> mapColumn;
        {
            final TransformerComponentBuilder<BuildMapTransformer> buildMap =
                    ajb.addTransformer(BuildMapTransformer.class);
            buildMap.addInputColumns(ajb.getSourceColumns());
            final BuildMapTransformer bean = buildMap.getComponentInstance();
            bean.setKeys(new String[] { "empno", "email_address" });
            bean.setRetainKeyOrder(true);
            bean.setIncludeNullValues(true);
            final List<MutableInputColumn<?>> outputColumns = buildMap.getOutputColumns();
            assertEquals(1, outputColumns.size());
            mapColumn = outputColumns.get(0);
            assertEquals("Map: empno,email_address", mapColumn.getName());
        }

        ajb.addSourceColumns("PUBLIC.EMPLOYEES.FIRSTNAME", "PUBLIC.EMPLOYEES.LASTNAME");

        final MutableInputColumn<?> listColumn;
        {
            final TransformerComponentBuilder<BuildListTransformer> buildList =
                    ajb.addTransformer(BuildListTransformer.class);
            buildList.addInputColumn(ajb.getSourceColumnByName("firstname"));
            buildList.addInputColumn(ajb.getSourceColumnByName("lastname"));
            final BuildListTransformer bean = buildList.getComponentInstance();
            bean.setIncludeNullValues(false);
            final List<MutableInputColumn<?>> outputColumns = buildList.getOutputColumns();
            assertEquals(1, outputColumns.size());
            listColumn = outputColumns.get(0);
            assertEquals("List: FIRSTNAME,LASTNAME", listColumn.getName());
        }

        assertSame(1, ajb.getAvailableInputColumns(Map.class).size());
        assertSame(mapColumn, ajb.getAvailableInputColumns(Map.class).get(0));
        assertSame(1, ajb.getAvailableInputColumns(List.class).size());
        assertSame(listColumn, ajb.getAvailableInputColumns(List.class).get(0));

        final MutableInputColumn<?> elementColumn;
        {
            final TransformerComponentBuilder<ReadFromListTransformer> extractFromList =
                    ajb.addTransformer(ReadFromListTransformer.class);
            extractFromList.addInputColumn(listColumn);
            final ReadFromListTransformer bean = extractFromList.getComponentInstance();
            bean.setVerifyTypes(true);
            bean.setElementType(String.class);
            final List<MutableInputColumn<?>> outputColumns = extractFromList.getOutputColumns();
            assertEquals(1, outputColumns.size());
            elementColumn = outputColumns.get(0);
            assertEquals("List: FIRSTNAME,LASTNAME (element)", elementColumn.getName());
        }

        final MutableInputColumn<?> valueColumn1;
        final MutableInputColumn<?> valueColumn2;
        {
            final TransformerComponentBuilder<SelectFromMapTransformer> extractFromMap =
                    ajb.addTransformer(SelectFromMapTransformer.class);
            extractFromMap.addInputColumn(mapColumn);
            final SelectFromMapTransformer bean = extractFromMap.getComponentInstance();
            bean.setKeys(new String[] { "empno", "email_address" });
            bean.setTypes(new Class[] { Number.class, String.class });
            bean.setVerifyTypes(true);
            final List<MutableInputColumn<?>> outputColumns = extractFromMap.getOutputColumns();
            assertEquals(2, outputColumns.size());
            valueColumn1 = outputColumns.get(0);
            assertEquals("empno", valueColumn1.getName());
            valueColumn2 = outputColumns.get(1);
            assertEquals("email_address", valueColumn2.getName());
        }

        ajb.addAnalyzer(MockAnalyzer.class)
                .addInputColumns(mapColumn, valueColumn1, valueColumn2, listColumn, elementColumn);

        final AnalysisJob job = ajb.toAnalysisJob();
        ajb.close();

        final AnalysisResultFuture resultFuture = new AnalysisRunnerImpl(configuration).run(job);

        if (resultFuture.isErrornous()) {
            throw resultFuture.getErrors().get(0);
        }

        final List<AnalyzerResult> results = resultFuture.getResults();
        assertEquals(1, results.size());

        @SuppressWarnings("unchecked") final ListResult<InputRow> result = (ListResult<InputRow>) results.get(0);
        final List<InputRow> rows = result.getValues();

        // first row
        InputRow row = rows.get(0);
        assertEquals("{empno=1002, email_address=dmurphy@classicmodelcars.com}", row.getValue(mapColumn).toString());
        assertEquals(1002, row.getValue(valueColumn1));
        assertEquals("dmurphy@classicmodelcars.com", row.getValue(valueColumn2).toString());
        assertEquals("[Diane, Murphy]", row.getValue(listColumn).toString());
        assertEquals("Diane", row.getValue(elementColumn).toString());

        // second row
        row = rows.get(1);
        assertEquals("{empno=1002, email_address=dmurphy@classicmodelcars.com}", row.getValue(mapColumn).toString());
        assertEquals(1002, row.getValue(valueColumn1));
        assertEquals("dmurphy@classicmodelcars.com", row.getValue(valueColumn2).toString());
        assertEquals("[Diane, Murphy]", row.getValue(listColumn).toString());
        assertEquals("Murphy", row.getValue(elementColumn).toString());

        // row count (2x 23 employees, one row for each name)
        assertEquals(46, rows.size());
    }
}
