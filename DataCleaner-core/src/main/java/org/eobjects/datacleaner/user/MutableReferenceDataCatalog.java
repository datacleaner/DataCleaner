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
package org.eobjects.datacleaner.user;

import java.util.ArrayList;
import java.util.List;

import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.reference.DatastoreDictionary;
import org.eobjects.analyzer.reference.DatastoreSynonymCatalog;
import org.eobjects.analyzer.reference.Dictionary;
import org.eobjects.analyzer.reference.ReferenceDataCatalog;
import org.eobjects.analyzer.reference.StringPattern;
import org.eobjects.analyzer.reference.SynonymCatalog;
import org.eobjects.analyzer.util.StringUtils;

/**
 * Reference data catalog implementation that allows mutations/modifications.
 * This is used to enable runtime changes by the user. This reference data
 * catalog wraps an immutable instance, which typically represents what is
 * configured in datacleaner's xml file.
 * 
 * @author Kasper Sørensen
 * 
 */
public class MutableReferenceDataCatalog implements ReferenceDataCatalog {

	private static final long serialVersionUID = 1L;

	private final DatastoreCatalog _datastoreCatalog;
	private final List<Dictionary> _dictionaries;
	private final List<DictionaryChangeListener> _dictionaryListeners = new ArrayList<DictionaryChangeListener>();
	private final List<SynonymCatalog> _synonymCatalogs;
	private final List<SynonymCatalogChangeListener> _synonymCatalogListeners = new ArrayList<SynonymCatalogChangeListener>();
	private final List<StringPattern> _stringPatterns;
	private final List<StringPatternChangeListener> _stringPatternListeners = new ArrayList<StringPatternChangeListener>();
	private final ReferenceDataCatalog _immutableDelegate;

	public MutableReferenceDataCatalog(final ReferenceDataCatalog immutableDelegate, DatastoreCatalog datastoreCatalog,
			UserPreferences userPreferences) {
		_immutableDelegate = immutableDelegate;
		_datastoreCatalog = datastoreCatalog;
		_dictionaries = userPreferences.getUserDictionaries();
		_synonymCatalogs = userPreferences.getUserSynonymCatalogs();
		_stringPatterns = userPreferences.getUserStringPatterns();

		String[] names = _immutableDelegate.getDictionaryNames();
		for (String name : names) {
			if (containsDictionary(name)) {
				// remove any copies of the dictionary - the immutable (XML)
				// version should always win
				_dictionaries.remove(getDictionary(name));
			}
			addDictionary(_immutableDelegate.getDictionary(name));
		}

		names = _immutableDelegate.getSynonymCatalogNames();
		for (String name : names) {
			if (containsSynonymCatalog(name)) {
				// remove any copies of the synonym catalog - the immutable
				// (XML) version should always win
				_synonymCatalogs.remove(getSynonymCatalog(name));
			}
			addSynonymCatalog(_immutableDelegate.getSynonymCatalog(name));
		}

		names = _immutableDelegate.getStringPatternNames();
		for (String name : names) {
			if (containsStringPattern(name)) {
				_stringPatterns.remove(getStringPattern(name));
			}
			addStringPattern(_immutableDelegate.getStringPattern(name));
		}
	}

	@Override
	public String[] getDictionaryNames() {
		String[] result = new String[_dictionaries.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = _dictionaries.get(i).getName();
		}
		return result;
	}

	public boolean isDictionaryMutable(String name) {
		return _immutableDelegate.getDictionary(name) == null;
	}

	public boolean containsDictionary(String name) {
		for (Dictionary dictionary : _dictionaries) {
			if (name.equals(dictionary.getName())) {
				return true;
			}
		}
		return false;
	}

	public boolean isSynonymCatalogMutable(String name) {
		return _immutableDelegate.getSynonymCatalog(name) == null;
	}

	public boolean isStringPatternMutable(String name) {
		return _immutableDelegate.getStringPattern(name) == null;
	}

	public boolean containsSynonymCatalog(String name) {
		for (SynonymCatalog sc : _synonymCatalogs) {
			if (name.equals(sc.getName())) {
				return true;
			}
		}
		return false;
	}

	public boolean containsStringPattern(String name) {
		for (StringPattern sp : _stringPatterns) {
			if (name.equals(sp.getName())) {
				return true;
			}
		}
		return false;
	}

	public void addDictionary(Dictionary dict) {
		String name = dict.getName();
		if (StringUtils.isNullOrEmpty(name)) {
			throw new IllegalArgumentException("Dictionary has no name!");
		}
		for (Dictionary dictionary : _dictionaries) {
			if (name.equals(dictionary.getName())) {
				throw new IllegalArgumentException("Dictionary name '" + name + "' is not unique!");
			}
		}

		if (dict instanceof DatastoreDictionary) {
			// deserialization of datastore dictionaries is insufficient, it
			// requires also a reference to the datastore catalog
			((DatastoreDictionary) dict).setDatastoreCatalog(_datastoreCatalog);
		}

		_dictionaries.add(dict);
		for (DictionaryChangeListener listener : _dictionaryListeners) {
			listener.onAdd(dict);
		}
	}

