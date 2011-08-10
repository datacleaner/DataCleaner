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
import org.eobjects.analyzer.reference.Dictionary;
import org.eobjects.analyzer.reference.ReferenceDataCatalog;

public class MultipleDictionariesPropertyWidget extends AbstractMultipleCheckboxesPropertyWidget<Dictionary> {

	private static final long serialVersionUID = 1L;
	private final ReferenceDataCatalog _referenceDataCatalog;

	@Inject
	public MultipleDictionariesPropertyWidget(AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
			ConfiguredPropertyDescriptor propertyDescriptor, ReferenceDataCatalog referenceDataCatalog) {
		super(beanJobBuilder, propertyDescriptor, Dictionary.class);
		_referenceDataCatalog = referenceDataCatalog;
	}

	@Override
	protected Dictionary[] getAvailableValues() {
		String[] names = _referenceDataCatalog.getDictionaryNames();
		Dictionary[] result = new Dictionary[names.length];
		for (int i = 0; i < names.length; i++) {
			result[i] = _referenceDataCatalog.getDictionary(names[i]);
		}
		return result;
	}
	
	@Override
	protected String getName(Dictionary item) {
		return item.getName();
	}

}
