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
package org.datacleaner.extension.output;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.metamodel.schema.Table;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.connection.CsvDatastore;
import org.datacleaner.descriptors.SimpleDescriptorProvider;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.PrefixedIdGenerator;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.job.builder.TransformerComponentBuilder;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.job.runner.AnalysisRunnerImpl;
import org.datacleaner.job.runner.JobStatus;
import org.junit.Test;

public class CreateExcelSpreadsheetAnalyzerIT {

    @Test
    public void testConcurrentWriting() {
        final DataCleanerConfigurationImpl configuration = new DataCleanerConfigurationImpl();
        final AnalysisJobBuilder jobBuilder = new AnalysisJobBuilder(configuration);

        final CsvDatastore datastore = new CsvDatastore("Customers", "src/test/resources/example_updated.csv");
        final File excelFile = new File("target/exceltest-concurrentwriting.xlsx");

        jobBuilder.setDatastore(datastore);

        final Table datastoreTableDefinition =
                datastore.openConnection().getDataContext().getDefaultSchema().getTable(0);

        jobBuilder.addSourceColumns(datastoreTableDefinition.getColumns());

        final TransformerComponentBuilder<MultiStreamTestTransformer> transformer =
                new TransformerComponentBuilder<>(jobBuilder, new SimpleDescriptorProvider()
                        .getTransformerDescriptorForClass(MultiStreamTestTransformer.class), new PrefixedIdGenerator());

        transformer.addInputColumns(jobBuilder.getSourceColumns());

        jobBuilder.addTransformer(transformer);

        addWriter(transformer, MultiStreamTestTransformer.OUTPUT_STREAM_EVEN, datastoreTableDefinition, excelFile);
        addWriter(transformer, MultiStreamTestTransformer.OUTPUT_STREAM_UNEVEN, datastoreTableDefinition, excelFile);

        assertTrue(jobBuilder.isConfigured());

        final AnalysisRunnerImpl runner = new AnalysisRunnerImpl(configuration);
        final AnalysisJob analysisJob = jobBuilder.toAnalysisJob();

        final AnalysisResultFuture firstRunResult = runner.run(analysisJob);
        firstRunResult.await();
        firstRunResult.getErrors().forEach(Throwable::printStackTrace);

        assertEquals(JobStatus.SUCCESSFUL, firstRunResult.getStatus());

        final AnalysisResultFuture secondRunResult = runner.run(analysisJob);
        secondRunResult.await();
        secondRunResult.getErrors().forEach(Throwable::printStackTrace);

        assertEquals(JobStatus.SUCCESSFUL, secondRunResult.getStatus());
    }

    private void addWriter(final TransformerComponentBuilder<MultiStreamTestTransformer> transformer,
            final String outputStreamName, final Table datastoreTableDefinition, final File excelFile) {
        final AnalysisJobBuilder jobBuilder = transformer.getOutputDataStreamJobBuilder(outputStreamName);

        final AnalyzerComponentBuilder<CreateExcelSpreadsheetAnalyzer> excelWriter =
                new AnalyzerComponentBuilder<>(jobBuilder, new SimpleDescriptorProvider()
                        .getAnalyzerDescriptorForClass(CreateExcelSpreadsheetAnalyzer.class));

        excelWriter.addInputColumns(jobBuilder.getAvailableInputColumns(excelWriter));
        excelWriter.setConfiguredProperty(AbstractOutputWriterAnalyzer.PROPERTY_FIELD_NAMES,
                datastoreTableDefinition.getColumnNames().toArray(new String[0]));
        excelWriter.setConfiguredProperty(CreateExcelSpreadsheetAnalyzer.PROPERTY_FILE, excelFile);
        excelWriter.setConfiguredProperty(CreateExcelSpreadsheetAnalyzer.PROPERTY_SHEET_NAME, outputStreamName);
        excelWriter.setConfiguredProperty(CreateExcelSpreadsheetAnalyzer.PROPERTY_OVERWRITE_SHEET_IF_EXISTS, true);

        jobBuilder.addAnalyzer(excelWriter);
    }
}
