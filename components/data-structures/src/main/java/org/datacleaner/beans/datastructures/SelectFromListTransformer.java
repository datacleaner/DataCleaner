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
package org.datacleaner.beans.datastructures;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.datacleaner.api.*;
import org.datacleaner.components.categories.DataStructuresCategory;

/**
 * Transformer for selecting values from maps.
 */
@Named("Select values from list")
@Description("Given a specified list of indices, this transformer will select the values from a list and place them as columns within the record")
@Categorized(DataStructuresCategory.class)
@WSStatelessComponent
public class SelectFromListTransformer implements Transformer {

	@Inject
	@Configured
	InputColumn<List<?>> listColumn;

	@Inject
	@Configured
	@Description("A list of (0-based) indices to use for fetching values from the list.")
	Number[] indices = { 0, 1, 2 };

	@Inject
	@Configured
	Class<?> elementType;

	@Inject
	@Configured
	@Description("Verify that expected element type and actual type are the same")
	boolean verifyTypes = false;

	@Override
	public OutputColumns getOutputColumns() {
		String[] names = new String[indices.length];
		Class<?>[] types = new Class[indices.length];
		for (int i = 0; i < indices.length; i++) {
			names[i] = listColumn.getName() + "[" + indices[i] + "]";
			types[i] = elementType;
		}
		return new OutputColumns(names, types);
	}

	@Override
	public Object[] transform(InputRow row) {
		final Object[] result = new Object[indices.length];

		List<?> list = row.getValue(listColumn);
		if (list != null && !list.isEmpty()) {
			for (int i = 0; i < indices.length; i++) {
				int index = indices[i].intValue();
				if (index >= 0 && index < list.size()) {
					Object value = list.get(index);
					result[i] = value;
				}
			}
		}

		return result;
	}
}
