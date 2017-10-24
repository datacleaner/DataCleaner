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
import org.datacleaner.api.Transformer;
import org.datacleaner.components.categories.TransformSuperCategory;
import org.datacleaner.util.ReflectionUtils;

final class AnnotationBasedTransformerComponentDescriptor<T extends Transformer> extends AbstractComponentDescriptor<T>
        implements TransformerDescriptor<T> {

    private static final long serialVersionUID = 1L;

    protected AnnotationBasedTransformerComponentDescriptor(final Class<T> transformerClass)
            throws DescriptorException {
        super(transformerClass, false);

        if (!ReflectionUtils.is(transformerClass, Transformer.class)) {
            throw new DescriptorException(transformerClass + " does not implement " + Transformer.class.getName());
        }

        visitClass();
    }

    @Override
    protected Class<? extends ComponentSuperCategory> getDefaultComponentSuperCategoryClass() {
        return TransformSuperCategory.class;
    }

    @Override
    public boolean isDistributableByDefault() {
        return true;
    }
}
