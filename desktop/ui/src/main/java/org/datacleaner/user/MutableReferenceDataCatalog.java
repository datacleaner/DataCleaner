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
package org.datacleaner.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.datacleaner.configuration.DomConfigurationWriter;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.Descriptors;
import org.datacleaner.lifecycle.LifeCycleHelper;
import org.datacleaner.reference.Dictionary;
import org.datacleaner.reference.ReferenceData;
import org.datacleaner.reference.ReferenceDataCatalog;
import org.datacleaner.reference.ReferenceDataCatalogImpl;
import org.datacleaner.reference.StringPattern;
import org.datacleaner.reference.SynonymCatalog;

import com.google.common.base.Strings;

/**
 * Reference data catalog implementation that allows mutations/modifications.
 * This is used to enable runtime changes by the user. This reference data
 * catalog wraps an immutable instance, which typically represents what is
 * configured in datacleaner's xml file.
 */
@SuppressWarnings("deprecation")
public class MutableReferenceDataCatalog implements ReferenceDataCatalog {

    private static final long serialVersionUID = 1L;

    private final List<DictionaryChangeListener> _dictionaryListeners = new ArrayList<>();
    private final List<ReferenceDataChangeListener<Dictionary>> _dictionaryChangeListeners = new ArrayList<>();
    private final List<SynonymCatalogChangeListener> _synonymCatalogListeners = new ArrayList<>();
    private final List<ReferenceDataChangeListener<SynonymCatalog>> _synonymCatalogChangeListeners = new ArrayList<>();
    private final List<StringPatternChangeListener> _stringPatternListeners = new ArrayList<>();
    private final List<ReferenceDataChangeListener<StringPattern>> _stringPatternChangeListeners = new ArrayList<>();
    private final ReferenceDataCatalog _immutableDelegate;
    private final LifeCycleHelper _lifeCycleHelper;
    private final DomConfigurationWriter _configurationWriter;
    private final UserPreferences _userPreferences;

    /**
     * No-args constructor, mostly usable for testing code.
     */
    public MutableReferenceDataCatalog() {
        _immutableDelegate = new ReferenceDataCatalogImpl();
        _configurationWriter = new DomConfigurationWriter();
        _userPreferences = new UserPreferencesImpl(null);
        _lifeCycleHelper = new LifeCycleHelper(null, true);
    }

    /**
     * Main constructor for {@link MutableReferenceDataCatalog}.
     *
     * @param immutableDelegate
     * @param configurationWriter
     * @param userPreferences
     * @param lifeCycleHelper
     */
    public MutableReferenceDataCatalog(final ReferenceDataCatalog immutableDelegate,
            final DomConfigurationWriter configurationWriter, final UserPreferences userPreferences,
            final LifeCycleHelper lifeCycleHelper) {
        _immutableDelegate = immutableDelegate;
        _configurationWriter = configurationWriter;
        _userPreferences = userPreferences;
        _lifeCycleHelper = lifeCycleHelper;

        String[] names = _immutableDelegate.getDictionaryNames();
        for (final String name : names) {
            if (containsDictionary(name)) {
                // remove any copies of the dictionary - the immutable (XML)
                // version should always win
                removeDictionary(getDictionary(name), false);
            }
            addDictionary(_immutableDelegate.getDictionary(name), false);
        }

        names = _immutableDelegate.getSynonymCatalogNames();
        for (final String name : names) {
            if (containsSynonymCatalog(name)) {
                // remove any copies of the synonym catalog - the immutable
                // (XML) version should always win
                removeSynonymCatalog(getSynonymCatalog(name), false);
            }
            addSynonymCatalog(_immutableDelegate.getSynonymCatalog(name), false);
        }

        names = _immutableDelegate.getStringPatternNames();
        for (final String name : names) {
            if (containsStringPattern(name)) {
                removeStringPattern(getStringPattern(name), false);
            }
            addStringPattern(_immutableDelegate.getStringPattern(name), false);
        }

        assignProvidedProperties(_userPreferences.getUserDictionaries());
        assignProvidedProperties(_userPreferences.getUserSynonymCatalogs());
        assignProvidedProperties(_userPreferences.getUserStringPatterns());
    }

