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
package org.datacleaner.monitor.configuration;

import java.util.Collection;
import java.util.Set;

import org.datacleaner.api.Analyzer;
import org.datacleaner.api.ComponentSuperCategory;
import org.datacleaner.api.Filter;
import org.datacleaner.api.Renderer;
import org.datacleaner.api.RenderingFormat;
import org.datacleaner.api.Transformer;
import org.datacleaner.descriptors.AnalyzerDescriptor;
import org.datacleaner.descriptors.ClasspathScanDescriptorProvider;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.DescriptorProvider;
import org.datacleaner.descriptors.DescriptorProviderListener;
import org.datacleaner.descriptors.FilterDescriptor;
import org.datacleaner.descriptors.RendererBeanDescriptor;
import org.datacleaner.descriptors.TransformerDescriptor;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

/**
 * A descriptor provider which delegates to a shared descriptor provides for all
 * tenants.
 */
public class SharedDescriptorProvider implements DescriptorProvider {

    private DescriptorProvider _delegate;

    @Override
    public void refresh() {
        if (_delegate != null) {
            _delegate.refresh();
        }
    }

    @Override
    public ComponentDescriptor<?> getComponentDescriptorByDisplayName(final String name) {
        return getDelegate().getComponentDescriptorByDisplayName(name);
    }

    public DescriptorProvider getDelegate() {
        if (_delegate == null) {
            final WebApplicationContext applicationContext = ContextLoader.getCurrentWebApplicationContext();
            if (applicationContext == null) {
                // use a hard-coded descriptor provider (will only occur in test
                // scenarios)
                final ClasspathScanDescriptorProvider scanner = new ClasspathScanDescriptorProvider();
                scanner.scanPackage("org.datacleaner.beans", true);
                scanner.scanPackage("org.datacleaner.components", true);
                scanner.scanPackage("org.datacleaner.result.renderer", false);
                scanner.scanPackage("org.datacleaner.extension", true);
                scanner.scanPackage("com.hi", true);
                scanner.scanPackage("com.neopost", true);
                _delegate = scanner;
            } else {
                _delegate = applicationContext.getBean(DescriptorProvider.class);
            }
        }
        return _delegate;
    }

    public void setDelegate(final DescriptorProvider delegate) {
        _delegate = delegate;
    }

    @Override
    public AnalyzerDescriptor<?> getAnalyzerDescriptorByDisplayName(final String arg0) {
        return getDelegate().getAnalyzerDescriptorByDisplayName(arg0);
    }

    @Override
    public <A extends Analyzer<?>> AnalyzerDescriptor<A> getAnalyzerDescriptorForClass(final Class<A> arg0) {
        return getDelegate().getAnalyzerDescriptorForClass(arg0);
    }

    @Override
    public Collection<AnalyzerDescriptor<?>> getAnalyzerDescriptors() {
        return getDelegate().getAnalyzerDescriptors();
    }

    @Override
    public FilterDescriptor<?, ?> getFilterDescriptorByDisplayName(final String arg0) {
        return getDelegate().getFilterDescriptorByDisplayName(arg0);
    }

    @Override
    public <F extends Filter<C>, C extends Enum<C>> FilterDescriptor<F, C> getFilterDescriptorForClass(
            final Class<F> arg0) {
        return getDelegate().getFilterDescriptorForClass(arg0);
    }

    @Override
    public Collection<FilterDescriptor<?, ?>> getFilterDescriptors() {
        return getDelegate().getFilterDescriptors();
    }

    @Override
    public <R extends Renderer<?, ?>> RendererBeanDescriptor<R> getRendererBeanDescriptorForClass(final Class<R> arg0) {
        return getDelegate().getRendererBeanDescriptorForClass(arg0);
    }

    @Override
    public Collection<RendererBeanDescriptor<?>> getRendererBeanDescriptors() {
        return getDelegate().getRendererBeanDescriptors();
    }

    @Override
    public Collection<RendererBeanDescriptor<?>> getRendererBeanDescriptorsForRenderingFormat(
            final Class<? extends RenderingFormat<?>> arg0) {
        return getDelegate().getRendererBeanDescriptorsForRenderingFormat(arg0);
    }

    @Override
    public TransformerDescriptor<?> getTransformerDescriptorByDisplayName(final String arg0) {
        return getDelegate().getTransformerDescriptorByDisplayName(arg0);
    }

    @Override
    public <T extends Transformer> TransformerDescriptor<T> getTransformerDescriptorForClass(final Class<T> arg0) {
        return getDelegate().getTransformerDescriptorForClass(arg0);
    }

    @Override
    public Collection<TransformerDescriptor<?>> getTransformerDescriptors() {
        return getDelegate().getTransformerDescriptors();
    }

    @Override
    public Collection<? extends ComponentDescriptor<?>> getComponentDescriptors() {
        return getComponentDescriptors();
    }

    @Override
    public Collection<? extends ComponentDescriptor<?>> getComponentDescriptorsOfSuperCategory(
            final ComponentSuperCategory category) {
        return getDelegate().getComponentDescriptorsOfSuperCategory(category);
    }

    @Override
    public Set<ComponentSuperCategory> getComponentSuperCategories() {
        return getDelegate().getComponentSuperCategories();
    }

    @Override
    public void addListener(final DescriptorProviderListener listener) {
        getDelegate().addListener(listener);
    }

    @Override
    public void removeListener(final DescriptorProviderListener listener) {
        getDelegate().removeListener(listener);
    }
}
