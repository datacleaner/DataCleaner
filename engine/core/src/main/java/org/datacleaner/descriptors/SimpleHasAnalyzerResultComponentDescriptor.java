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

import org.datacleaner.api.ComponentSuperCategory;
import org.datacleaner.api.HasAnalyzerResult;
import org.datacleaner.components.categories.AnalyzeSuperCategory;

/**
 * Simple and disposable implementation of
 * {@link HasAnalyzerResultComponentDescriptor}.
 * 
 * @param <C>
 *            the component type
 */
public class SimpleHasAnalyzerResultComponentDescriptor<C extends HasAnalyzerResult<?>> extends
        AbstractHasAnalyzerResultComponentDescriptor<C> {

    private static final long serialVersionUID = 1L;

    public SimpleHasAnalyzerResultComponentDescriptor(Class<C> beanClass) {
        super(beanClass, false);
    }

    @Override
    protected String getDisplayNameIfNotNamed(Class<?> componentClass) {
        return componentClass.getSimpleName();
    }

    @Override
    protected Class<? extends ComponentSuperCategory> getDefaultComponentSuperCategoryClass() {
        return AnalyzeSuperCategory.class;
    }

}
