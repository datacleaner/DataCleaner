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
package org.eobjects.analyzer.descriptors;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import org.eobjects.analyzer.beans.api.Analyzer;
import org.eobjects.analyzer.beans.api.ComponentCategory;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.descriptors.CloseMethodDescriptor;
import org.eobjects.analyzer.descriptors.ComponentDescriptor;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.descriptors.InitializeMethodDescriptor;
import org.eobjects.analyzer.descriptors.MetricDescriptor;
import org.eobjects.analyzer.descriptors.MetricDescriptorImpl;
import org.eobjects.analyzer.descriptors.ProvidedPropertyDescriptor;
import org.eobjects.analyzer.descriptors.ValidateMethodDescriptor;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.job.BeanConfiguration;
import org.eobjects.analyzer.job.Outcome;
import org.eobjects.analyzer.result.AnalysisResult;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.AnalyzerResultReducer;
import org.eobjects.analyzer.result.Metric;
import org.eobjects.analyzer.util.ReflectionUtils;

/**
 * A 'dummy' component job instance to put as a placeholder when rendering
 * "unidentified" result objects, ie. result objects that are serialized outside
 * of a {@link AnalysisResult}
 */
public class UnidentifiedComponentJob implements AnalyzerJob, AnalyzerBeanDescriptor<Analyzer<?>> {

    private static final long serialVersionUID = 1L;

    private final AnalyzerResult _analyzerResult;
    private final Set<MetricDescriptor> _metrics;

    public UnidentifiedComponentJob(AnalyzerResult analyzerResult) {
        _analyzerResult = analyzerResult;
        _metrics = new TreeSet<MetricDescriptor>();

        final Class<? extends AnalyzerResult> resultClass = analyzerResult.getClass();
        final Method[] metricMethods = ReflectionUtils.getMethods(resultClass, Metric.class);
        for (Method method : metricMethods) {
            MetricDescriptor metric = new MetricDescriptorImpl(resultClass, method);
            _metrics.add(metric);
        }
    }

    @Override
    public AnalyzerBeanDescriptor<?> getDescriptor() {
        return this;
    }

    @Override
    public String getName() {
        return "Result";
    }

    @Override
    public int compareTo(ComponentDescriptor<?> o) {
        return -1;
    }

    @Override
    public String getDisplayName() {
        return getName();
    }

    @Override
    public Analyzer<?> newInstance() {
        return null;
    }

    @Override
    public Class<Analyzer<?>> getComponentClass() {
        return null;
    }

    @Override
    public Set<ConfiguredPropertyDescriptor> getConfiguredProperties() {
        return Collections.emptySet();
    }

    @Override
    public Set<ConfiguredPropertyDescriptor> getConfiguredPropertiesByAnnotation(Class<? extends Annotation> annotation) {
        return Collections.emptySet();
    }

    @Override
    public Set<ConfiguredPropertyDescriptor> getConfiguredPropertiesByType(Class<?> type, boolean includeArrays) {
        return Collections.emptySet();
    }

    @Override
    public ConfiguredPropertyDescriptor getConfiguredProperty(String name) {
        return null;
    }

    @Override
    public Set<ValidateMethodDescriptor> getValidateMethods() {
        return Collections.emptySet();
    }

    @Override
    public Set<InitializeMethodDescriptor> getInitializeMethods() {
        return Collections.emptySet();
    }

    @Override
    public Set<CloseMethodDescriptor> getCloseMethods() {
        return Collections.emptySet();
    }

    @Override
    public Set<ProvidedPropertyDescriptor> getProvidedProperties() {
        return Collections.emptySet();
    }

    @Override
    public Set<ProvidedPropertyDescriptor> getProvidedPropertiesByType(Class<?> cls) {
        return Collections.emptySet();
    }

    @Override
    public Class<? extends AnalyzerResult> getResultClass() {
        return _analyzerResult.getClass();
    }

    @Override
    public MetricDescriptor getResultMetric(String name) {
        if (name == null) {
            return null;
        }

        for (MetricDescriptor metric : _metrics) {
            if (name.equals(metric.getName())) {
                return metric;
            }
        }

        // second try - case insensitive
        for (MetricDescriptor metric : _metrics) {
            if (name.equalsIgnoreCase(metric.getName())) {
                return metric;
            }
        }
        return null;
    }

    @Override
    public Set<MetricDescriptor> getResultMetrics() {
        if (_metrics == null) {
            // can happen with deserialized instances only
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(_metrics);
    }

    @Override
    public Class<? extends AnalyzerResultReducer<?>> getResultReducerClass() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public Set<ComponentCategory> getComponentCategories() {
        return Collections.emptySet();
    }

    @Override
    public Set<Annotation> getAnnotations() {
        return Collections.emptySet();
    }

    @Override
    public boolean isDistributable() {
        return false;
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        return null;
    }

    @Override
    public Set<ConfiguredPropertyDescriptor> getConfiguredPropertiesForInput() {
        return Collections.emptySet();
    }

    @Override
    public Set<ConfiguredPropertyDescriptor> getConfiguredPropertiesForInput(boolean includeOptional) {
        return Collections.emptySet();
    }

    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public BeanConfiguration getConfiguration() {
        return null;
    }

    @Override
    public InputColumn<?>[] getInput() {
        return new InputColumn[0];
    }

    @Override
    public Outcome[] getRequirements() {
        return null;
    }

}