    private void assignProvidedProperties(final Collection<?> objects) {
        for (final Object object : objects) {
            assignProvidedProperties(object);
        }
    }

    private void assignProvidedProperties(final Object object) {
        final ComponentDescriptor<?> descriptor = Descriptors.ofComponent(object.getClass());
        _lifeCycleHelper.assignProvidedProperties(descriptor, object);
    }

    @Override
    public String[] getDictionaryNames() {
        return _userPreferences.getUserDictionaries().stream().map(ReferenceData::getName).toArray(String[]::new);
    }

    public void addDictionary(final Dictionary dict) {
        addDictionary(dict, true);
    }

    public void addDictionary(final Dictionary dict, final boolean externalize) {
        addDictionaryInternal(dict, externalize);

        for (final DictionaryChangeListener listener : _dictionaryListeners) {
            listener.onAdd(dict);
        }
        for (final ReferenceDataChangeListener<Dictionary> listener : _dictionaryChangeListeners) {
            listener.onAdd(dict);
        }

    }

    private void addDictionaryInternal(final Dictionary dict, final boolean externalize) {
        final String name = dict.getName();
        if (Strings.isNullOrEmpty(name)) {
            throw new IllegalArgumentException("Dictionary has no name!");
        }
        final List<Dictionary> dictionaries = _userPreferences.getUserDictionaries();
        for (final Dictionary dictionary : dictionaries) {
            if (name.equals(dictionary.getName())) {
                throw new IllegalArgumentException("Dictionary name '" + name + "' is not unique!");
            }
        }
        assignProvidedProperties(dict);
        dictionaries.add(dict);
        if (externalize) {
            if (_configurationWriter.isExternalizable(dict)) {
                _configurationWriter.externalize(dict);
            }
            _userPreferences.save();
        }
    }

    public void removeDictionary(final Dictionary dict) {
        removeDictionary(dict, true);
    }

    public void removeDictionary(final Dictionary dict, final boolean externalize) {
        final List<Dictionary> dictionaries = _userPreferences.getUserDictionaries();
        if (dictionaries.remove(dict)) {
            for (final DictionaryChangeListener listener : _dictionaryListeners) {
                listener.onRemove(dict);
            }
            for (final ReferenceDataChangeListener<Dictionary> listener : _dictionaryChangeListeners) {
                listener.onRemove(dict);
            }
        }
        if (externalize) {
            _configurationWriter.removeDictionary(dict.getName());
            _userPreferences.save();
        }
    }

    public void changeDictionary(final Dictionary oldDictionary, final Dictionary newDictionary) {
        changeDictionary(oldDictionary, newDictionary, true);
    }

    public void changeDictionary(final Dictionary oldDictionary, final Dictionary newDictionary,
            final boolean externalize) {
        final List<Dictionary> dictionaries = _userPreferences.getUserDictionaries();
        if (dictionaries.remove(oldDictionary)) {
            if (externalize) {
                _configurationWriter.removeDictionary(oldDictionary.getName());
                _userPreferences.save();
            }
        }

        addDictionaryInternal(newDictionary, externalize);
        for (final ReferenceDataChangeListener<Dictionary> listener : _dictionaryChangeListeners) {
            listener.onChange(oldDictionary, newDictionary);
        }
        for (final DictionaryChangeListener listener : _dictionaryListeners) {
            listener.onRemove(oldDictionary);
            listener.onAdd(newDictionary);
        }
    }

    public void addStringPattern(final StringPattern sp) {
        addStringPattern(sp, true);
    }

    public void addStringPattern(final StringPattern sp, final boolean externalize) {
        addStringPatternInternal(sp, externalize);
        for (final StringPatternChangeListener listener : _stringPatternListeners) {
            listener.onAdd(sp);
        }
        for (final ReferenceDataChangeListener<StringPattern> listener : _stringPatternChangeListeners) {
            listener.onAdd(sp);
        }
    }

