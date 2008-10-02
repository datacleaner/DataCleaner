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

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dk.eobjects.datacleaner.catalog.IDictionary;

/**
 * This class keeps trac of configured dictionaries. If a dictionary is given by
 * it's name, the DictionaryManager is used to resolve it.
 */
public class DictionaryManager {

	private static final Log _log = LogFactory.getLog(DictionaryManager.class);
	private static List<IDictionary> _dictionaries = new ArrayList<IDictionary>();

	public static void setDictionaries(List<IDictionary> dictionaries) {
		if (_log.isInfoEnabled()) {
			_log.info("Setting dictionaries: "
					+ ArrayUtils.toString(dictionaries));
		}
		_dictionaries = dictionaries;
	}

	public static IDictionary[] getDictionaries() {
		return _dictionaries.toArray(new IDictionary[_dictionaries.size()]);
	}

	public static IDictionary getDictionaryByName(String name) {
		if (name != null) {
			for (IDictionary dictionary : _dictionaries) {
				if (dictionary != null) {
					String dictionaryName = dictionary.getName();
					if (name.equals(dictionaryName)) {
						return dictionary;
					}
				}
			}
		}
		return null;
	}

	public static void addDictionary(IDictionary dictionary) {
		_dictionaries.add(dictionary);
	}
}