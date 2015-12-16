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

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections.collection.CompositeCollection;
import org.datacleaner.api.Analyzer;
import org.datacleaner.api.ComponentSuperCategory;
import org.datacleaner.api.Filter;
import org.datacleaner.api.Renderer;
import org.datacleaner.api.RenderingFormat;
import org.datacleaner.api.Transformer;

/**
 * DescriptorProvider that provides a composite view of descriptors from 2
 * delegate providers.
 */
public class CompositeDescriptorProvider implements DescriptorProvider {

    private final DescriptorProvider delegate1;
    private final DescriptorProvider delegate2;

    public CompositeDescriptorProvider(DescriptorProvider delegate1, DescriptorProvider delegate2) {
        this.delegate1 = delegate1;
        this.delegate2 = delegate2;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<AnalyzerDescriptor<?>> getAnalyzerDescriptors() {
        return new CompositeCollection(
                new Collection[] { delegate1.getAnalyzerDescriptors(), delegate2.getAnalyzerDescriptors() });
    }

    @Override
    public <A extends Analyzer<?>> AnalyzerDescriptor<A> getAnalyzerDescriptorForClass(Class<A> analyzerClass) {
        AnalyzerDescriptor<A> result = delegate1.getAnalyzerDescriptorForClass(analyzerClass);
        if (result != null) {
            return result;
        }
        return delegate2.getAnalyzerDescriptorForClass(analyzerClass);
    }

    @Override
    public AnalyzerDescriptor<?> getAnalyzerDescriptorByDisplayName(String name) {
        AnalyzerDescriptor<?> result = delegate1.getAnalyzerDescriptorByDisplayName(name);
        if (result != null) {
            return result;
        }
        return delegate2.getAnalyzerDescriptorByDisplayName(name);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<TransformerDescriptor<?>> getTransformerDescriptors() {
        return new CompositeCollection(
                new Collection[] { delegate1.getTransformerDescriptors(), delegate2.getTransformerDescriptors() });
    }

    @Override
    public <T extends Transformer> TransformerDescriptor<T> getTransformerDescriptorForClass(
            Class<T> transformerClass) {
        TransformerDescriptor<T> result = delegate1.getTransformerDescriptorForClass(transformerClass);
        if (result != null) {
            return result;
        }
        return delegate2.getTransformerDescriptorForClass(transformerClass);
    }

    @Override
    public TransformerDescriptor<?> getTransformerDescriptorByDisplayName(String name) {
        TransformerDescriptor<?> result = delegate1.getTransformerDescriptorByDisplayName(name);
        if (result != null) {
            return result;
        }
        return delegate2.getTransformerDescriptorByDisplayName(name);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<FilterDescriptor<?, ?>> getFilterDescriptors() {
        return new CompositeCollection(
                new Collection[] { delegate1.getFilterDescriptors(), delegate2.getFilterDescriptors() });
    }

    @Override
    public <F extends Filter<C>, C extends Enum<C>> FilterDescriptor<F, C> getFilterDescriptorForClass(
            Class<F> filterClass) {
        FilterDescriptor<F, C> result = delegate1.getFilterDescriptorForClass(filterClass);
        if (result != null) {
            return result;
        }
        return delegate2.getFilterDescriptorForClass(filterClass);
    }

    @Override
    public FilterDescriptor<?, ?> getFilterDescriptorByDisplayName(String name) {
        FilterDescriptor<?, ?> result = delegate1.getFilterDescriptorByDisplayName(name);
        if (result != null) {
            return result;
        }
        return delegate2.getFilterDescriptorByDisplayName(name);
    }

    @Override
    public Set<ComponentSuperCategory> getComponentSuperCategories() {
        final Set<ComponentSuperCategory> result = new TreeSet<>();
        final Collection<? extends ComponentDescriptor<?>> descriptors = getComponentDescriptors();
        for (ComponentDescriptor<?> componentDescriptor : descriptors) {
            ComponentSuperCategory superCategory = componentDescriptor.getComponentSuperCategory();
            result.add(superCategory);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<? extends ComponentDescriptor<?>> getComponentDescriptors() {
        return new CompositeCollection(
                new Collection[] { delegate1.getComponentDescriptors(), delegate2.getComponentDescriptors() });
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<? extends ComponentDescriptor<?>> getComponentDescriptorsOfSuperCategory(
            ComponentSuperCategory category) {
        return new CompositeCollection(new Collection[] { delegate1.getComponentDescriptorsOfSuperCategory(category),
                delegate2.getComponentDescriptorsOfSuperCategory(category) });
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<RendererBeanDescriptor<?>> getRendererBeanDescriptors() {
        return new CompositeCollection(
                new Collection[] { delegate1.getRendererBeanDescriptors(), delegate2.getRendererBeanDescriptors() });
    }

    @Override
    public <R extends Renderer<?, ?>> RendererBeanDescriptor<R> getRendererBeanDescriptorForClass(
            Class<R> rendererBeanClass) {
        RendererBeanDescriptor<R> result = delegate1.getRendererBeanDescriptorForClass(rendererBeanClass);
        if (result != null) {
            return result;
        }
        return delegate2.getRendererBeanDescriptorForClass(rendererBeanClass);
    }

    @Override
    public Collection<RendererBeanDescriptor<?>> getRendererBeanDescriptorsForRenderingFormat(
            Class<? extends RenderingFormat<?>> renderingFormat) {
        Collection<RendererBeanDescriptor<?>> result = delegate1
                .getRendererBeanDescriptorsForRenderingFormat(renderingFormat);
        if (result != null && !result.isEmpty()) {
            return result;
        }
        return delegate2.getRendererBeanDescriptorsForRenderingFormat(renderingFormat);
    }

    @Override
    public void addComponentDescriptorsUpdatedListener(ComponentDescriptorsUpdatedListener listener) {
        delegate1.addComponentDescriptorsUpdatedListener(listener);
        delegate2.addComponentDescriptorsUpdatedListener(listener);
    }

    @Override
    public void removeComponentDescriptorsUpdatedListener(ComponentDescriptorsUpdatedListener listener) {
        delegate1.removeComponentDescriptorsUpdatedListener(listener);
        delegate2.removeComponentDescriptorsUpdatedListener(listener);
    }

    public ClasspathScanDescriptorProvider findClasspathScanProvider() {
        ClasspathScanDescriptorProvider result = findClasspathScanProvider(delegate1);
        if (result != null) {
            return result;
        }
        return findClasspathScanProvider(delegate2);
    }

    private ClasspathScanDescriptorProvider findClasspathScanProvider(DescriptorProvider delegate) {
        if (delegate instanceof ClasspathScanDescriptorProvider) {
            return (ClasspathScanDescriptorProvider) delegate;
        } else if (delegate instanceof CompositeDescriptorProvider) {
            return ((CompositeDescriptorProvider) delegate).findClasspathScanProvider();
        }
        return null;
    }
}
