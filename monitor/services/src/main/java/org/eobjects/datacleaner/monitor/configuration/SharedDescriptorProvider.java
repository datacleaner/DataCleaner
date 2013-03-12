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
package org.eobjects.datacleaner.monitor.configuration;

import java.util.Collection;

import org.eobjects.analyzer.beans.api.Analyzer;
import org.eobjects.analyzer.beans.api.Explorer;
import org.eobjects.analyzer.beans.api.Filter;
import org.eobjects.analyzer.beans.api.Renderer;
import org.eobjects.analyzer.beans.api.RenderingFormat;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.descriptors.ClasspathScanDescriptorProvider;
import org.eobjects.analyzer.descriptors.DescriptorProvider;
import org.eobjects.analyzer.descriptors.ExplorerBeanDescriptor;
import org.eobjects.analyzer.descriptors.FilterBeanDescriptor;
import org.eobjects.analyzer.descriptors.RendererBeanDescriptor;
import org.eobjects.analyzer.descriptors.TransformerBeanDescriptor;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

/**
 * A descriptor provider which delegates to a shared descriptor provides for all
 * tenants.
 */
public class SharedDescriptorProvider implements DescriptorProvider {

    private DescriptorProvider _delegate;

    public void setDelegate(DescriptorProvider delegate) {
        _delegate = delegate;
    }

    public DescriptorProvider getDelegate() {
        if (_delegate == null) {
            WebApplicationContext applicationContext = ContextLoader.getCurrentWebApplicationContext();
            if (applicationContext == null) {
                // use a hard-coded descriptor provider (will only occur in test
                // scenarios)
                ClasspathScanDescriptorProvider scanner = new ClasspathScanDescriptorProvider();
                scanner.scanPackage("org.eobjects.analyzer.beans", true);
                scanner.scanPackage("org.eobjects.analyzer.result.renderer", false);
                scanner.scanPackage("com.hi", true);
                _delegate = scanner;
            } else {
                _delegate = applicationContext.getBean(DescriptorProvider.class);
            }
        }
        return _delegate;
    }

    @Override
    public AnalyzerBeanDescriptor<?> getAnalyzerBeanDescriptorByDisplayName(String arg0) {
        return getDelegate().getAnalyzerBeanDescriptorByDisplayName(arg0);
    }

    @Override
    public <A extends Analyzer<?>> AnalyzerBeanDescriptor<A> getAnalyzerBeanDescriptorForClass(Class<A> arg0) {
        return getDelegate().getAnalyzerBeanDescriptorForClass(arg0);
    }

    @Override
    public Collection<AnalyzerBeanDescriptor<?>> getAnalyzerBeanDescriptors() {
        return getDelegate().getAnalyzerBeanDescriptors();
    }

    @Override
    public ExplorerBeanDescriptor<?> getExplorerBeanDescriptorByDisplayName(String arg0) {
        return getDelegate().getExplorerBeanDescriptorByDisplayName(arg0);
    }

    @Override
    public <E extends Explorer<?>> ExplorerBeanDescriptor<E> getExplorerBeanDescriptorForClass(Class<E> arg0) {
        return getDelegate().getExplorerBeanDescriptorForClass(arg0);
    }

    @Override
    public Collection<ExplorerBeanDescriptor<?>> getExplorerBeanDescriptors() {
        return getDelegate().getExplorerBeanDescriptors();
    }

    @Override
    public FilterBeanDescriptor<?, ?> getFilterBeanDescriptorByDisplayName(String arg0) {
        return getDelegate().getFilterBeanDescriptorByDisplayName(arg0);
    }

    @Override
    public <F extends Filter<C>, C extends Enum<C>> FilterBeanDescriptor<F, C> getFilterBeanDescriptorForClass(
            Class<F> arg0) {
        return getDelegate().getFilterBeanDescriptorForClass(arg0);
    }

    @Override
    public Collection<FilterBeanDescriptor<?, ?>> getFilterBeanDescriptors() {
        return getDelegate().getFilterBeanDescriptors();
    }

    @Override
    public <R extends Renderer<?, ?>> RendererBeanDescriptor<R> getRendererBeanDescriptorForClass(Class<R> arg0) {
        return getDelegate().getRendererBeanDescriptorForClass(arg0);
    }

    @Override
    public Collection<RendererBeanDescriptor<?>> getRendererBeanDescriptors() {
        return getDelegate().getRendererBeanDescriptors();
    }

    @Override
    public Collection<RendererBeanDescriptor<?>> getRendererBeanDescriptorsForRenderingFormat(
            Class<? extends RenderingFormat<?>> arg0) {
        return getDelegate().getRendererBeanDescriptorsForRenderingFormat(arg0);
    }

    @Override
    public TransformerBeanDescriptor<?> getTransformerBeanDescriptorByDisplayName(String arg0) {
        return getDelegate().getTransformerBeanDescriptorByDisplayName(arg0);
    }

    @Override
    public <T extends Transformer<?>> TransformerBeanDescriptor<T> getTransformerBeanDescriptorForClass(Class<T> arg0) {
        return getDelegate().getTransformerBeanDescriptorForClass(arg0);
    }

    @Override
    public Collection<TransformerBeanDescriptor<?>> getTransformerBeanDescriptors() {
        return getDelegate().getTransformerBeanDescriptors();
    }

}
