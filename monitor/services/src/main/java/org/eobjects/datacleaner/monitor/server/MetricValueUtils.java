/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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

import java.util.Collection;
import java.util.List;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.descriptors.MetricDescriptor;
import org.eobjects.analyzer.descriptors.MetricParameters;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.job.AnalyzerJobHelper;
import org.eobjects.analyzer.job.ComponentJob;
import org.eobjects.analyzer.result.AnalysisResult;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.util.CollectionUtils2;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.monitor.shared.model.MetricIdentifier;
import org.eobjects.metamodel.util.CollectionUtils;
import org.eobjects.metamodel.util.HasNameMapper;
import org.eobjects.metamodel.util.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.odysseus.el.ExpressionFactoryImpl;
import de.odysseus.el.util.SimpleContext;

public class MetricValueUtils {

    private static final Logger logger = LoggerFactory.getLogger(MetricValueUtils.class);

    public AnalyzerResult getResult(final AnalysisResult analysisResult, final AnalyzerJob analyzerJob,
            final MetricIdentifier metricIdentifier) {
        AnalyzerResult result = analysisResult.getResult(analyzerJob);
        if (result == null) {
            logger.debug("Could not resolve AnalyzerResult using key={}, reiterating using non-exact matching",
                    analyzerJob);
            result = getResultFuzzy(analysisResult, analyzerJob, metricIdentifier);
        } else {
            logger.debug("Resolved AnalyzerResult using key={}", analyzerJob);
        }
        return result;
    }

