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
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.ArrayUtils;
import org.datacleaner.api.Analyzer;
import org.datacleaner.api.ComponentSuperCategory;
import org.datacleaner.api.Filter;
import org.datacleaner.api.Renderer;
import org.datacleaner.api.RenderingFormat;
import org.datacleaner.api.Transformer;

import com.google.common.collect.Iterables;

/**
 * Abstract descriptor provider implementation that implements most trivial
 * methods.
 */
public abstract class AbstractDescriptorProvider implements DescriptorProvider {

    private final boolean _autoDiscover;
    private final Collection<DescriptorProviderListener> _listeners;

    /**
     * Creates an {@link AbstractDescriptorProvider}
     *
     * @param autoLoadDescriptorClasses
     *            whether or not to automatically load descriptors when they are
     *            requested by class names. This typically happens in
     *            {@link #getAnalyzerDescriptorForClass(Class)},
     *            {@link #getTransformerDescriptorForClass(Class)} or
     *            {@link #getFilterDescriptorForClass(Class)}
     */
    public AbstractDescriptorProvider(final boolean autoLoadDescriptorClasses) {
        _autoDiscover = autoLoadDescriptorClasses;
        _listeners = new ArrayList<>();
    }

    @Override
    public final AnalyzerDescriptor<?> getAnalyzerDescriptorByDisplayName(final String name) {
        return getComponentDescriptorByDisplayName(name, getAnalyzerDescriptors(), true, true);
    }

