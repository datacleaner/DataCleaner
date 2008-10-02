/**
 *  This file is part of DataCleaner.
 *
 *  DataCleaner is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DataCleaner is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DataCleaner.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.eobjects.datacleaner.validator.dictionary;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dk.eobjects.datacleaner.catalog.IDictionary;
import dk.eobjects.datacleaner.validator.IValidationRule;
import dk.eobjects.datacleaner.validator.IValidationRuleResult;
import dk.eobjects.datacleaner.validator.SimpleValidationRuleResult;
import dk.eobjects.datacleaner.validator.ValidatorManager;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.schema.Column;

/**
 * A validation rule that looks up the content of the columns in a dictionary.
 * The rule buffers requests to a dictionary in order to decrease number of
 * dictionary-requests/queries.
 * 
 * @see IDictionary
 */
public class DictionaryValidationRule implements IValidationRule {

	public static final String PROPERTY_BUFFER_SIZE = "Buffer size";
	public static final String PROPERTY_DICTIONARY_NAME = "Dictionary name";
	private static Log _log = LogFactory.getLog(DictionaryValidationRule.class);
	private IDictionary _dictionary;
	private int _queryBufferSize = 50;
	private Column[] _columns;
	private List<Row> _bufferValues = new ArrayList<Row>();
	private SimpleValidationRuleResult _result;
	private Map<String, String> _properties;

	public void setDictionary(IDictionary dictionary) {
		_dictionary = dictionary;
	}

	public void setQueryBufferSize(int i) {
		_queryBufferSize = i;
	}

	public IValidationRuleResult getResult() {
		checkValues();
		return _result;
	}

	public void initialize(Column... columns) {
		if (_properties != null) {
			String bufferSizeStr = _properties.get(PROPERTY_BUFFER_SIZE);
			if (bufferSizeStr != null) {
				try {
					_queryBufferSize = Integer.parseInt(bufferSizeStr);
				} catch (NumberFormatException e) {
					_log.info(e);
				}
			}
			if (_dictionary == null) {
				String dictionaryName = _properties
						.get(PROPERTY_DICTIONARY_NAME);
				_dictionary = DictionaryManager
						.getDictionaryByName(dictionaryName);
			}
		}
		_columns = columns;
		_result = new SimpleValidationRuleResult(
				columns,
				ValidatorManager
						.getValidationRuleDescriptorByValidationRuleClass(DictionaryValidationRule.class),
				_properties);
	}

	public void process(Row row, long distinctRowCount) {
		if (_dictionary == null) {
			throw new IllegalStateException("No dictionary provided!");
		}
		_bufferValues.add(row);
		int numValues = _columns.length * _bufferValues.size();
		if (numValues >= _queryBufferSize) {
			checkValues();
		}
	}

	private void checkValues() {
		int numValues = _columns.length * _bufferValues.size();
		String[] queryValues = new String[numValues];
		for (int i = 0; i < _bufferValues.size(); i++) {
			Row row = _bufferValues.get(i);
			for (int j = 0; j < _columns.length; j++) {
				Object value = row.getValue(_columns[j]);
				String queryValue = null;
				if (value != null) {
					queryValue = value.toString();
				}
				queryValues[i * _columns.length + j] = queryValue;
			}
		}
		boolean[] results = _dictionary.isValid(queryValues);
		for (int i = 0; i < _bufferValues.size(); i++) {
			Row row = _bufferValues.get(i);
			for (int j = 0; j < _columns.length; j++) {
				if (!results[i * _columns.length + j]) {
					_result.addErrorRow(row);
					break;
				}
			}
		}
		_bufferValues.clear();
	}

	public void setProperties(Map<String, String> properties) {
		_properties = properties;
	}

	public IDictionary getDictionary() {
		return _dictionary;
	}

	public int getQueryBufferSize() {
		return _queryBufferSize;
	}
}