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

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.datacleaner.api.Alias;
import org.datacleaner.api.ComponentSuperCategory;
import org.datacleaner.api.Filter;
import org.datacleaner.api.QueryOptimizedFilter;
import org.datacleaner.components.categories.TransformSuperCategory;
import org.datacleaner.util.ReflectionUtils;

final class AnnotationBasedFilterComponentDescriptor<F extends Filter<C>, C extends Enum<C>>
        extends AbstractComponentDescriptor<F> implements FilterDescriptor<F, C> {

    private static final long serialVersionUID = 1L;

    protected AnnotationBasedFilterComponentDescriptor(final Class<F> filterClass) throws DescriptorException {
        super(filterClass, false);

        if (!ReflectionUtils.is(filterClass, Filter.class)) {
            throw new DescriptorException(filterClass + " does not implement " + Filter.class.getName());
        }

        visitClass();
    }

    @Override
    protected Class<? extends ComponentSuperCategory> getDefaultComponentSuperCategoryClass() {
        return TransformSuperCategory.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<C> getOutcomeCategoryEnum() {
        final Class<?> typeParameter = ReflectionUtils.getTypeParameter(getComponentClass(), Filter.class, 0);
        if (typeParameter == null) {
            throw new IllegalStateException("Could not determine Filter's category enum type");
        }
        return (Class<C>) typeParameter;
    }

    @Override
    public EnumSet<C> getOutcomeCategories() {
        final Class<C> categoryEnum = getOutcomeCategoryEnum();
        return EnumSet.allOf(categoryEnum);
    }

    @Override
    public Set<String> getOutcomeCategoryNames() {
        final EnumSet<C> enumSet = getOutcomeCategories();
        final Set<String> result = new HashSet<>();
        for (final Enum<C> category : enumSet) {
            result.add(category.name());
        }
        return result;
    }

    @Override
    public Enum<C> getOutcomeCategoryByName(final String categoryName) {
        if (categoryName == null) {
            return null;
        }
        final EnumSet<C> categories = getOutcomeCategories();
        for (final Enum<C> c : categories) {
            if (c.name().equals(categoryName)) {
                return c;
            }
        }

        for (final Enum<C> c : categories) {
            // check aliases
            final Alias aliasAnnotation = ReflectionUtils.getAnnotation(c, Alias.class);
            if (aliasAnnotation != null) {
                final String[] aliases = aliasAnnotation.value();
                for (final String alias : aliases) {
                    if (categoryName.equals(alias)) {
                        return c;
                    }
                }
            }
        }

        return null;
    }

    @Override
    public boolean isQueryOptimizable() {
        return ReflectionUtils.is(getComponentClass(), QueryOptimizedFilter.class);
    }

    @Override
    public boolean isDistributableByDefault() {
        if (isQueryOptimizable()) {
            // The general rule for query optimized filters is that they are NOT
            // distributeable (unless annotated with @Distributed).
            return false;
        }
        return true;
    }
}
