/**
 * AnalyzerBeans
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
package org.eobjects.analyzer.beans.datastructures;

import java.util.Map;

import javax.inject.Inject;

import org.eobjects.analyzer.beans.api.Categorized;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.OutputRowCollector;
import org.eobjects.analyzer.beans.api.Provided;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.beans.categories.DataStructuresCategory;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

/**
 * Transformer for extracting elements from lists.
 * 
 * 
 */
@TransformerBean("Read keys and values from map")
@Description("Reads all key/value pairs of a map, creating a record for each pair")
@Categorized(DataStructuresCategory.class)
public class ReadFromMapTransformer implements Transformer<Object> {

	@Inject
	@Configured
	InputColumn<Map<String, ?>> mapColumn;

	@Inject
	@Configured
	@Description("Expected type of the values")
	Class<?> valueType;

	@Inject
	@Configured
	@Description("Verify that expected value type and actual type are the same")
	boolean verifyTypes = false;

	@Inject
	@Provided
	OutputRowCollector outputRowCollector;

	@Override
	public OutputColumns getOutputColumns() {
		String[] columnNames = new String[] { mapColumn.getName() + " (key)", mapColumn.getName() + " (value)" };
		Class<?>[] columnTypes = new Class[] { String.class, valueType };
		return new OutputColumns(columnNames, columnTypes);
	}

	@Override
	public Object[] transform(InputRow row) {
		Map<String, ?> map = row.getValue(mapColumn);
		if (map == null || map.isEmpty()) {
			return new Object[2];
		}

		for (Map.Entry<String, ?> entry : map.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			if (verifyTypes) {
				value = valueType.cast(value);
			}
			outputRowCollector.putValues(key, value);
		}

		return null;
	}

}