    private void addStringPatternInternal(final StringPattern sp, final boolean externalize) {
        final String name = sp.getName();
        if (Strings.isNullOrEmpty(name)) {
            throw new IllegalArgumentException("StringPattern has no name!");
        }
        final List<StringPattern> stringPatterns = _userPreferences.getUserStringPatterns();
        for (final StringPattern stringPattern : stringPatterns) {
            if (name.equals(stringPattern.getName())) {
                throw new IllegalArgumentException("StringPattern name '" + name + "' is not unique!");
            }
        }
        assignProvidedProperties(sp);
        stringPatterns.add(sp);

        if (externalize) {
            if (_configurationWriter.isExternalizable(sp)) {
                _configurationWriter.externalize(sp);
            }
            _userPreferences.save();
        }
    }


    public void removeStringPattern(final StringPattern sp) {
        removeStringPattern(sp, true);
    }

    public void removeStringPattern(final StringPattern sp, final boolean externalize) {
        final List<StringPattern> stringPatterns = _userPreferences.getUserStringPatterns();
        if (stringPatterns.remove(sp)) {
            for (final StringPatternChangeListener listener : _stringPatternListeners) {
                listener.onRemove(sp);
            }
            for (final ReferenceDataChangeListener<StringPattern> listener : _stringPatternChangeListeners) {
                listener.onRemove(sp);
            }
        }
        if (externalize) {
            _configurationWriter.removeStringPattern(sp.getName());
            _userPreferences.save();
        }
    }

    public void changeStringPattern(final StringPattern oldPattern, final StringPattern newPattern) {
        changeStringPattern(oldPattern, newPattern, true);
    }

    public void changeStringPattern(final StringPattern oldPattern, final StringPattern newPattern,
            final boolean externalize) {
        // The old reference is removed from user preferences and we add a new
        //pattern with the same name but with a different expression value
        final List<StringPattern> stringPatterns = _userPreferences.getUserStringPatterns();
        stringPatterns.remove(oldPattern);
        if (externalize) {
            _configurationWriter.removeStringPattern(oldPattern.getName());
            _userPreferences.save();
        }
        addStringPatternInternal(newPattern, externalize);

        for (final ReferenceDataChangeListener<StringPattern> listener : _stringPatternChangeListeners) {
            listener.onChange(oldPattern, newPattern);
        }
        for (final StringPatternChangeListener listener : _stringPatternListeners) {
            listener.onRemove(oldPattern);
            listener.onAdd(newPattern);
        }
    }

    @Override
    public Dictionary getDictionary(final String name) {
        if (name != null) {
            for (final Dictionary dict : _userPreferences.getUserDictionaries()) {
                if (name.equals(dict.getName())) {
                    return dict;
                }
            }
        }
        return null;
    }

    @Override
    public String[] getSynonymCatalogNames() {
        return _userPreferences.getUserSynonymCatalogs().stream().map(SynonymCatalog::getName).toArray(String[]::new);
    }

    public void addSynonymCatalog(final SynonymCatalog sc) {
        addSynonymCatalog(sc, true);
    }

    public void addSynonymCatalog(final SynonymCatalog sc, final boolean externalize) {
        addSynonymCatalogInternal(sc, externalize);
        for (final SynonymCatalogChangeListener listener : _synonymCatalogListeners) {
            listener.onAdd(sc);
        }
        for (final ReferenceDataChangeListener<SynonymCatalog> listener : _synonymCatalogChangeListeners) {
            listener.onAdd(sc);
        }
    }

    private void addSynonymCatalogInternal(final SynonymCatalog sc, final boolean externalize) {
        final String name = sc.getName();
        if (Strings.isNullOrEmpty(name)) {
            throw new IllegalArgumentException("SynonymCatalog has no name!");
        }
        final List<SynonymCatalog> synonymCatalogs = _userPreferences.getUserSynonymCatalogs();
        for (final SynonymCatalog synonymCatalog : synonymCatalogs) {
            if (name.equals(synonymCatalog.getName())) {
                throw new IllegalArgumentException("SynonymCatalog name '" + name + "' is not unique!");
            }
        }

        assignProvidedProperties(sc);
        synonymCatalogs.add(sc);
        if (externalize) {
            if (_configurationWriter.isExternalizable(sc)) {
                _configurationWriter.externalize(sc);
            }
            _userPreferences.save();
        }
    }

    public void removeSynonymCatalog(final SynonymCatalog sc) {
        removeSynonymCatalog(sc, true);
    }