    @Override
    public ComponentDescriptor<?> getComponentDescriptorByDisplayName(final String name) {
        final Iterable<ComponentDescriptor<?>> allComponentDescriptors =
                Iterables.concat(getAnalyzerDescriptors(), getTransformerDescriptors(), getFilterDescriptors());
        ComponentDescriptor<?> match = getComponentDescriptorByDisplayName(name, allComponentDescriptors, true, false);
        if (match == null) {
            match = getComponentDescriptorByDisplayName(name, allComponentDescriptors, false, true);
        }
        return match;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <A extends Analyzer<?>> AnalyzerDescriptor<A> getAnalyzerDescriptorForClass(
            final Class<A> analyzerBeanClass) {
        for (final AnalyzerDescriptor<?> descriptor : getAnalyzerDescriptors()) {
            if (descriptor.getComponentClass() == analyzerBeanClass) {
                return (AnalyzerDescriptor<A>) descriptor;
            }
        }
        return notFoundAnalyzer(analyzerBeanClass);
    }

    @Override
    public final FilterDescriptor<?, ?> getFilterDescriptorByDisplayName(final String name) {
        return getComponentDescriptorByDisplayName(name, getFilterDescriptors(), true, true);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <F extends Filter<C>, C extends Enum<C>> FilterDescriptor<F, C> getFilterDescriptorForClass(
            final Class<F> filterClass) {
        return (FilterDescriptor<F, C>) getFilterBeanDescriptorForClassUnbounded(filterClass);
    }

    /**
     * Alternative getter method used when sufficient type-information about the
     * class is not available.
     *
     * This method is basically a hack to make the compiler happy, see Ticket
     * #417.
     *
     * @see http://eobjects.org/trac/ticket/417
     *
     * @param filterClass
     * @return
     */
    protected final FilterDescriptor<?, ?> getFilterBeanDescriptorForClassUnbounded(final Class<?> filterClass) {
        for (final FilterDescriptor<?, ?> descriptor : getFilterDescriptors()) {
            if (filterClass == descriptor.getComponentClass()) {
                return descriptor;
            }
        }
        return notFoundFilter(filterClass);
    }

    @Override
    public final <R extends Renderer<?, ?>> RendererBeanDescriptor<R> getRendererBeanDescriptorForClass(
            final Class<R> rendererBeanClass) {
        for (final RendererBeanDescriptor<?> descriptor : getRendererBeanDescriptors()) {
            if (descriptor.getComponentClass() == rendererBeanClass) {
                @SuppressWarnings("unchecked") final RendererBeanDescriptor<R> result =
                        (RendererBeanDescriptor<R>) descriptor;
                return result;
            }
        }
        return notFoundRenderer(rendererBeanClass);
    }

    @Override
    public final TransformerDescriptor<?> getTransformerDescriptorByDisplayName(final String name) {
        return getComponentDescriptorByDisplayName(name, getTransformerDescriptors(), true, true);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <T extends Transformer> TransformerDescriptor<T> getTransformerDescriptorForClass(
            final Class<T> transformerClass) {
        for (final TransformerDescriptor<?> descriptor : getTransformerDescriptors()) {
            if (descriptor.getComponentClass() == transformerClass) {
                return (TransformerDescriptor<T>) descriptor;
            }
        }
        return notFoundTransformer(transformerClass);
    }

    @Override
    public final Collection<RendererBeanDescriptor<?>> getRendererBeanDescriptorsForRenderingFormat(
            final Class<? extends RenderingFormat<?>> renderingFormat) {
        final List<RendererBeanDescriptor<?>> result = new ArrayList<>();
        final Collection<RendererBeanDescriptor<?>> descriptors = getRendererBeanDescriptors();
        for (final RendererBeanDescriptor<?> descriptor : descriptors) {
            final Class<? extends RenderingFormat<?>> descriptorsRenderingFormat = descriptor.getRenderingFormat();
            if (descriptorsRenderingFormat == renderingFormat) {
                result.add(descriptor);
            }
        }
        return result;
    }

    private <D extends ComponentDescriptor<?>> D getComponentDescriptorByDisplayName(String name,
            final Iterable<D> descriptors, final boolean searchPrimaryNames, final boolean searchAliases) {
        if (name == null) {
            return null;
        }

        // Ticket #951 : trim descriptor names
        name = name.trim();

        if (name.length() == 0) {
            return null;
        }

        if (searchPrimaryNames) {
            for (final D descriptor : descriptors) {
                final String displayName = descriptor.getDisplayName();
                if (name.equals(displayName)) {
                    return descriptor;
                }
            }
        }

        if (searchAliases) {
            for (final D descriptor : descriptors) {
                final String[] aliases = descriptor.getAliases();
                if (ArrayUtils.contains(aliases, name)) {
                    return descriptor;
                }
            }
        }
        return null;
    }

    private <A extends Analyzer<?>> AnalyzerDescriptor<A> notFoundAnalyzer(final Class<A> analyzerClass) {
        if (!_autoDiscover) {
            return null;
        }
        return Descriptors.ofAnalyzer(analyzerClass);
    }

    private FilterDescriptor<?, ?> notFoundFilter(final Class<?> filterClass) {
        if (!_autoDiscover) {
            return null;
        }
        return Descriptors.ofFilterUnbound(filterClass);
    }

    private <R extends Renderer<?, ?>> RendererBeanDescriptor<R> notFoundRenderer(final Class<R> rendererClass) {
        if (!_autoDiscover) {
            return null;
        }
        return Descriptors.ofRenderer(rendererClass);
    }

    private <T extends Transformer> TransformerDescriptor<T> notFoundTransformer(final Class<T> transformerClass) {
        if (!_autoDiscover) {
            return null;
        }
        return Descriptors.ofTransformer(transformerClass);
    }

    @Override
    public Collection<? extends ComponentDescriptor<?>> getComponentDescriptors() {
        final List<ComponentDescriptor<?>> result = new ArrayList<>();
        result.addAll(getTransformerDescriptors());
        result.addAll(getFilterDescriptors());
        result.addAll(getAnalyzerDescriptors());
        return result;
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
    public Collection<? extends ComponentDescriptor<?>> getComponentDescriptorsOfSuperCategory(
            final ComponentSuperCategory category) {
        if (category == null) {
            return Collections.emptyList();
        }
        final List<ComponentDescriptor<?>> result = new ArrayList<>();
        final Collection<? extends ComponentDescriptor<?>> descriptors = getComponentDescriptors();
        for (final ComponentDescriptor<?> componentDescriptor : descriptors) {
            if (category.equals(componentDescriptor.getComponentSuperCategory())) {
                result.add(componentDescriptor);
            }
        }
        return result;
    }

    protected void notifyListeners() {
        synchronized (_listeners) {
            for (final DescriptorProviderListener listener : _listeners) {
                listener.onDescriptorsUpdated(this);
            }
        }
    }

    @Override
    public void addListener(final DescriptorProviderListener listener) {
        synchronized (_listeners) {
            _listeners.add(listener);
        }
    }

    @Override
    public void removeListener(final DescriptorProviderListener listener) {
        synchronized (_listeners) {
            _listeners.remove(listener);
        }
    }

}
