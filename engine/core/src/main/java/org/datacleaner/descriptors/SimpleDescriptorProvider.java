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
package org.datacleaner.descriptors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.datacleaner.api.Analyzer;
import org.datacleaner.api.Filter;
import org.datacleaner.api.Renderer;
import org.datacleaner.api.Transformer;

/**
 * A simple descriptor provider with a method signature suitable externalizing
 * class names of analyzer and transformer beans. For example, if you're using
 * the Spring Framework you initialize this descriptor provider as follows:
 * 
 * <pre>
 * &lt;bean id="descriptorProvider" class="org.datacleaner.descriptors.SimpleDescriptorProvider"&gt;
 *   &lt;property name="analyzerClassNames"&gt;
 *     &lt;list&gt;
 *       &lt;value&gt;org.datacleaner.beans.StringAnalyzer&lt;/value&gt;
 *       &lt;value&gt;org.datacleaner.beans.valuedist.ValueDistributionAnalyzer&lt;/value&gt;
 *       ...
 *     &lt;/list&gt;
 *   &lt;/property&gt;
 *   &lt;property name="transformerClassNames"&gt;
 *     &lt;list&gt;
 *       &lt;value&gt;org.datacleaner.beans.TokenizerTransformer&lt;/value&gt;
 *       ...
 *     &lt;/list&gt;
 *   &lt;/property&gt;
 *   &lt;property name="rendererClassNames"&gt;
 *     &lt;list&gt;
 *       &lt;value&gt;org.datacleaner.result.renderer.DefaultTextRenderer&lt;/value&gt;
 *       ...
 *     &lt;/list&gt;
 *   &lt;/property&gt;
 * &lt;/bean&gt;
 * </pre>
 * 
 * 
 */
public class SimpleDescriptorProvider extends AbstractDescriptorProvider {

    private List<AnalyzerDescriptor<?>> _analyzerBeanDescriptors = new ArrayList<AnalyzerDescriptor<?>>();
    private List<TransformerDescriptor<?>> _transformerBeanDescriptors = new ArrayList<TransformerDescriptor<?>>();
    private List<RendererBeanDescriptor<?>> _rendererBeanDescriptors = new ArrayList<RendererBeanDescriptor<?>>();
    private List<FilterDescriptor<?, ?>> _filterBeanDescriptors = new ArrayList<FilterDescriptor<?, ?>>();

    public SimpleDescriptorProvider() {
        this(true);
    }

    public SimpleDescriptorProvider(boolean autoDiscover) {
        super(autoDiscover);
    }

    public void addAnalyzerBeanDescriptor(AnalyzerDescriptor<?> analyzerBeanDescriptor) {
        _analyzerBeanDescriptors.add(analyzerBeanDescriptor);
    }

    public void addTransformerBeanDescriptor(TransformerDescriptor<?> transformerBeanDescriptor) {
        _transformerBeanDescriptors.add(transformerBeanDescriptor);
    }

    public void addRendererBeanDescriptor(RendererBeanDescriptor<?> rendererBeanDescriptor) {
        _rendererBeanDescriptors.add(rendererBeanDescriptor);
    }

    public void addFilterBeanDescriptor(FilterDescriptor<?, ?> descriptor) {
        _filterBeanDescriptors.add(descriptor);
    }

    @Override
    public List<AnalyzerDescriptor<?>> getAnalyzerDescriptors() {
        return _analyzerBeanDescriptors;
    }

    public void setAnalyzerBeanDescriptors(List<AnalyzerDescriptor<?>> descriptors) {
        _analyzerBeanDescriptors = descriptors;
    }

    @Override
    public List<TransformerDescriptor<?>> getTransformerDescriptors() {
        return _transformerBeanDescriptors;
    }

    public void setTransformerBeanDescriptors(List<TransformerDescriptor<?>> transformerBeanDescriptors) {
        _transformerBeanDescriptors = transformerBeanDescriptors;
    }

    @Override
    public List<RendererBeanDescriptor<?>> getRendererBeanDescriptors() {
        return _rendererBeanDescriptors;
    }

    public void setRendererBeanDescriptors(List<RendererBeanDescriptor<?>> rendererBeanDescriptors) {
        _rendererBeanDescriptors = rendererBeanDescriptors;
    }

    @Override
    public Collection<FilterDescriptor<?, ?>> getFilterDescriptors() {
        return _filterBeanDescriptors;
    }

    public void setFilterBeanDescriptors(List<FilterDescriptor<?, ?>> filterBeanDescriptors) {
        _filterBeanDescriptors = filterBeanDescriptors;
    }

    public void setAnalyzerClassNames(Collection<String> classNames) throws ClassNotFoundException {
        for (String className : classNames) {
            @SuppressWarnings("unchecked")
            Class<? extends Analyzer<?>> c = (Class<? extends Analyzer<?>>) Class.forName(className);
            AnalyzerDescriptor<?> descriptor = getAnalyzerDescriptorForClass(c);
            if (descriptor == null || !_analyzerBeanDescriptors.contains(descriptor)) {
                addAnalyzerBeanDescriptor(Descriptors.ofAnalyzer(c));
            }
        }
    }

    public void setTransformerClassNames(Collection<String> classNames) throws ClassNotFoundException {
        for (String className : classNames) {
            @SuppressWarnings("unchecked")
            Class<? extends Transformer> c = (Class<? extends Transformer>) Class.forName(className);
            TransformerDescriptor<?> descriptor = getTransformerDescriptorForClass(c);
            if (descriptor == null || !_transformerBeanDescriptors.contains(descriptor)) {
                addTransformerBeanDescriptor(Descriptors.ofTransformer(c));
            }
        }
    }

    public void setRendererClassNames(Collection<String> classNames) throws ClassNotFoundException {
        for (String className : classNames) {
            @SuppressWarnings("unchecked")
            Class<? extends Renderer<?, ?>> c = (Class<? extends Renderer<?, ?>>) Class.forName(className);
            RendererBeanDescriptor<?> descriptor = getRendererBeanDescriptorForClass(c);
            if (descriptor == null || !_rendererBeanDescriptors.contains(descriptor)) {
                addRendererBeanDescriptor(Descriptors.ofRenderer(c));
            }
        }
    }

    public void setFilterClassNames(Collection<String> classNames) throws ClassNotFoundException {
        for (String className : classNames) {
            @SuppressWarnings("unchecked")
            Class<? extends Filter<?>> c = (Class<? extends Filter<?>>) Class.forName(className);

            FilterDescriptor<?, ?> descriptor = getFilterBeanDescriptorForClassUnbounded(c);

            if (descriptor == null || !_filterBeanDescriptors.contains(descriptor)) {
                addFilterBeanDescriptor(Descriptors.ofFilterUnbound(c));
            }
        }
    }

}
