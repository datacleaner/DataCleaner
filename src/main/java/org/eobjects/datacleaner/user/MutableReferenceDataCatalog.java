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

import org.eobjects.analyzer.reference.Dictionary;
import org.eobjects.analyzer.reference.ReferenceDataCatalog;
import org.eobjects.analyzer.reference.SynonymCatalog;
import org.eobjects.analyzer.util.StringUtils;

public class MutableReferenceDataCatalog implements ReferenceDataCatalog {

	private static final long serialVersionUID = 1L;

	private final List<Dictionary> _dictionaries;
	private final List<DictionaryChangeListener> _dictionaryListeners = new ArrayList<DictionaryChangeListener>();
	private final List<SynonymCatalog> _synonymCatalogs;
	private final List<SynonymCatalogChangeListener> _synonymCatalogListeners = new ArrayList<SynonymCatalogChangeListener>();

	public MutableReferenceDataCatalog(final ReferenceDataCatalog referenceDataCatalog) {
		_dictionaries = UserPreferences.getInstance().getUserDictionaries();
		_synonymCatalogs = UserPreferences.getInstance().getUserSynonymCatalogs();

		String[] names = referenceDataCatalog.getDictionaryNames();
		for (String name : names) {
			if (!containsDictionary(name)) {
				addDictionary(referenceDataCatalog.getDictionary(name));
			}
		}

		names = referenceDataCatalog.getSynonymCatalogNames();
		for (String name : names) {
			if (!containsSynonymCatalog(name)) {
				addSynonymCatalog(referenceDataCatalog.getSynonymCatalog(name));
			}
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

	public boolean containsDictionary(String name) {
		for (Dictionary dictionary : _dictionaries) {
			if (name.equals(dictionary.getName())) {
				return true;
			}
		}
		return false;
	}

	public boolean containsSynonymCatalog(String name) {
		for (SynonymCatalog sc : _synonymCatalogs) {
			if (name.equals(sc.getName())) {
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
		_dictionaries.add(dict);
		for (DictionaryChangeListener listener : _dictionaryListeners) {
			listener.onAdd(dict);
		}
	}

	public void removeDictionary(Dictionary dict) {
		if (_dictionaries.remove(dict)) {
			for (DictionaryChangeListener listener : _dictionaryListeners) {
				listener.onRemove(dict);
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
		_synonymCatalogs.add(sc);
		for (SynonymCatalogChangeListener listener : _synonymCatalogListeners) {
			listener.onAdd(sc);
		}
	}

	public void removeSynonymCatalog(SynonymCatalog sc) {
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
}
