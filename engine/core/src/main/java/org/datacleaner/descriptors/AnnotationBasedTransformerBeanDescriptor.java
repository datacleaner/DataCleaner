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

import org.datacleaner.beans.api.Distributed;
import org.datacleaner.beans.api.Transformer;
import org.datacleaner.beans.api.TransformerBean;
import org.datacleaner.util.ReflectionUtils;
import org.datacleaner.util.StringUtils;

final class AnnotationBasedTransformerBeanDescriptor<T extends Transformer<?>> extends AbstractBeanDescriptor<T>
        implements TransformerBeanDescriptor<T> {

    private static final long serialVersionUID = 1L;

    private final String _displayName;

    protected AnnotationBasedTransformerBeanDescriptor(Class<T> transformerClass) throws DescriptorException {
        super(transformerClass, false);

        if (!ReflectionUtils.is(transformerClass, Transformer.class)) {
            throw new DescriptorException(transformerClass + " does not implement " + Transformer.class.getName());
        }

        final TransformerBean transformerAnnotation = ReflectionUtils.getAnnotation(transformerClass, TransformerBean.class);
        if (transformerAnnotation == null) {
            throw new DescriptorException(transformerClass + " doesn't implement the TransformerBean annotation");
        }

        String displayName = transformerAnnotation.value();
        if (StringUtils.isNullOrEmpty(displayName)) {
            displayName = ReflectionUtils.explodeCamelCase(transformerClass.getSimpleName(), false);
        }
        _displayName = displayName.trim();

        visitClass();
    }

    @Override
    public String getDisplayName() {
        return _displayName;
    }

    @Override
    public Class<?> getOutputDataType() {
        Class<?> typeParameter = ReflectionUtils.getTypeParameter(getComponentClass(), Transformer.class, 0);
        return typeParameter;
    }

    @Override
    public boolean isDistributable() {
        final Distributed annotation = getAnnotation(Distributed.class);
        if (annotation != null) {
            return annotation.value();
        }
        return true;
    }
}
