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

import javax.inject.Named;

import org.datacleaner.api.Component;
import org.datacleaner.api.ComponentSuperCategory;
import org.datacleaner.util.ReflectionUtils;

/**
 * Abstract implementation of the {@link ComponentDescriptor} interface.
 * Convenient for implementing it's subclasses.
 * 
 * @param <B>
 *            the type of {@link Component}
 */
abstract class AbstractComponentDescriptor<B> extends SimpleComponentDescriptor<B> implements ComponentDescriptor<B> {

    private static final long serialVersionUID = 1L;

    private final boolean _requireInputColumns;
    private final String _displayName;

    public AbstractComponentDescriptor(Class<B> componentClass, boolean requireInputColumns) {
        super(componentClass);
        _requireInputColumns = requireInputColumns;
        _displayName = determineDisplayName();
    }

    private String determineDisplayName() {
        final Class<B> componentClass = getComponentClass();
        final Named named = getAnnotation(Named.class);
        String displayName;
        if (named == null) {
            displayName = getDisplayNameIfNotNamed(componentClass);
        } else {
            displayName = named.value();
        }

        if (displayName == null) {
            displayName = "";
        }
        displayName = displayName.trim();
        if ("".equals(displayName)) {
            displayName = ReflectionUtils.explodeCamelCase(componentClass.getSimpleName(), false);
        }
        return displayName;
    }

    protected abstract String getDisplayNameIfNotNamed(Class<?> componentClass);

    public final String getDisplayName() {
        if (_displayName == null) {
            // in deserialized instances _displayName may be null
            return determineDisplayName();
        }
        return _displayName;
    };
    
    @Override
    protected abstract Class<? extends ComponentSuperCategory> getDefaultComponentSuperCategoryClass();

    @Override
    protected void visitClass() {
        super.visitClass();

        if (_requireInputColumns) {
            int numConfiguredColumns = 0;
            int numConfiguredColumnArrays = 0;
            for (ConfiguredPropertyDescriptor cd : _configuredProperties) {
                if (cd.isInputColumn()) {
                    if (cd.isArray()) {
                        numConfiguredColumnArrays++;
                    } else {
                        numConfiguredColumns++;
                    }
                }
            }
            final int totalColumns = numConfiguredColumns + numConfiguredColumnArrays;
            if (totalColumns == 0) {
                throw new DescriptorException(getComponentClass()
                        + " does not define a @Configured InputColumn or InputColumn-array");
            }
        }
    }
}
