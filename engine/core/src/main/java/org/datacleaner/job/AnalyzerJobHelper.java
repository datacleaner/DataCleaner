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
package org.datacleaner.job;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.datacleaner.api.InputColumn;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.util.CollectionUtils2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class that wraps a collection of {@link AnalyzerJob}s and provides
 * richer functionality to traverse and search jobs for components and columns
 * etc.
 */
public class AnalyzerJobHelper {

    private static final Logger logger = LoggerFactory.getLogger(AnalyzerJobHelper.class);

    private final Collection<AnalyzerJob> _jobs;

    public AnalyzerJobHelper(final Collection<AnalyzerJob> jobs) {
        _jobs = jobs;
    }

    public AnalyzerJobHelper(final AnalysisJob analysisJob) {
        this(analysisJob.flattened().flatMap(analysisJob1 -> analysisJob1.getAnalyzerJobs().stream())
                .collect(Collectors.toList()));
    }

    /**
     * Gets the identifying input column of an {@link ComponentJob}, if there is
     * such a column. With an identifying input column, a externalizable
     * reference to the {@link ComponentJob} can be build, based on the
     * descriptor name, component name and the identifying column.
     *
     * @param componentJob
     * @return
     */
    public static InputColumn<?> getIdentifyingInputColumn(final ComponentJob componentJob) {
        final ComponentDescriptor<?> descriptor = componentJob.getDescriptor();
        final Set<ConfiguredPropertyDescriptor> inputProperties = descriptor.getConfiguredPropertiesForInput(false);
        if (inputProperties.size() != 1) {
            return null;
        }

        final ConfiguredPropertyDescriptor inputProperty = inputProperties.iterator().next();
        final Object input = componentJob.getConfiguration().getProperty(inputProperty);

        if (input instanceof InputColumn) {
            return (InputColumn<?>) input;
        } else if (input instanceof InputColumn[]) {
            final InputColumn<?>[] inputColumns = (InputColumn[]) input;
            if (inputColumns.length != 1) {
                return null;
            }
            return inputColumns[0];
        }
        return null;
    }

    public Collection<AnalyzerJob> getAnalyzerJobs() {
        return _jobs;
    }

    /**
     * Gets the "best candidate" to be the same (or a copy of) the analyzer job
     * provided in parameter.
     *
     * @param analyzerJob
     * @return
     */
    public AnalyzerJob getAnalyzerJob(final AnalyzerJob analyzerJob) {
        if (_jobs.contains(analyzerJob)) {
            return analyzerJob;
        }

        final String analyzerInputName;
        final InputColumn<?> inputColumn = getIdentifyingInputColumn(analyzerJob);
        if (inputColumn == null) {
            analyzerInputName = null;
        } else {
            analyzerInputName = inputColumn.getName();
        }
        return getAnalyzerJob(analyzerJob.getDescriptor().getDisplayName(), analyzerJob.getName(), analyzerInputName);
    }

    /**
     * Gets the "best candidate" analyzer job based on search criteria offered
     * in parameters.
     *
     * @param descriptorName
     * @param analyzerName
     * @param analyzerInputName
     * @return
     */
    public AnalyzerJob getAnalyzerJob(final String descriptorName, final String analyzerName,
            final String analyzerInputName) {
        List<AnalyzerJob> candidates = new ArrayList<>(_jobs);

        // filter analyzers of the corresponding type
        candidates = CollectionUtils2.refineCandidates(candidates, o -> {
            final String actualDescriptorName = o.getDescriptor().getDisplayName();
            return descriptorName.equals(actualDescriptorName);
        });

        if (analyzerName != null) {
            // filter analyzers with a particular name
            candidates = CollectionUtils2.refineCandidates(candidates, o -> {
                final String actualAnalyzerName = o.getName();
                return analyzerName.equals(actualAnalyzerName);
            });
        }

        if (analyzerInputName != null) {
            // filter analyzers with a particular input
            candidates = CollectionUtils2.refineCandidates(candidates, o -> {
                final InputColumn<?> inputColumn = getIdentifyingInputColumn(o);
                if (inputColumn == null) {
                    return false;
                }

                return analyzerInputName.equals(inputColumn.getName());
            });
        }

        if (candidates.isEmpty()) {
            logger.error("No more AnalyzerJob candidates to choose from");
            return null;
        } else if (candidates.size() > 1) {
            logger.warn("Multiple ({}) AnalyzerJob candidates to choose from, picking first");
        }

        return candidates.iterator().next();
    }
}