	public void removeDictionary(Dictionary dict) {
		if (!isDictionaryMutable(dict.getName())) {
			throw new IllegalArgumentException("Dictionary '" + dict.getName() + " is not removeable");
		}
		if (_dictionaries.remove(dict)) {
			for (DictionaryChangeListener listener : _dictionaryListeners) {
				listener.onRemove(dict);
			}
		}
	}

	public void addStringPattern(StringPattern sp) {
		String name = sp.getName();
		if (StringUtils.isNullOrEmpty(name)) {
			throw new IllegalArgumentException("StringPattern has no name!");
		}
		for (StringPattern stringPattern : _stringPatterns) {
			if (name.equals(stringPattern.getName())) {
				throw new IllegalArgumentException("StringPattern name '" + name + "' is not unique!");
			}
		}
		_stringPatterns.add(sp);
		for (StringPatternChangeListener listener : _stringPatternListeners) {
			listener.onAdd(sp);
		}
	}

	public void removeStringPattern(StringPattern sp) {
		if (!isStringPatternMutable(sp.getName())) {
			throw new IllegalArgumentException("StringPattern '" + sp.getName() + " is not removeable");
		}
		if (_stringPatterns.remove(sp)) {
			for (StringPatternChangeListener listener : _stringPatternListeners) {
				listener.onRemove(sp);
			}
		}
	}

	@Override
	public Dictionary getDictionary(String name) {
		if (name != null) {
			for (Dictionary dict : _dictionaries) {
				if (name.equals(dict.getName())) {
					return dict;
				}
			}
		}
		return null;
	}

	@Override
	public String[] getSynonymCatalogNames() {
		String[] result = new String[_synonymCatalogs.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = _synonymCatalogs.get(i).getName();
		}
		return result;
	}

	public void addSynonymCatalog(SynonymCatalog sc) {
		String name = sc.getName();
		if (StringUtils.isNullOrEmpty(name)) {
			throw new IllegalArgumentException("SynonymCatalog has no name!");
		}
		for (SynonymCatalog synonymCatalog : _synonymCatalogs) {
			if (name.equals(synonymCatalog.getName())) {
				throw new IllegalArgumentException("SynonymCatalog name '" + name + "' is not unique!");
			}
		}

		if (sc instanceof DatastoreSynonymCatalog) {
			((DatastoreSynonymCatalog) sc).setDatastoreCatalog(_datastoreCatalog);
		}

		_synonymCatalogs.add(sc);
		for (SynonymCatalogChangeListener listener : _synonymCatalogListeners) {
			listener.onAdd(sc);
		}
	}

	public void removeSynonymCatalog(SynonymCatalog sc) {
		if (!isSynonymCatalogMutable(sc.getName())) {
			throw new IllegalArgumentException("Synonym catalog '" + sc.getName() + " is not removeable");
		}
		if (_synonymCatalogs.remove(sc)) {
			for (SynonymCatalogChangeListener listener : _synonymCatalogListeners) {
				listener.onRemove(sc);
			}
		}
	}

	@Override
	public SynonymCatalog getSynonymCatalog(String name) {
		if (name != null) {
			for (SynonymCatalog sc : _synonymCatalogs) {
				if (name.equals(sc.getName())) {
					return sc;
				}
			}
		}
		return null;
	}

	@Override
	public String[] getStringPatternNames() {
		String[] names = new String[_stringPatterns.size()];
		for (int i = 0; i < names.length; i++) {
			names[i] = _stringPatterns.get(i).getName();
		}
		return names;
	}

	@Override
	public StringPattern getStringPattern(String name) {
		if (name != null) {
			for (StringPattern sp : _stringPatterns) {
				if (name.equals(sp.getName())) {
					return sp;
				}
			}
		}
		return null;
	}

	public void addDictionaryListener(DictionaryChangeListener listener) {
		_dictionaryListeners.add(listener);
	}

	public void removeDictionaryListener(DictionaryChangeListener listener) {
		_dictionaryListeners.remove(listener);
	}

	public void addSynonymCatalogListener(SynonymCatalogChangeListener listener) {
		_synonymCatalogListeners.add(listener);
	}

	public void removeSynonymCatalogListener(SynonymCatalogChangeListener listener) {
		_synonymCatalogListeners.remove(listener);
	}

	public void addStringPatternListener(StringPatternChangeListener listener) {
		_stringPatternListeners.add(listener);
	}

	public void removeStringPatternListener(StringPatternChangeListener listener) {
		_stringPatternListeners.remove(listener);
	}
}
