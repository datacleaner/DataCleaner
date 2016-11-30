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

import java.lang.reflect.Field;

import org.datacleaner.api.Alias;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Convertable;
import org.datacleaner.api.Converter;
import org.datacleaner.api.Description;
import org.datacleaner.util.ReflectionUtils;
import org.datacleaner.util.StringUtils;

/**
 * Default implementation of {@link ConfiguredPropertyDescriptor}.
 */
final class ConfiguredPropertyDescriptorImpl extends AbstractPropertyDescriptor
        implements ConfiguredPropertyDescriptor {

    private static final long serialVersionUID = 1L;

    protected ConfiguredPropertyDescriptorImpl(final Field field, final ComponentDescriptor<?> componentDescriptor)
            throws DescriptorException {
        super(field, componentDescriptor);
    }

    @Override
    public String getName() {
        final Configured configured = getAnnotation(Configured.class);
        if (configured != null) {
            final String value = configured.value();
            if (!StringUtils.isNullOrEmpty(value)) {
                return value.trim();
            }
        }
        return ReflectionUtils.explodeCamelCase(super.getName(), true);
    }

    @Override
    public String getDescription() {
        final Description desc = getAnnotation(Description.class);
        if (desc == null) {
            return null;
        }
        return desc.value();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[name=" + getName() + "]";
    }

    @Override
    public boolean isInputColumn() {
        final Class<?> baseType = getBaseType();
        return ReflectionUtils.isInputColumn(baseType);
    }

    @Override
    public boolean isRequired() {
        final Configured configured = getAnnotation(Configured.class);
        if (configured == null) {
            return true;
        }
        return configured.required();
    }

    @Override
    public int compareTo(final PropertyDescriptor o) {
        final Configured conf1 = getAnnotation(Configured.class);
        final int order1 = conf1.order();
        final Configured conf2 = o.getAnnotation(Configured.class);
        final int order2;
        if (conf2 == null) {
            order2 = Integer.MAX_VALUE;
        } else {
            order2 = conf2.order();
        }
        final int diff = order1 - order2;
        if (diff == 0) {
            return super.compareTo(o);
        }
        return diff;
    }

    @Override
    public Converter<?> createCustomConverter() {
        final Class<? extends Converter<?>> converterClass = getCustomConverterClass();
        if (converterClass != null) {
            try {
                return converterClass.newInstance();
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            return null;
        }
    }

    private Class<? extends Converter<?>> getCustomConverterClass() {
        final Convertable convertable = getAnnotation(Convertable.class);
        if (convertable != null) {
            return convertable.value();
        }
        return null;
    }

    @Override
    public String[] getAliases() {
        final Alias alias = getAnnotation(Alias.class);
        if (alias == null) {
            return new String[0];
        }
        return alias.value();
    }
}
