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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
    private final Set<DescriptorProviderListener> activeListeners;

    public CompositeDescriptorProvider() {
        delegates = new ArrayList<>();
        activeListeners = new HashSet<>();
    }

    public void addDelegates(final List<DescriptorProvider> descriptorProviders) {
        for (final DescriptorProvider descriptorProvider : descriptorProviders) {
            addDelegate(descriptorProvider);
        }
    }

    public void addDelegate(final DescriptorProvider descriptorProvider) {
        for (final DescriptorProviderListener activeListener : activeListeners) {
            descriptorProvider.addListener(activeListener);
        }
        delegates.add(descriptorProvider);
    }

    @Override
    public void refresh() {
        for (final DescriptorProvider provider : delegates) {
            provider.refresh();
        }
    }

    private void removeDuplicateComponents(final Collection<? extends ComponentDescriptor<?>> col) {
        final Set<String> names = new HashSet<>();
        for (final Iterator<? extends ComponentDescriptor<?>> it = col.iterator(); it.hasNext(); ) {
            final ComponentDescriptor<?> componentDescriptor = it.next();
            final boolean newName = names.add(componentDescriptor.getDisplayName());
            if (!newName) {
                it.remove();
            }
        }
    }

    @Override
    public Collection<AnalyzerDescriptor<?>> getAnalyzerDescriptors() {
        final Collection<AnalyzerDescriptor<?>> col = new ArrayList<>();
        for (final DescriptorProvider provider : delegates) {
            col.addAll(provider.getAnalyzerDescriptors());
        }
        removeDuplicateComponents(col);
        return col;
    }

    @Override
    public <A extends Analyzer<?>> AnalyzerDescriptor<A> getAnalyzerDescriptorForClass(final Class<A> analyzerClass) {
        for (final DescriptorProvider provider : delegates) {
            final AnalyzerDescriptor<A> descriptor = provider.getAnalyzerDescriptorForClass(analyzerClass);
            if (descriptor != null) {
                return descriptor;
            }
        }
        return null;
    }

    @Override
    public AnalyzerDescriptor<?> getAnalyzerDescriptorByDisplayName(final String name) {
        for (final DescriptorProvider provider : delegates) {
            final AnalyzerDescriptor<?> descriptor = provider.getAnalyzerDescriptorByDisplayName(name);
            if (descriptor != null) {
                return descriptor;
            }
        }
        return null;
    }

    @Override
    public ComponentDescriptor<?> getComponentDescriptorByDisplayName(final String name) {
        for (final DescriptorProvider provider : delegates) {
            final ComponentDescriptor<?> descriptor = provider.getComponentDescriptorByDisplayName(name);
            if (descriptor != null) {
                return descriptor;
            }
        }
        return null;
    }

    @Override
    public Collection<TransformerDescriptor<?>> getTransformerDescriptors() {
        final Collection<TransformerDescriptor<?>> col = new ArrayList<>();
        for (final DescriptorProvider provider : delegates) {
            col.addAll(provider.getTransformerDescriptors());
        }
        removeDuplicateComponents(col);
        return col;
    }

    @Override
    public <T extends Transformer> TransformerDescriptor<T> getTransformerDescriptorForClass(
            final Class<T> transformerClass) {
        for (final DescriptorProvider provider : delegates) {
            final TransformerDescriptor<T> descriptor = provider.getTransformerDescriptorForClass(transformerClass);
            if (descriptor != null) {
                return descriptor;
            }
        }
        return null;
    }

    @Override
    public TransformerDescriptor<?> getTransformerDescriptorByDisplayName(final String name) {
        for (final DescriptorProvider provider : delegates) {
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
        for (final DescriptorProvider provider : delegates) {
            col.addAll(provider.getFilterDescriptors());
        }
        removeDuplicateComponents(col);
        return col;
    }

    @Override
    public <F extends Filter<C>, C extends Enum<C>> FilterDescriptor<F, C> getFilterDescriptorForClass(
            final Class<F> filterClass) {
        for (final DescriptorProvider provider : delegates) {
            final FilterDescriptor<F, C> descriptor = provider.getFilterDescriptorForClass(filterClass);
            if (descriptor != null) {
                return descriptor;
            }
        }
        return null;
    }

    @Override
    public FilterDescriptor<?, ?> getFilterDescriptorByDisplayName(final String name) {
        for (final DescriptorProvider provider : delegates) {
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
        for (final ComponentDescriptor<?> componentDescriptor : descriptors) {
            final ComponentSuperCategory superCategory = componentDescriptor.getComponentSuperCategory();
            result.add(superCategory);
        }
        return result;
    }

    @Override
    public Collection<? extends ComponentDescriptor<?>> getComponentDescriptors() {
        final Collection<ComponentDescriptor<?>> col = new ArrayList<>();
        for (final DescriptorProvider provider : delegates) {
            col.addAll(provider.getComponentDescriptors());
        }
        removeDuplicateComponents(col);
        return col;
    }

    @Override
    public Collection<? extends ComponentDescriptor<?>> getComponentDescriptorsOfSuperCategory(
            final ComponentSuperCategory category) {
        final Collection<ComponentDescriptor<?>> col = new ArrayList<>();
        for (final DescriptorProvider provider : delegates) {
            col.addAll(provider.getComponentDescriptorsOfSuperCategory(category));
        }
        removeDuplicateComponents(col);
        return col;
    }

    @Override
    public Collection<RendererBeanDescriptor<?>> getRendererBeanDescriptors() {
        final Collection<RendererBeanDescriptor<?>> col = new ArrayList<>();
        for (final DescriptorProvider provider : delegates) {
            col.addAll(provider.getRendererBeanDescriptors());
        }
        removeDuplicateComponents(col);
        return col;
    }

    @Override
    public <R extends Renderer<?, ?>> RendererBeanDescriptor<R> getRendererBeanDescriptorForClass(
            final Class<R> rendererBeanClass) {
        for (final DescriptorProvider provider : delegates) {
            final RendererBeanDescriptor<R> descriptor = provider.getRendererBeanDescriptorForClass(rendererBeanClass);
            if (descriptor != null) {
                return descriptor;
            }
        }
        return null;
    }

    @Override
    public Collection<RendererBeanDescriptor<?>> getRendererBeanDescriptorsForRenderingFormat(
            final Class<? extends RenderingFormat<?>> renderingFormat) {
        final Collection<RendererBeanDescriptor<?>> col = new ArrayList<>();
        for (final DescriptorProvider provider : delegates) {
            col.addAll(provider.getRendererBeanDescriptorsForRenderingFormat(renderingFormat));
        }
        removeDuplicateComponents(col);
        return col;
    }

    @Override
    public void addListener(final DescriptorProviderListener listener) {
        activeListeners.add(listener);
        for (final DescriptorProvider provider : delegates) {
            provider.addListener(listener);
        }
    }

    @Override
    public void removeListener(final DescriptorProviderListener listener) {
        activeListeners.remove(listener);
        for (final DescriptorProvider provider : delegates) {
            provider.removeListener(listener);
        }
    }

    public ClasspathScanDescriptorProvider findClasspathScanProvider() {
        for (final DescriptorProvider provider : delegates) {
            if (provider instanceof ClasspathScanDescriptorProvider) {
                return (ClasspathScanDescriptorProvider) provider;
            }
        }
        return null;
    }
}
