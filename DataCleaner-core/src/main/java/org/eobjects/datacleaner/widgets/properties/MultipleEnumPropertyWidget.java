/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.widgets.properties;

import javax.inject.Inject;

import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;

public class MultipleEnumPropertyWidget extends AbstractMultipleCheckboxesPropertyWidget<Enum<?>> {

	@Inject
	@SuppressWarnings("unchecked")
	public MultipleEnumPropertyWidget(AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
			ConfiguredPropertyDescriptor propertyDescriptor) {
		super(beanJobBuilder, propertyDescriptor, (Class<Enum<?>>) propertyDescriptor.getBaseType());
	}

	private static final long serialVersionUID = 1L;

	@Override
	protected Enum<?>[] getAvailableValues() {
		@SuppressWarnings("unchecked")
		Class<? extends Enum<?>> baseType = (Class<? extends Enum<?>>) getPropertyDescriptor().getBaseType();
		return baseType.getEnumConstants();
	}

	@Override
	protected String getName(Enum<?> item) {
		return item.toString();
	}

}
