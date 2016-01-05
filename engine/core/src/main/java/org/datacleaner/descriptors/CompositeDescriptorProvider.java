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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.datacleaner.api.Analyzer;
import org.datacleaner.api.ComponentSuperCategory;
import org.datacleaner.api.Filter;
import org.datacleaner.api.Renderer;
import org.datacleaner.api.RenderingFormat;
import org.datacleaner.api.Transformer;

/**
 * DescriptorProvider that provides a composite view of descriptors from a list
 * of delegate providers.
 */
public class CompositeDescriptorProvider implements DescriptorProvider {

    private final List<DescriptorProvider> delegates;

    public CompositeDescriptorProvider(DescriptorProvider delegate1, DescriptorProvider delegate2) {
        this(Arrays.asList(delegate1, delegate2));
    }

    public CompositeDescriptorProvider(List<DescriptorProvider> delegates) {
        this.delegates = delegates;
    }

    public void refresh() {
        for (DescriptorProvider provider : delegates) {
            provider.refresh();
        }
    }

    @Override
    public Collection<AnalyzerDescriptor<?>> getAnalyzerDescriptors() {
        final Collection<AnalyzerDescriptor<?>> col = new ArrayList<>();
        for (DescriptorProvider provider : delegates) {
            col.addAll(provider.getAnalyzerDescriptors());
        }
        return col;
    }

    @Override
    public <A extends Analyzer<?>> AnalyzerDescriptor<A> getAnalyzerDescriptorForClass(Class<A> analyzerClass) {
        for (DescriptorProvider provider : delegates) {
            final AnalyzerDescriptor<A> descriptor = provider.getAnalyzerDescriptorForClass(analyzerClass);
            if (descriptor != null) {
                return descriptor;
            }
        }
        return null;
    }

    @Override
    public AnalyzerDescriptor<?> getAnalyzerDescriptorByDisplayName(String name) {
        for (DescriptorProvider provider : delegates) {
            final AnalyzerDescriptor<?> descriptor = provider.getAnalyzerDescriptorByDisplayName(name);
            if (descriptor != null) {
                return descriptor;
            }
        }
        return null;
    }

    @Override
    public Collection<TransformerDescriptor<?>> getTransformerDescriptors() {
        final Collection<TransformerDescriptor<?>> col = new ArrayList<>();
        for (DescriptorProvider provider : delegates) {
            col.addAll(provider.getTransformerDescriptors());
        }
        return col;
    }

    @Override
    public <T extends Transformer> TransformerDescriptor<T> getTransformerDescriptorForClass(
            Class<T> transformerClass) {
        for (DescriptorProvider provider : delegates) {
            final TransformerDescriptor<T> descriptor = provider.getTransformerDescriptorForClass(transformerClass);
            if (descriptor != null) {
                return descriptor;
            }
        }
        return null;
    }

    @Override
    public TransformerDescriptor<?> getTransformerDescriptorByDisplayName(String name) {
        for (DescriptorProvider provider : delegates) {
            final TransformerDescriptor<?> descriptor = provider.getTransformerDescriptorByDisplayName(name);
            if (descriptor != null) {
                return descriptor;
            }
        }
        return null;
    }

    @Override
    public Collection<FilterDescriptor<?, ?>> getFilterDescriptors() {
        final Collection<FilterDescriptor<?, ?>> col = new ArrayList<>();
        for (DescriptorProvider provider : delegates) {
            col.addAll(provider.getFilterDescriptors());
        }
        return col;
    }

    @Override
    public <F extends Filter<C>, C extends Enum<C>> FilterDescriptor<F, C> getFilterDescriptorForClass(
            Class<F> filterClass) {
        for (DescriptorProvider provider : delegates) {
            final FilterDescriptor<F, C> descriptor = provider.getFilterDescriptorForClass(filterClass);
            if (descriptor != null) {
                return descriptor;
            }
        }
        return null;
    }

    @Override
    public FilterDescriptor<?, ?> getFilterDescriptorByDisplayName(String name) {
        for (DescriptorProvider provider : delegates) {
            final FilterDescriptor<?, ?> descriptor = provider.getFilterDescriptorByDisplayName(name);
            if (descriptor != null) {
                return descriptor;
            }
        }
        return null;
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

    @Override
    public Collection<? extends ComponentDescriptor<?>> getComponentDescriptors() {
        final Collection<ComponentDescriptor<?>> col = new ArrayList<>();
        for (DescriptorProvider provider : delegates) {
            col.addAll(provider.getComponentDescriptors());
        }
        return col;
    }

    @Override
    public Collection<? extends ComponentDescriptor<?>> getComponentDescriptorsOfSuperCategory(
            ComponentSuperCategory category) {
        final Collection<ComponentDescriptor<?>> col = new ArrayList<>();
        for (DescriptorProvider provider : delegates) {
            col.addAll(provider.getComponentDescriptorsOfSuperCategory(category));
        }
        return col;
    }

    @Override
    public Collection<RendererBeanDescriptor<?>> getRendererBeanDescriptors() {
        final Collection<RendererBeanDescriptor<?>> col = new ArrayList<>();
        for (DescriptorProvider provider : delegates) {
            col.addAll(provider.getRendererBeanDescriptors());
        }
        return col;
    }

    @Override
    public <R extends Renderer<?, ?>> RendererBeanDescriptor<R> getRendererBeanDescriptorForClass(
            Class<R> rendererBeanClass) {
        for (DescriptorProvider provider : delegates) {
            final RendererBeanDescriptor<R> descriptor = provider.getRendererBeanDescriptorForClass(rendererBeanClass);
            if (descriptor != null) {
                return descriptor;
            }
        }
        return null;
    }

    @Override
    public Collection<RendererBeanDescriptor<?>> getRendererBeanDescriptorsForRenderingFormat(
            Class<? extends RenderingFormat<?>> renderingFormat) {
        final Collection<RendererBeanDescriptor<?>> col = new ArrayList<>();
        for (DescriptorProvider provider : delegates) {
            col.addAll(provider.getRendererBeanDescriptorsForRenderingFormat(renderingFormat));
        }
        return col;
    }

    @Override
    public void addListener(DescriptorProviderListener listener) {
        for (DescriptorProvider provider : delegates) {
            provider.addListener(listener);
        }
    }

    @Override
    public void removeListener(DescriptorProviderListener listener) {
        for (DescriptorProvider provider : delegates) {
            provider.removeListener(listener);
        }
    }

    public ClasspathScanDescriptorProvider findClasspathScanProvider() {
        for (DescriptorProvider provider : delegates) {
            if (provider instanceof ClasspathScanDescriptorProvider) {
                return (ClasspathScanDescriptorProvider) provider;
            }
        }
        return null;
    }

    public Map<DescriptorProvider, DescriptorProviderStatus> getProviderStatusMap() {
        Map<DescriptorProvider, DescriptorProviderStatus> statusMap = new HashMap<>();
        for (DescriptorProvider provider : delegates) {
            statusMap.putAll(provider.getProviderStatusMap());
        }
        return statusMap;
    }
}
