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

import org.apache.commons.lang.ArrayUtils;
import org.datacleaner.beans.api.Analyzer;
import org.datacleaner.beans.api.Filter;
import org.datacleaner.beans.api.Renderer;
import org.datacleaner.beans.api.RenderingFormat;
import org.datacleaner.beans.api.Transformer;

/**
 * Abstract descriptor provider implementation that implements most trivial
 * methods.
 */
public abstract class AbstractDescriptorProvider implements DescriptorProvider {

    @Override
    public final AnalyzerBeanDescriptor<?> getAnalyzerBeanDescriptorByDisplayName(String name) {
        return getBeanDescriptorByDisplayName(name, getAnalyzerBeanDescriptors());
    }

    /**
     * Overridable method for handling (and perhaps discovering) unfound
     * analyzer descriptors by class.
     * 
     * @param analyzerClass
     * @return
     */
    protected <A extends Analyzer<?>> AnalyzerBeanDescriptor<A> notFoundAnalyzer(Class<A> analyzerClass) {
        return null;
    }

    /**
     * Overridable method for handling (and perhaps discovering) unfound
     * transformer descriptors by class.
     * 
     * @param transformerClass
     * @return
     */
    protected <A extends Transformer<?>> TransformerBeanDescriptor<A> notFoundTransformer(Class<A> transformerClass) {
        return null;
    }

    /**
     * Overridable method for handling (and perhaps discovering) unfound filter
     * descriptors by class.
     * 
     * @param filterClass
     * @return
     */
    protected FilterBeanDescriptor<?, ?> notFoundFilter(Class<?> filterClass) {
        return null;
    }

    /**
     * Overridable method for handling (and perhaps discovering) unfound
     * renderer descriptors by class.
     * 
     * @param rendererClass
     * @return
     */
    protected <R extends Renderer<?, ?>> RendererBeanDescriptor<R> notFoundRenderer(Class<R> rendererClass) {
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <A extends Analyzer<?>> AnalyzerBeanDescriptor<A> getAnalyzerBeanDescriptorForClass(
            Class<A> analyzerBeanClass) {
        for (AnalyzerBeanDescriptor<?> descriptor : getAnalyzerBeanDescriptors()) {
            if (descriptor.getComponentClass() == analyzerBeanClass) {
                return (AnalyzerBeanDescriptor<A>) descriptor;
            }
        }
        return notFoundAnalyzer(analyzerBeanClass);
    }

    @Override
    public final FilterBeanDescriptor<?, ?> getFilterBeanDescriptorByDisplayName(String name) {
        return getBeanDescriptorByDisplayName(name, getFilterBeanDescriptors());
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <F extends Filter<C>, C extends Enum<C>> FilterBeanDescriptor<F, C> getFilterBeanDescriptorForClass(
            Class<F> filterClass) {
        return (FilterBeanDescriptor<F, C>) getFilterBeanDescriptorForClassUnbounded(filterClass);
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
     * @param clazz
     * @return
     */
    protected final FilterBeanDescriptor<?, ?> getFilterBeanDescriptorForClassUnbounded(Class<?> filterClass) {
        for (FilterBeanDescriptor<?, ?> descriptor : getFilterBeanDescriptors()) {
            if (filterClass == descriptor.getComponentClass()) {
                return descriptor;
            }
        }
        return notFoundFilter(filterClass);
    }

    @Override
    public final <R extends Renderer<?, ?>> RendererBeanDescriptor<R> getRendererBeanDescriptorForClass(
            Class<R> rendererBeanClass) {
        for (RendererBeanDescriptor<?> descriptor : getRendererBeanDescriptors()) {
            if (descriptor.getComponentClass() == rendererBeanClass) {
                @SuppressWarnings("unchecked")
                RendererBeanDescriptor<R> result = (RendererBeanDescriptor<R>) descriptor;
                return result;
            }
        }
        return notFoundRenderer(rendererBeanClass);
    }

    @Override
    public final TransformerBeanDescriptor<?> getTransformerBeanDescriptorByDisplayName(String name) {
        return getBeanDescriptorByDisplayName(name, getTransformerBeanDescriptors());
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <T extends Transformer<?>> TransformerBeanDescriptor<T> getTransformerBeanDescriptorForClass(
            Class<T> transformerClass) {
        for (TransformerBeanDescriptor<?> descriptor : getTransformerBeanDescriptors()) {
            if (descriptor.getComponentClass() == transformerClass) {
                return (TransformerBeanDescriptor<T>) descriptor;
            }
        }
        return notFoundTransformer(transformerClass);
    }

    @Override
    public final Collection<RendererBeanDescriptor<?>> getRendererBeanDescriptorsForRenderingFormat(
            Class<? extends RenderingFormat<?>> renderingFormat) {
        List<RendererBeanDescriptor<?>> result = new ArrayList<RendererBeanDescriptor<?>>();
        Collection<RendererBeanDescriptor<?>> descriptors = getRendererBeanDescriptors();
        for (RendererBeanDescriptor<?> descriptor : descriptors) {
            Class<? extends RenderingFormat<?>> descriptorsRenderingFormat = descriptor.getRenderingFormat();
            if (descriptorsRenderingFormat == renderingFormat) {
                result.add(descriptor);
            }
        }
        return result;
    }

    private <E extends BeanDescriptor<?>> E getBeanDescriptorByDisplayName(String name, Collection<E> descriptors) {
        if (name == null) {
            return null;
        }

        // Ticket #951 : trim descriptor names
        name = name.trim();
        
        if (name.length() == 0) {
            return null;
        }

        for (E descriptor : descriptors) {
            String displayName = descriptor.getDisplayName();
            if (name.equals(displayName)) {
                return descriptor;
            }
        }

        for (E descriptor : descriptors) {
            String[] aliases = descriptor.getAliases();
            if (ArrayUtils.contains(aliases, name)) {
                return descriptor;
            }
        }
        return null;
    }
}
