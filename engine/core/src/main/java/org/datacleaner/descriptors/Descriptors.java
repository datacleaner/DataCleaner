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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.datacleaner.api.Analyzer;
import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.Filter;
import org.datacleaner.api.Renderer;
import org.datacleaner.api.Transformer;

/**
 * Contains static factory and utility methods for descriptors within this
 * package.
 * 
 * 
 */
public class Descriptors {

    private static final ConcurrentMap<Class<?>, ResultDescriptor> _resultDescriptors = new ConcurrentHashMap<>();

    private Descriptors() {
        // prevent instantiation
    }

    /**
     * Creates a {@link ComponentDescriptor} for any class.
     * 
     * @param <C>
     * @param componentClass
     * @return
     */
    public static <C> ComponentDescriptor<C> ofComponent(Class<C> componentClass) {
        return new SimpleComponentDescriptor<C>(componentClass, true);
    }

    /**
     * Creates an {@link AnalyzerDescriptor} for an analyzer class.
     * 
     * @param <A>
     * @param analyzerClass
     * @return
     */
    public static <A extends Analyzer<?>> AnalyzerDescriptor<A> ofAnalyzer(Class<A> analyzerClass) {
        return new AnnotationBasedAnalyzerComponentDescriptor<A>(analyzerClass);
    }

    /**
     * Creates a {@link FilterDescriptor} for a filter class.
     * 
     * @param <F>
     * @param <C>
     * @param filterClass
     * @return
     */
    public static <F extends Filter<C>, C extends Enum<C>> FilterDescriptor<F, C> ofFilter(Class<F> filterClass) {
        return new AnnotationBasedFilterComponentDescriptor<F, C>(filterClass);
    }

    /**
     * Creates a {@link FilterDescriptor} for a filter class.
     * 
     * Alternative factory method used when sufficient type-information about
     * the {@link Filter} class is not available.
     * 
     * This method is basically a hack to make the compiler happy, see Ticket
     * #417.
     * 
     * @see http://eobjects.org/trac/ticket/417
     * 
     * @param clazz
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static FilterDescriptor<?, ?> ofFilterUnbound(Class<?> clazz) {
        return new AnnotationBasedFilterComponentDescriptor(clazz);
    }

    /**
     * Creates a {@link TransformerDescriptor} for a transformer class.
     * 
     * @param <T>
     * @param transformerClass
     * @return
     */
    public static <T extends Transformer> TransformerDescriptor<T> ofTransformer(Class<T> transformerClass) {
        return new AnnotationBasedTransformerComponentDescriptor<T>(transformerClass);
    }

    /**
     * Creates a {@link RendererBeanDescriptor} for a renderer class.
     * 
     * @param <R>
     * @param rendererClass
     * @return
     */
    public static <R extends Renderer<?, ?>> RendererBeanDescriptor<R> ofRenderer(Class<R> rendererClass) {
        return new AnnotationBasedRendererBeanDescriptor<R>(rendererClass);
    }

    /**
     * Creates a {@link ResultDescriptor} for a {@link AnalyzerResult}
     * 
     * @param result
     * @return
     */
    public static ResultDescriptor ofResult(AnalyzerResult result) {
        if (result == null) {
            throw new IllegalArgumentException("AnalyzerResult cannot be null");
        }
        return ofResult(result.getClass());
    }

    /**
     * Creates a {@link ResultDescriptor} for a {@link AnalyzerResult} class
     * 
     * @param resultClass
     * @return
     */
    public static ResultDescriptor ofResult(Class<? extends AnalyzerResult> resultClass) {
        ResultDescriptor resultDescriptor = _resultDescriptors.get(resultClass);
        if (resultDescriptor == null) {
            resultDescriptor = new ResultDescriptorImpl(resultClass);
            _resultDescriptors.put(resultClass, resultDescriptor);
        }
        return resultDescriptor;
    }
}
