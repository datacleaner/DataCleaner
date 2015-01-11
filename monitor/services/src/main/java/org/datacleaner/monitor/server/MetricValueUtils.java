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
package org.datacleaner.monitor.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;

import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.InputColumn;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.descriptors.HasAnalyzerResultBeanDescriptor;
import org.datacleaner.descriptors.MetricDescriptor;
import org.datacleaner.descriptors.MetricParameters;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.AnalyzerJob;
import org.datacleaner.job.AnalyzerJobHelper;
import org.datacleaner.job.ComponentJob;
import org.datacleaner.job.InputColumnSinkJob;
import org.datacleaner.result.AnalysisResult;
import org.datacleaner.util.CollectionUtils2;
import org.datacleaner.util.LabelUtils;
import org.datacleaner.util.StringUtils;
import org.datacleaner.monitor.job.MetricJobContext;
import org.datacleaner.monitor.job.MetricJobEngine;
import org.datacleaner.monitor.shared.model.MetricGroup;
import org.datacleaner.monitor.shared.model.MetricIdentifier;
import org.apache.metamodel.util.CollectionUtils;
import org.apache.metamodel.util.HasNameMapper;
import org.apache.metamodel.util.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.odysseus.el.ExpressionFactoryImpl;
import de.odysseus.el.util.SimpleContext;

public class MetricValueUtils {

    private static final Logger logger = LoggerFactory.getLogger(MetricValueUtils.class);

    /**
     * Gets the {@link AnalyzerResult} from an {@link AnalysisResult} object,
     * matching a particular {@link AnalyzerJob}. Unlike the plain
     * {@link AnalysisResult#getResult(ComponentJob)} method, this method will
     * apply fuzzy logic to identify the right component, to overcome
     * serialization and deserialization differences.
     * 
     * @param analysisResult
     * @param componentJob
     * @param metricIdentifier
     * @return
     * @throws IllegalArgumentException
     *             if it was not possible to identify a proper
     *             {@link AnalyzerResult} based on the parameters.
     */
    public AnalyzerResult getResult(final AnalysisResult analysisResult, final ComponentJob componentJob,
            final MetricIdentifier metricIdentifier) throws IllegalArgumentException {
        AnalyzerResult result = null;
        try {
            result = analysisResult.getResult(componentJob);
        } catch (Exception e) {
            // We are extra careful here because there has been a history of
            // bugs in proper retrieving analyzerJobs, related to comparison of
            // MetaModel schema objects which are deserialized etc.
            if (logger.isWarnEnabled()) {
                logger.warn("An error occurred while retrieving the AnalyzerResult of : " + componentJob, e);
            }
        }
        if (result == null) {
            logger.debug("Could not resolve AnalyzerResult using key={}, reiterating using non-exact matching", componentJob);
            result = getResultFuzzy(analysisResult, componentJob, metricIdentifier);
        } else {
            logger.debug("Resolved AnalyzerResult using key={}", componentJob);
        }
        return result;
    }