    private AnalyzerResult getResultFuzzy(final AnalysisResult analysisResult, final AnalyzerJob analyzerJob,
            final MetricIdentifier metricIdentifier) {
        Collection<ComponentJob> componentJobs = analysisResult.getResultMap().keySet();

        List<AnalyzerJob> candidates = CollectionUtils2.filterOnClass(componentJobs, AnalyzerJob.class);

        // filter analyzers of the corresponding type
        candidates = CollectionUtils2.refineCandidates(candidates, new Predicate<AnalyzerJob>() {
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
            candidates = CollectionUtils2.refineCandidates(candidates, new Predicate<AnalyzerJob>() {
                @Override
                public Boolean eval(AnalyzerJob o) {
                    final String actualAnalyzerName = o.getName();
                    final String metricAnalyzerName = analyzerJobName;
                    return metricAnalyzerName.equals(actualAnalyzerName);
                }
            });
        }

        // filter analyzer jobs with same input
        candidates = CollectionUtils2.refineCandidates(candidates, new Predicate<AnalyzerJob>() {
            @Override
            public Boolean eval(AnalyzerJob o) {
                final String actualAnalyzerInputNames = CollectionUtils.map(o.getInput(), new HasNameMapper())
                        .toString();
                final String metricAnalyzerInputNames = CollectionUtils
                        .map(analyzerJob.getInput(), new HasNameMapper()).toString();
                return metricAnalyzerInputNames.equals(actualAnalyzerInputNames);
            }
        });

        // filter analyzer jobs with input matching the metric
        final String analyzerInputName = metricIdentifier.getAnalyzerInputName();
        if (analyzerInputName != null) {
            candidates = CollectionUtils2.refineCandidates(candidates, new Predicate<AnalyzerJob>() {
                @Override
                public Boolean eval(AnalyzerJob o) {
                    InputColumn<?> identifyingInputColumn = AnalyzerJobHelper.getIdentifyingInputColumn(o);
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

        final AnalyzerJob candidate = candidates.iterator().next();

        if (logger.isDebugEnabled()) {
            int candidateHash = candidate.hashCode();
            int keyHash = analyzerJob.hashCode();
            boolean equals = candidate.equals(analyzerJob);
            logger.debug("Result of fuzzy result lookup: Equals={}, CandidateHash={}, KeyHash={}", new Object[] {
                    equals, candidateHash, keyHash });
        }

        return analysisResult.getResult(candidate);
    }

    public AnalyzerJob getAnalyzerJob(MetricIdentifier metric, final AnalysisJob analysisJob) {
        final MetricIdentifier metricIdentifier;
        if (metric.isFormulaBased()) {
            metricIdentifier = findSingularMetricIdentifierOfFormula(metric);
        } else {
            metricIdentifier = metric;
        }

        final AnalyzerJobHelper analyzerJobHelper = new AnalyzerJobHelper(analysisJob);
        final AnalyzerJob analyzerJob = analyzerJobHelper.getAnalyzerJob(metricIdentifier.getAnalyzerDescriptorName(), metric.getAnalyzerName(), metric.getAnalyzerInputName());
        return analyzerJob;
    }

    private MetricIdentifier findSingularMetricIdentifierOfFormula(MetricIdentifier metric) {
        List<MetricIdentifier> children = metric.getChildren();
        for (MetricIdentifier child : children) {
            if (child.isFormulaBased()) {
                MetricIdentifier childCandidate = findSingularMetricIdentifierOfFormula(child);
                if (childCandidate != null) {
                    return childCandidate;
                }
            } else {
                return child;
            }
        }
        throw new IllegalStateException("No singular metrics found in formula metric: " + metric);
    }

    public MetricDescriptor getMetricDescriptor(final MetricIdentifier metricIdentifier, final AnalysisJob analysisJob,
            final AnalyzerJob analyzerJob) {
        if (metricIdentifier.isFormulaBased()) {
            return null;
        }

        final AnalyzerJob analyzerJobToUse;
        if (analyzerJob == null) {
            // analyzer job has not been specified yet, probably because this
            // metric is a child to a formula based metric
            analyzerJobToUse = getAnalyzerJob(metricIdentifier, analysisJob);
        } else {
            analyzerJobToUse = analyzerJob;
        }

        final AnalyzerBeanDescriptor<?> analyzerDescriptor = analyzerJobToUse.getDescriptor();
        final MetricDescriptor metric = analyzerDescriptor.getResultMetric(metricIdentifier.getMetricDescriptorName());

        if (metric == null) {
            logger.error("Did not find any metric descriptors with name '{}' in {}",
                    metricIdentifier.getMetricDescriptorName(), analyzerDescriptor.getResultClass());
        }
        return metric;
    }

    public String getFormulaVariableName(MetricIdentifier child) {
        final String variableName;
        if (child.isDisplayNameSet()) {
            variableName = child.getDisplayName();
        } else {
            variableName = child.getMetricDescriptorName();
        }
        return variableName;
    }

    public MetricParameters getParameters(final MetricIdentifier metricIdentifier,
            final MetricDescriptor metricDescriptor, AnalyzerJob analyzerJob) {
        final String queryString;
        final InputColumn<?> queryInputColumn;

        final String paramQueryString = metricIdentifier.getParamQueryString();
        if (paramQueryString == null) {
            queryString = null;
        } else {
            queryString = paramQueryString;
        }

        final String paramColumnName = metricIdentifier.getParamColumnName();
        if (paramColumnName == null) {
            queryInputColumn = null;
        } else {
            InputColumn<?>[] inputColumns = analyzerJob.getInput();
            InputColumn<?> candidate = null;
            for (InputColumn<?> inputColumn : inputColumns) {
                if (paramColumnName.equals(inputColumn.getName())) {
                    candidate = inputColumn;
                    break;
                }
            }
            if (candidate == null) {
                logger.warn("Could not find any input column with name '{}'", paramColumnName);
            }
            queryInputColumn = candidate;
        }

        return new MetricParameters(queryString, queryInputColumn);
    }

    public Number getMetricValue(MetricIdentifier metricIdentifier, MetricDescriptor metric, AnalysisJob analysisJob,
            AnalyzerJob job, AnalysisResult analysisResult, MetricParameters parameters) {
        final AnalyzerResult analyzerResult = getResult(analysisResult, job, metricIdentifier);

        if (metricIdentifier.isFormulaBased()) {
            final ExpressionFactory factory = createExpressionFactory();
            final ELContext context = createContext(factory);

            final List<MetricIdentifier> children = metricIdentifier.getChildren();
            for (MetricIdentifier child : children) {
                final MetricDescriptor childDescriptor = getMetricDescriptor(child, analysisJob, null);
                final AnalyzerJob childAnalyzerJob = getAnalyzerJob(child, analysisJob);
                final MetricParameters childParameters = getParameters(child, childDescriptor, childAnalyzerJob);
                final Number childValue = getMetricValue(child, childDescriptor, analysisJob, childAnalyzerJob,
                        analysisResult, childParameters);
                final String variableName = prepareVariableName(child.getDisplayName());
                context.getELResolver().setValue(context, null, variableName, childValue);
            }

            final String formula = prepareFormula(metricIdentifier.getFormula());

            final ValueExpression valueExpression = factory.createValueExpression(context, formula, Integer.class);
            return (Number) valueExpression.getValue(context);
        } else {
            try {
                return metric.getValue(analyzerResult, parameters);
            } catch (Exception e) {
                // typically this can occur if the job has changed over time and
                // metrics are not resolveable.
                if (logger.isWarnEnabled()) {
                    logger.warn("Failed to get metric value for " + metricIdentifier + " in result of date: "
                            + analysisResult.getCreationDate(), e);
                }
                return null;
            }
        }
    }

    private String prepareVariableName(String variableName) {
        variableName = StringUtils.replaceWhitespaces(variableName, "");
        return variableName;
    }

    private String prepareFormula(String formula) {
        // replace whitespaces to normalize variable names (which MAY contain
        // spaces in the raw input-formula), and to trim before evaluating.
        formula = StringUtils.replaceWhitespaces(formula, "");

        if (formula.indexOf("#{") == -1) {
            formula = "#{" + formula + "}";
        }

        return formula;
    }

    private ELContext createContext(ExpressionFactory factory) {
        return new SimpleContext();
    }

    private ExpressionFactory createExpressionFactory() {
        return new ExpressionFactoryImpl();
    }
}
