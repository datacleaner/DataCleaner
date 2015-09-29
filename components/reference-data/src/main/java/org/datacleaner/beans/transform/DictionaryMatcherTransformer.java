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
package org.datacleaner.beans.transform;

import javax.inject.Named;

import org.datacleaner.api.Categorized;
import org.datacleaner.api.Close;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.Initialize;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.Provided;
import org.datacleaner.api.Transformer;
import org.datacleaner.components.categories.ImproveSuperCategory;
import org.datacleaner.components.categories.ReferenceDataCategory;
import org.datacleaner.components.convert.ConvertToStringTransformer;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.reference.Dictionary;
import org.datacleaner.reference.DictionaryConnection;
import org.datacleaner.reference.ReferenceData;

@Named("Dictionary matcher")
@Description("Matches string values against a set of dictionaries, producing a corresponding set of output columns specifying whether or not the values exist in those dictionaries")
@Categorized(superCategory = ImproveSuperCategory.class, value = ReferenceDataCategory.class)
public class DictionaryMatcherTransformer implements Transformer {

	@Configured
	Dictionary[] _dictionaries;

	@Configured
	InputColumn<?> _column;

	@Configured
	MatchOutputType _outputType = MatchOutputType.TRUE_FALSE;
	
	@Provided
	DataCleanerConfiguration _configuration;
	
	private DictionaryConnection[] dictionaryConnections;

	public DictionaryMatcherTransformer() {
	}

	public DictionaryMatcherTransformer(InputColumn<?> column, Dictionary[] dictionaries, DataCleanerConfiguration configuration) {
		this();
		_column = column;
		_dictionaries = dictionaries;
		_configuration = configuration;
	}

	public void setDictionaries(Dictionary[] dictionaries) {
		_dictionaries = dictionaries;
	}

	public void setColumn(InputColumn<?> column) {
		_column = column;
	}

	@Override
	public OutputColumns getOutputColumns() {
		String columnName = _column.getName();
		String[] names = new String[_dictionaries.length];
		for (int i = 0; i < names.length; i++) {
			names[i] = columnName + " in '" + _dictionaries[i].getName() + "'";
		}
		Class<?>[] types = new Class[_dictionaries.length];
		for (int i = 0; i < types.length; i++) {
			types[i] = _outputType.getOutputClass();
		}
		return new OutputColumns(names, types);
	}
	
	@Initialize
	public void init() {
	    dictionaryConnections = new DictionaryConnection[_dictionaries.length];
	    for (int i = 0; i < _dictionaries.length; i++) {
            dictionaryConnections[i] = _dictionaries[i].openConnection(_configuration);
        }
	}
	
	@Close
	public void close() {
	    if (dictionaryConnections != null) {
	        for (int i = 0; i < dictionaryConnections.length; i++) {
	            dictionaryConnections[i].close();
            }
	        dictionaryConnections=null;
	    }
	}

	@Override
	public Object[] transform(InputRow inputRow) {
		Object value = inputRow.getValue(_column);
		return transform(value);
	}

	public Object[] transform(final Object value) {
		String stringValue = ConvertToStringTransformer.transformValue(value);
		Object[] result = new Object[_dictionaries.length];
		if (stringValue != null) {
			for (int i = 0; i < result.length; i++) {
				boolean containsValue = dictionaryConnections[i].containsValue(stringValue);
				if (_outputType == MatchOutputType.TRUE_FALSE) {
					result[i] = containsValue;
				} else if (_outputType == MatchOutputType.INPUT_OR_NULL) {
					if (containsValue) {
						result[i] = stringValue;
					} else {
						result[i] = null;
					}
				}
			}
		}
		return result;
	}

}
