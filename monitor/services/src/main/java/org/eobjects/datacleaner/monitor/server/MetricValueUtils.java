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
package org.eobjects.datacleaner.monitor.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.job.ComponentJob;
import org.eobjects.analyzer.result.AnalysisResult;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.util.CollectionUtils2;
import org.eobjects.datacleaner.monitor.shared.model.MetricIdentifier;
import org.eobjects.metamodel.util.CollectionUtils;
import org.eobjects.metamodel.util.HasNameMapper;
import org.eobjects.metamodel.util.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetricValueUtils {

    private static final Logger logger = LoggerFactory.getLogger(MetricValueUtils.class);

    public AnalyzerResult getResult(final AnalysisResult analysisResult, final AnalyzerJob analyzerJob,
            final MetricIdentifier metricIdentifier) {
        AnalyzerResult result = analysisResult.getResult(analyzerJob);
        if (result == null) {
            logger.info("Could not resolve AnalyzerResult using key={}, reiterating using non-exact matching",
                    analyzerJob);

            Collection<ComponentJob> componentJobs = analysisResult.getResultMap().keySet();

            List<AnalyzerJob> candidates = CollectionUtils2.filterOnClass(componentJobs, AnalyzerJob.class);

            // filter analyzers of the corresponding type
            candidates = refineCandidates(candidates, new Predicate<AnalyzerJob>() {
                @Override
                public Boolean eval(AnalyzerJob o) {
                    final String actualDescriptorName = o.getDescriptor().getDisplayName();
                    final String metricDescriptorName = analyzerJob.getDescriptor().getDisplayName();
                    return metricDescriptorName.equals(actualDescriptorName);
                }
            });

            final String analyzerJobName = analyzerJob.getName();
            if (analyzerJobName != null) {
                // filter analyzers with a particular name
                candidates = refineCandidates(candidates, new Predicate<AnalyzerJob>() {
                    @Override
                    public Boolean eval(AnalyzerJob o) {
                        final String actualAnalyzerName = o.getName();
                        final String metricAnalyzerName = analyzerJobName;
                        return metricAnalyzerName.equals(actualAnalyzerName);
                    }
                });
            }

            // filter analyzer jobs with same input
            candidates = refineCandidates(candidates, new Predicate<AnalyzerJob>() {
                @Override
                public Boolean eval(AnalyzerJob o) {
                    final String actualAnalyzerInputNames = CollectionUtils.map(o.getInput(), new HasNameMapper())
                            .toString();
                    final String metricAnalyzerInputNames = CollectionUtils.map(analyzerJob.getInput(),
                            new HasNameMapper()).toString();
                    return metricAnalyzerInputNames.equals(actualAnalyzerInputNames);
                }
            });

            // filter analyzer jobs with input matching the metric
            final String analyzerInputName = metricIdentifier.getAnalyzerInputName();
            if (analyzerInputName != null) {
                candidates = refineCandidates(candidates, new Predicate<AnalyzerJob>() {
                    @Override
                    public Boolean eval(AnalyzerJob o) {
                        InputColumn<?> identifyingInputColumn = getIdentifyingInputColumn(o);
                        if (identifyingInputColumn == null) {
                            return false;
                        }
                        return analyzerInputName.equals(identifyingInputColumn.getName());
                    }
                });
            }

            if (candidates.isEmpty()) {
                throw new IllegalArgumentException("No matching AnalyzerJobs found");
            } else if (candidates.size() > 1) {
                logger.warn("Multiple matching AnalyzerJobs found, selecting the first: {}", candidates);
            }
            AnalyzerJob candidate = candidates.iterator().next();
            result = analysisResult.getResult(candidate);
        }
        return result;
    }

    public AnalyzerJob getAnalyzerJob(final MetricIdentifier metricIdentifier, final AnalysisJob analysisJob) {
        List<AnalyzerJob> candidates = new ArrayList<AnalyzerJob>(analysisJob.getAnalyzerJobs());

        // filter analyzers of the corresponding type
        candidates = refineCandidates(candidates, new Predicate<AnalyzerJob>() {
            @Override
            public Boolean eval(AnalyzerJob o) {
                final String actualDescriptorName = o.getDescriptor().getDisplayName();
                final String metricDescriptorName = metricIdentifier.getAnalyzerDescriptorName();
                return metricDescriptorName.equals(actualDescriptorName);
            }
        });

        if (metricIdentifier.getAnalyzerName() != null) {
            // filter analyzers with a particular name
            candidates = refineCandidates(candidates, new Predicate<AnalyzerJob>() {
                @Override
                public Boolean eval(AnalyzerJob o) {
                    final String actualAnalyzerName = o.getName();
                    final String metricAnalyzerName = metricIdentifier.getAnalyzerName();
                    return metricAnalyzerName.equals(actualAnalyzerName);
                }
            });
        }

        if (metricIdentifier.getAnalyzerInputName() != null) {
            // filter analyzers with a particular input
            candidates = refineCandidates(candidates, new Predicate<AnalyzerJob>() {
                @Override
                public Boolean eval(AnalyzerJob o) {
                    final InputColumn<?> inputColumn = getIdentifyingInputColumn(o);
                    if (inputColumn == null) {
                        return false;
                    }

                    final String metricInputName = metricIdentifier.getAnalyzerInputName();
                    return metricInputName.equals(inputColumn.getName());
                }
            });
        }

        if (candidates.isEmpty()) {
            logger.error("No more AnalyzerJob candidates to choose from");
            return null;
        } else if (candidates.size() > 1) {
            logger.warn("Multiple ({}) AnalyzerJob candidates to choose from, picking first");
        }

        AnalyzerJob analyzerJob = candidates.iterator().next();
        return analyzerJob;
    }

    private <E> List<E> refineCandidates(final List<E> candidates, final Predicate<? super E> predicate) {
        if (candidates.size() == 1) {
            return candidates;
        }
        List<E> newCandidates = CollectionUtils.filter(candidates, predicate);
        if (newCandidates.isEmpty()) {
            return candidates;
        }
        return newCandidates;
    }

    public InputColumn<?> getIdentifyingInputColumn(final AnalyzerJob o) {
        final Set<ConfiguredPropertyDescriptor> inputProperties = o.getDescriptor().getConfiguredPropertiesForInput(
                false);
        if (inputProperties.size() != 1) {
            return null;
        }

        final ConfiguredPropertyDescriptor inputProperty = inputProperties.iterator().next();
        final Object input = o.getConfiguration().getProperty(inputProperty);

        if (input instanceof InputColumn) {
            final InputColumn<?> inputColumn = (InputColumn<?>) input;
            return inputColumn;
        } else if (input instanceof InputColumn[]) {
            final InputColumn<?>[] inputColumns = (InputColumn[]) input;
            if (inputColumns.length != 1) {
                return null;
            }
            return inputColumns[0];
        }
        return null;
    }
}
