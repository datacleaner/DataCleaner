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
package org.datacleaner.customcolumn;

import java.util.List;

import junit.framework.TestCase;

import org.datacleaner.configuration.AnalyzerBeansConfiguration;
import org.datacleaner.configuration.AnalyzerBeansConfigurationImpl;
import org.datacleaner.connection.CsvDatastore;
import org.datacleaner.data.InputRow;
import org.datacleaner.data.MutableInputColumn;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.TransformerJobBuilder;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.job.runner.AnalysisRunner;
import org.datacleaner.job.runner.AnalysisRunnerImpl;
import org.datacleaner.result.ListResult;

public class CustomColumnTypeTest extends TestCase {

    @SuppressWarnings("unchecked")
    public void testCustomColumnOutputInJob() throws Throwable {
        AnalyzerBeansConfiguration configuration = new AnalyzerBeansConfigurationImpl();
        final AnalysisJob job;
        final MutableInputColumn<?> monthObjectColumn;

        // build example job
        try (AnalysisJobBuilder builder = new AnalysisJobBuilder(configuration)) {
            builder.setDatastore(new CsvDatastore("Names", "src/test/resources/month-strings.csv"));
            builder.addSourceColumns("month");

            TransformerJobBuilder<MockConvertToMonthObjectTransformer> convertTransformer = builder.addTransformer(
                    MockConvertToMonthObjectTransformer.class).addInputColumn(builder.getSourceColumnByName("month"));
            monthObjectColumn = convertTransformer.getOutputColumns().get(0);

            builder.addAnalyzer(MockMonthConsumingAnalyzer.class).addInputColumns(monthObjectColumn);
            job = builder.toAnalysisJob();
        }

        ListResult<InputRow> result;

        // run job
        {
            AnalysisRunner runner = new AnalysisRunnerImpl(configuration);
            AnalysisResultFuture resultFuture = runner.run(job);
            if (resultFuture.isErrornous()) {
                throw resultFuture.getErrors().get(0);
            }
            result = (ListResult<InputRow>) resultFuture.getResults().get(0);
        }

        List<InputRow> list = result.getValues();

        assertEquals(9, list.size());
        Month value = (Month) list.get(0).getValue(monthObjectColumn);
        assertEquals("Month [monthNameFull=January, monthShortCut=JAN, monthAsNumber=1]", value.toString());
        assertEquals(1, value.getMonthAsNumber());
        value = (Month) list.get(5).getValue(monthObjectColumn);
        assertEquals("Month [monthNameFull=December, monthShortCut=DEC, monthAsNumber=12]", value.toString());
        assertEquals(12, value.getMonthAsNumber());

    }

}