    private ComponentJob getComponentJobFuzzy(Collection<ComponentJob> componentJobs, final ComponentJob componentJob,
            final MetricIdentifier metricIdentifier) {
        List<ComponentJob> candidates = new ArrayList<ComponentJob>(componentJobs);

        final String analyzerJobName;
        final String componentJobDescriptorName;
        if (componentJob == null) {
            componentJobDescriptorName = metricIdentifier.getAnalyzerDescriptorName();
            analyzerJobName = metricIdentifier.getAnalyzerName();
        } else {
            componentJobDescriptorName = componentJob.getDescriptor().getDisplayName();
            analyzerJobName = componentJob.getName();
        }

        // filter analyzers of the corresponding type
        candidates = CollectionUtils.filter(candidates, new Predicate<ComponentJob>() {
            @Override
            public Boolean eval(ComponentJob o) {
                final String actualDescriptorName = o.getDescriptor().getDisplayName();
                final String metricDescriptorName = componentJobDescriptorName;
                return metricDescriptorName.equals(actualDescriptorName);
            }
        });

        if (!StringUtils.isNullOrEmpty(analyzerJobName)) {
            // filter analyzers with a particular name
            candidates = CollectionUtils2.refineCandidates(candidates, new Predicate<ComponentJob>() {
                @Override
                public Boolean eval(ComponentJob o) {
                    final String actualAnalyzerName = o.getName();
                    final String metricAnalyzerName = analyzerJobName;
                    return metricAnalyzerName.equals(actualAnalyzerName);
                }
            });
        }

        if (componentJob instanceof InputColumnSinkJob) {
            // filter analyzer jobs with same input
            candidates = CollectionUtils2.refineCandidates(candidates, new Predicate<ComponentJob>() {
                @Override
                public Boolean eval(ComponentJob o) {
                    if (o instanceof InputColumnSinkJob) {
                        final InputColumn<?>[] input1 = ((InputColumnSinkJob) o).getInput();
                        final InputColumn<?>[] input2 = ((InputColumnSinkJob) componentJob).getInput();
                        final String actualAnalyzerInputNames = CollectionUtils.map(input1, new HasNameMapper()).toString();
                        final String metricAnalyzerInputNames = CollectionUtils.map(input2, new HasNameMapper()).toString();
                        return metricAnalyzerInputNames.equals(actualAnalyzerInputNames);
                    }
                    return false;
                }
            });
        }

        // filter analyzer jobs with input matching the metric
        final String analyzerInputName = metricIdentifier.getAnalyzerInputName();
        if (analyzerInputName != null) {
            candidates = CollectionUtils2.refineCandidates(candidates, new Predicate<ComponentJob>() {
                @Override
                public Boolean eval(ComponentJob o) {
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

        final ComponentJob candidate = candidates.iterator().next();
        return candidate;
    }

    private AnalyzerResult getResultFuzzy(final AnalysisResult analysisResult, final ComponentJob componentJob,
            final MetricIdentifier metricIdentifier) throws IllegalArgumentException {
        final Collection<ComponentJob> componentJobs = analysisResult.getResultMap().keySet();

        final ComponentJob candidate = getComponentJobFuzzy(componentJobs, componentJob, metricIdentifier);

        if (logger.isDebugEnabled()) {
            int candidateHash = candidate.hashCode();
            int keyHash = componentJob.hashCode();
            boolean equals = candidate.equals(componentJob);
            logger.debug("Result of fuzzy result lookup: Equals={}, CandidateHash={}, KeyHash={}", new Object[] { equals,
                    candidateHash, keyHash });
        }

        return analysisResult.getResult(candidate);
    }

    /**
     * Gets the {@link ComponentJob} that applies to a specific metric.
     * 
     * @param metric
     *            the metric to query for
     * @param analysisJob
     *            the analysis job to look into, or null if not available
     * @param analysisResult
     *            the analysis result to look into, or null if not available
     * @return
     */
    public ComponentJob getComponentJob(MetricIdentifier metric, final AnalysisJob analysisJob, AnalysisResult analysisResult) {
        final MetricIdentifier metricIdentifier;
        if (metric.isFormulaBased()) {
            metricIdentifier = findSingularMetricIdentifierOfFormula(metric);
        } else {
            metricIdentifier = metric;
        }

        if (analysisJob == null) {
            final Set<ComponentJob> componentJobs = analysisResult.getResultMap().keySet();
            final ComponentJob analyzer = getComponentJobFuzzy(componentJobs, null, metricIdentifier);
            return analyzer;
        }

        final AnalyzerJobHelper analyzerJobHelper = new AnalyzerJobHelper(analysisJob);
        final AnalyzerJob analyzerJob = analyzerJobHelper.getAnalyzerJob(metricIdentifier.getAnalyzerDescriptorName(),
                metric.getAnalyzerName(), metric.getAnalyzerInputName());
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
            final ComponentJob componentJob, AnalysisResult analysisResult) {
        if (metricIdentifier.isFormulaBased()) {
            return null;
        }

        final ComponentJob componentJobToUse;
        if (componentJob == null) {
            // analyzer job has not been specified yet, probably because this
            // metric is a child to a formula based metric
            componentJobToUse = getComponentJob(metricIdentifier, analysisJob, analysisResult);
        } else {
            componentJobToUse = componentJob;
        }

        final ComponentDescriptor<?> componentDescriptor = componentJobToUse.getDescriptor();

        if (componentDescriptor instanceof HasAnalyzerResultBeanDescriptor) {
            HasAnalyzerResultBeanDescriptor<?> hasAnalyzerResultBeanDescriptor = (HasAnalyzerResultBeanDescriptor<?>) componentDescriptor;
            final MetricDescriptor metric = hasAnalyzerResultBeanDescriptor.getResultMetric(metricIdentifier
                    .getMetricDescriptorName());

            if (metric == null) {
                logger.error("Did not find any metric descriptors with name '{}' in {}",
                        metricIdentifier.getMetricDescriptorName(), hasAnalyzerResultBeanDescriptor.getResultClass());
            }
            return metric;
        }

        return null;
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

    public MetricParameters getParameters(final MetricJobEngine<?> jobEngine, final MetricJobContext job,
            final MetricIdentifier metricIdentifier, final MetricDescriptor metricDescriptor, final ComponentJob componentJob) {
        final String queryString;
        final InputColumn<?> queryInputColumn;

        final String paramQueryString = metricIdentifier.getParamQueryString();
        if (paramQueryString == null) {
            queryString = null;
        } else {
            queryString = paramQueryString;
        }

        final String paramColumnName = metricIdentifier.getParamColumnName();
        if (StringUtils.isNullOrEmpty(paramColumnName)) {
            queryInputColumn = null;
        } else {
            final Collection<InputColumn<?>> inputColumns = jobEngine.getMetricParameterColumns(job, componentJob);
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

    public Number getMetricValue(MetricJobEngine<?> jobEngine, MetricJobContext jobContext, MetricIdentifier metricIdentifier,
            MetricDescriptor metric, AnalysisJob analysisJob, ComponentJob componentJob, AnalysisResult analysisResult,
            MetricParameters parameters) {

        final AnalyzerResult analyzerResult;

        try {
            analyzerResult = getResult(analysisResult, componentJob, metricIdentifier);
        } catch (IllegalArgumentException e) {
            // typically this can occur if the job has changed over time and
            // metrics are not resolveable.
            if (logger.isWarnEnabled()) {
                logger.warn(
                        "Failed to get analyzer result for " + metricIdentifier + " in result of date: "
                                + analysisResult.getCreationDate(), e);
            }
            return null;
        }

        if (metricIdentifier.isFormulaBased()) {
            final ExpressionFactory factory = createExpressionFactory();
            final ELContext context = createContext(factory);

            final List<MetricIdentifier> children = metricIdentifier.getChildren();
            for (MetricIdentifier child : children) {
                final MetricDescriptor childDescriptor = getMetricDescriptor(child, analysisJob, null, analysisResult);
                final ComponentJob childComponentJob = getComponentJob(child, analysisJob, null);
                final MetricParameters childParameters = getParameters(jobEngine, jobContext, child, childDescriptor,
                        childComponentJob);
                final Number childValue = getMetricValue(jobEngine, jobContext, child, childDescriptor, analysisJob,
                        childComponentJob, analysisResult, childParameters);
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
                    logger.warn(
                            "Failed to get metric value for " + metricIdentifier + " in result of date: "
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

    /**
     * Builds a list of {@link MetricGroup}s for a specific {@link AnalysisJob}.
     * 
     * @param jobContext
     * @param analysisJob
     * 
     * @return
     */
    public List<MetricGroup> getMetricGroups(MetricJobContext jobContext, AnalysisJob analysisJob) {
        final Collection<AnalyzerJob> analyzerJobs = analysisJob.getAnalyzerJobs();

        final List<MetricGroup> metricGroups = new ArrayList<MetricGroup>();
        for (AnalyzerJob analyzerJob : analyzerJobs) {
            final Set<MetricDescriptor> metricDescriptors = analyzerJob.getDescriptor().getResultMetrics();
            final MetricGroup metricGroup = getMetricGroup(jobContext, analyzerJob, metricDescriptors);
            if (metricGroup != null) {
                metricGroups.add(metricGroup);
            }
        }
        return metricGroups;
    }

    public List<String> getInputColumnNames(AnalyzerJob analyzerJob) {
        final List<String> columnNames = new ArrayList<String>();
        final Set<ConfiguredPropertyDescriptor> inputProperties = analyzerJob.getDescriptor().getConfiguredPropertiesForInput(
                false);
        for (ConfiguredPropertyDescriptor inputProperty : inputProperties) {
            final Object input = analyzerJob.getConfiguration().getProperty(inputProperty);
            if (input instanceof InputColumn) {
                String columnName = ((InputColumn<?>) input).getName();
                columnNames.add(columnName);
            } else if (input instanceof InputColumn[]) {
                InputColumn<?>[] inputColumns = (InputColumn<?>[]) input;
                for (InputColumn<?> inputColumn : inputColumns) {
                    String columnName = inputColumn.getName();
                    if (!columnNames.contains(columnName)) {
                        columnNames.add(columnName);
                    }
                }
            }
        }
        return columnNames;
    }

    /**
     * Builds a {@link MetricGroup} for a specific {@link ComponentJob}.
     * 
     * @param job
     * @param componentJob
     * @param metricDescriptors
     * @return the built {@link MetricGroup}, or null if there was no metrics to
     *         build
     */
    public MetricGroup getMetricGroup(MetricJobContext job, ComponentJob componentJob, Set<MetricDescriptor> metricDescriptors) {
        if (metricDescriptors == null || metricDescriptors.isEmpty()) {
            return null;
        }
        final String label = LabelUtils.getLabel(componentJob);
        final InputColumn<?> identifyingInputColumn = AnalyzerJobHelper.getIdentifyingInputColumn(componentJob);

        final List<MetricIdentifier> metricIdentifiers = new ArrayList<MetricIdentifier>();

        // Represents any of the column parameterized metrics. We assume that
        // the column-set is the same for all metrics, since they originate from
        // the same ComponentJob.
        MetricIdentifier columnParameterizedMetric = null;

        for (MetricDescriptor metricDescriptor : metricDescriptors) {
            final MetricIdentifier metricIdentifier = new MetricIdentifier();
            metricIdentifier.setAnalyzerDescriptorName(componentJob.getDescriptor().getDisplayName());
            metricIdentifier.setAnalyzerName(componentJob.getName());
            if (identifyingInputColumn != null) {
                metricIdentifier.setAnalyzerInputName(identifyingInputColumn.getName());
            }
            metricIdentifier.setMetricDescriptorName(metricDescriptor.getName());
            metricIdentifier.setParameterizedByColumnName(metricDescriptor.isParameterizedByInputColumn());
            metricIdentifier.setParameterizedByQueryString(metricDescriptor.isParameterizedByString());

            if (metricIdentifier.isParameterizedByColumnName()) {
                columnParameterizedMetric = metricIdentifier;
            }

            metricIdentifiers.add(metricIdentifier);
        }

        final List<String> columnNames;
        if (columnParameterizedMetric != null) {
            final MetricJobEngine<? extends MetricJobContext> jobEngine = job.getJobEngine();
            final Collection<InputColumn<?>> columns = jobEngine.getMetricParameterColumns(job, componentJob);
            columnNames = CollectionUtils.map(columns, new HasNameMapper());
        } else {
            columnNames = Collections.emptyList();
        }

        final MetricGroup metricGroup = new MetricGroup();
        metricGroup.setName(label);
        metricGroup.setMetrics(metricIdentifiers);
        metricGroup.setColumnNames(columnNames);
        return metricGroup;
    }
}
