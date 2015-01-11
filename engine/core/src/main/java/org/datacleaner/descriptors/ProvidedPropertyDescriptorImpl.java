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

import org.datacleaner.connection.SchemaNavigator;
import org.datacleaner.storage.CollectionFactory;
import org.datacleaner.storage.RowAnnotationFactory;
import org.datacleaner.util.ReflectionUtils;
import org.apache.metamodel.DataContext;

final class ProvidedPropertyDescriptorImpl extends AbstractPropertyDescriptor implements ProvidedPropertyDescriptor {
	
	private static final long serialVersionUID = 1L;

	protected ProvidedPropertyDescriptorImpl(Field field, ComponentDescriptor<?> componentDescriptor)
			throws DescriptorException {
		super(field, componentDescriptor);
	}

	@Override
	public boolean isSet() {
		return ReflectionUtils.isSet(getBaseType());
	}

	@Override
	public boolean isList() {
		return ReflectionUtils.isList(getBaseType());
	}

	@Override
	public boolean isMap() {
		return ReflectionUtils.isMap(getBaseType());
	}

	@Override
	public boolean isSchemaNavigator() {
		return getBaseType() == SchemaNavigator.class;
	}

	@Override
	public boolean isRowAnnotationFactory() {
		return getBaseType() == RowAnnotationFactory.class;
	}

	@Override
	public boolean isDataContext() {
		return getBaseType() == DataContext.class;
	}

	@Override
	public boolean isCollectionFactory() {
		return getBaseType() == CollectionFactory.class;
	}
}