    public void removeSynonymCatalog(final SynonymCatalog sc, final boolean externalize) {
        final List<SynonymCatalog> synonymCatalogs = _userPreferences.getUserSynonymCatalogs();
        if (synonymCatalogs.remove(sc)) {
            for (final SynonymCatalogChangeListener listener : _synonymCatalogListeners) {
                listener.onRemove(sc);
            }
            for (final ReferenceDataChangeListener<SynonymCatalog> listener : _synonymCatalogChangeListeners) {
                listener.onRemove(sc);
            }
        }
        if (externalize) {
            _configurationWriter.removeSynonymCatalog(sc.getName());
            _userPreferences.save();
        }
    }

    public void changeSynonymCatalog(final SynonymCatalog oldSynonymcatalog, final SynonymCatalog newSynonymCatalog) {
        changeSynonymCatalog(oldSynonymcatalog, newSynonymCatalog, true);
    }

    public void changeSynonymCatalog(final SynonymCatalog oldSynonymcatalog, final SynonymCatalog newSynonymCatalog,
            final boolean externalize) {

        final List<SynonymCatalog> synonymCatalogs = _userPreferences.getUserSynonymCatalogs();
        synonymCatalogs.remove(oldSynonymcatalog);
        if (externalize) {
            _configurationWriter.removeSynonymCatalog(oldSynonymcatalog.getName());
            _userPreferences.save();
        }
        addSynonymCatalogInternal(newSynonymCatalog, externalize);

        for (final ReferenceDataChangeListener<SynonymCatalog> listener : _synonymCatalogChangeListeners) {
            listener.onChange(oldSynonymcatalog, newSynonymCatalog);
        }
        for (final SynonymCatalogChangeListener listener : _synonymCatalogListeners) {
            listener.onRemove(oldSynonymcatalog);
            listener.onAdd(newSynonymCatalog);
        }
    }

    @Override
    public SynonymCatalog getSynonymCatalog(final String name) {
        if (name != null) {
            for (final SynonymCatalog sc : _userPreferences.getUserSynonymCatalogs()) {
                if (name.equals(sc.getName())) {
                    return sc;
                }
            }
        }
        return null;
    }

    @Override
    public String[] getStringPatternNames() {
        return _userPreferences.getUserStringPatterns().stream().map(ReferenceData::getName).toArray(String[]::new);
    }

    @Override
    public StringPattern getStringPattern(final String name) {
        if (name != null) {
            for (final StringPattern sp : _userPreferences.getUserStringPatterns()) {
                if (name.equals(sp.getName())) {
                    return sp;
                }
            }
        }
        return null;
    }

    public void addDictionaryListener(final DictionaryChangeListener listener) {
        _dictionaryListeners.add(listener);
    }

    public void removeDictionaryListener(final DictionaryChangeListener listener) {
        _dictionaryListeners.remove(listener);
    }

    public void addDictionaryListener(final ReferenceDataChangeListener<Dictionary> listener) {
        _dictionaryChangeListeners.add(listener);
    }

    public void removeDictionaryListener(final ReferenceDataChangeListener<Dictionary> listener) {
        _dictionaryChangeListeners.remove(listener);
    }

    public void addSynonymCatalogListener(final SynonymCatalogChangeListener listener) {
        _synonymCatalogListeners.add(listener);
    }

    public void removeSynonymCatalogListener(final SynonymCatalogChangeListener listener) {
        _synonymCatalogListeners.remove(listener);
    }

    public void addSynonymCatalogListener(final ReferenceDataChangeListener<SynonymCatalog> listener) {
        _synonymCatalogChangeListeners.add(listener);
    }

    public void removeSynonymCatalogListener(final ReferenceDataChangeListener<SynonymCatalog> listener) {
        _synonymCatalogChangeListeners.remove(listener);
    }

    public void addStringPatternListener(final StringPatternChangeListener listener) {
        _stringPatternListeners.add(listener);
    }

    public void removeStringPatternListener(final StringPatternChangeListener listener) {
        _stringPatternListeners.remove(listener);
    }

    public void addStringPatternListener(final ReferenceDataChangeListener<StringPattern> listener) {
        _stringPatternChangeListeners.add(listener);
    }

    public void removeStringPatternListener(final ReferenceDataChangeListener<StringPattern> listener) {
        _stringPatternChangeListeners.remove(listener);
    }
}
