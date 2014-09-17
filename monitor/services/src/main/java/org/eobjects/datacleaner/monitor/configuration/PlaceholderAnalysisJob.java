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
package org.eobjects.datacleaner.monitor.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.descriptors.Descriptors;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalysisJobMetadata;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.job.BeanConfiguration;
import org.eobjects.analyzer.job.FilterJob;
import org.eobjects.analyzer.job.ImmutableAnalyzerJob;
import org.eobjects.analyzer.job.ImmutableBeanConfiguration;
import org.eobjects.analyzer.job.TransformerJob;
import org.eobjects.datacleaner.util.PreviewTransformedDataAnalyzer;

/**
 * {@link AnalysisJob} which replaces the {@link Datastore} of another
 * {@link AnalysisJob}.
 */
public class PlaceholderAnalysisJob implements AnalysisJob {

    private final Datastore _datastore;
    private final AnalysisJob _delegateJob;

    public PlaceholderAnalysisJob(Datastore datastore, AnalysisJob delegateJob) {
        _datastore = datastore;
        _delegateJob = delegateJob;
    }

    @Override
    public Datastore getDatastore() {
        return _datastore;
    }

    @Override
    public List<InputColumn<?>> getSourceColumns() {
        return _delegateJob.getSourceColumns();
    }

    @Override
    public List<TransformerJob> getTransformerJobs() {
        return _delegateJob.getTransformerJobs();
    }

    @Override
    public List<FilterJob> getFilterJobs() {
        return _delegateJob.getFilterJobs();
    }

    @Override
    public List<AnalyzerJob> getAnalyzerJobs() {
        // create a single analyzer for picking up records

        final AnalyzerBeanDescriptor<?> descriptor = Descriptors.ofAnalyzer(PreviewTransformedDataAnalyzer.class);
        final Map<ConfiguredPropertyDescriptor, Object> properties = new HashMap<ConfiguredPropertyDescriptor, Object>();

        final List<InputColumn<?>> columns = new ArrayList<InputColumn<?>>();
        final Collection<TransformerJob> transformerJobs = getTransformerJobs();
        for (TransformerJob transformerJob : transformerJobs) {
            InputColumn<?>[] outputColumns = transformerJob.getOutput();
            for (InputColumn<?> outputColumn : outputColumns) {
                columns.add(outputColumn);
            }
        }

        properties.put(descriptor.getConfiguredPropertiesForInput().iterator().next(),
                columns.toArray(new InputColumn[columns.size()]));
        final BeanConfiguration beanConfiguration = new ImmutableBeanConfiguration(properties);

        final AnalyzerJob analyzerJob = new ImmutableAnalyzerJob("Record gatherer", descriptor, beanConfiguration, null);
        return Arrays.asList(analyzerJob);
    }

    @Override
    public AnalysisJobMetadata getMetadata() {
        return _delegateJob.getMetadata();
    }

}
