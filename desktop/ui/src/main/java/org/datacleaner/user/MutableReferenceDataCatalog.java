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
public class MutableReferenceDataCatalog implements ReferenceDataCatalog {

    private static final long serialVersionUID = 1L;

    private final List<DictionaryChangeListener> _dictionaryListeners = new ArrayList<>();
    private final List<ReferenceDataChangeListener<Dictionary>> _dictionaryV2Listeners = new ArrayList<>();
    private final List<SynonymCatalogChangeListener> _synonymCatalogListeners = new ArrayList<>();
    private final List<ReferenceDataChangeListener<SynonymCatalog>> _synonymCatalogV2Listeners = new ArrayList<>();
    private final List<StringPatternChangeListener> _stringPatternListeners = new ArrayList<>();
    private final List<ReferenceDataChangeListener<StringPattern>> _stringPatternV2Listeners = new ArrayList<>();
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
        for (String name : names) {
            if (containsDictionary(name)) {
                // remove any copies of the dictionary - the immutable (XML)
                // version should always win
                removeDictionary(getDictionary(name), false);
            }
            addDictionary(_immutableDelegate.getDictionary(name), false);
        }

        names = _immutableDelegate.getSynonymCatalogNames();
        for (String name : names) {
            if (containsSynonymCatalog(name)) {
                // remove any copies of the synonym catalog - the immutable
                // (XML) version should always win
                removeSynonymCatalog(getSynonymCatalog(name), false);
            }
            addSynonymCatalog(_immutableDelegate.getSynonymCatalog(name), false);
        }

        names = _immutableDelegate.getStringPatternNames();
        for (String name : names) {
            if (containsStringPattern(name)) {
                removeStringPattern(getStringPattern(name), false);
            }
            addStringPattern(_immutableDelegate.getStringPattern(name), false);
        }

        assignProvidedProperties(_userPreferences.getUserDictionaries());
        assignProvidedProperties(_userPreferences.getUserSynonymCatalogs());
        assignProvidedProperties(_userPreferences.getUserStringPatterns());
    }

    private void assignProvidedProperties(Collection<?> objects) {
        for (Object object : objects) {
            assignProvidedProperties(object);
        }
    }

    private void assignProvidedProperties(Object object) {
        final ComponentDescriptor<?> descriptor = Descriptors.ofComponent(object.getClass());
        _lifeCycleHelper.assignProvidedProperties(descriptor, object);
    }

    @Override
    public String[] getDictionaryNames() {
        return _userPreferences.getUserDictionaries().stream().map(d -> d.getName()).toArray(size -> new String[size]);
    }

    public void addDictionary(Dictionary dict) {
        addDictionary(dict, true);
    }

    public void addDictionary(Dictionary dict, boolean externalize) {
        addDictionaryInternal(dict, externalize);
        
        for (DictionaryChangeListener listener : _dictionaryListeners) {
            listener.onAdd(dict);
        }
        for(ReferenceDataChangeListener<Dictionary> listener : _dictionaryV2Listeners){
            listener.onAdd(dict);
        }

    }

    private void addDictionaryInternal(Dictionary dict, boolean externalize) {
        String name = dict.getName();
        if (Strings.isNullOrEmpty(name)) {
            throw new IllegalArgumentException("Dictionary has no name!");
        }
        final List<Dictionary> dictionaries = _userPreferences.getUserDictionaries();
        for (Dictionary dictionary : dictionaries) {
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

    public void removeDictionary(Dictionary dict) {
        removeDictionary(dict, true);
    }

    public void removeDictionary(Dictionary dict, boolean externalize) {
        final List<Dictionary> dictionaries = _userPreferences.getUserDictionaries();
        if (dictionaries.remove(dict)) {
            for (DictionaryChangeListener listener : _dictionaryListeners) {
                listener.onRemove(dict);
            }
            for(ReferenceDataChangeListener<Dictionary> listener : _dictionaryV2Listeners){
                listener.onRemove(dict);
            }
        }
        if (externalize) {
            _configurationWriter.removeDictionary(dict.getName());
            _userPreferences.save();
        }
    }

    public void changeDictionary(Dictionary oldDictionary, Dictionary newDictionary){
        changeDictionary(oldDictionary, newDictionary, true);
    }
    
    public void changeDictionary(Dictionary oldDictionary, Dictionary newDictionary, boolean externalize) {
        final List<Dictionary> dictionaries = _userPreferences.getUserDictionaries();
        if (dictionaries.remove(oldDictionary)) {
            if (externalize) {
                _configurationWriter.removeDictionary(oldDictionary.getName());
                _userPreferences.save();
            }
        }
        
        addDictionaryInternal(newDictionary, externalize);
        for(ReferenceDataChangeListener<Dictionary> listener : _dictionaryV2Listeners){
            listener.onChange(oldDictionary, newDictionary);
        }
        for (DictionaryChangeListener listener : _dictionaryListeners) {
            listener.onRemove(oldDictionary);
            listener.onAdd(newDictionary);
        }
    }

    public void addStringPattern(StringPattern sp) {
        addStringPattern(sp, true);
    }

    public void addStringPattern(StringPattern sp, boolean externalize) {
        addStringPatternInternal(sp, externalize);
        for (StringPatternChangeListener listener : _stringPatternListeners) {
            listener.onAdd(sp);
        }
        for(ReferenceDataChangeListener<StringPattern> listener : _stringPatternV2Listeners){
            listener.onAdd(sp);
        }
    }
    private void addStringPatternInternal(StringPattern sp, boolean externalize) {
        String name = sp.getName();
        if (Strings.isNullOrEmpty(name)) {
            throw new IllegalArgumentException("StringPattern has no name!");
        }
        final List<StringPattern> stringPatterns = _userPreferences.getUserStringPatterns();
        for (StringPattern stringPattern : stringPatterns) {
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


    public void removeStringPattern(StringPattern sp) {
        removeStringPattern(sp, true);
    }

    public void removeStringPattern(StringPattern sp, boolean externalize) {
        final List<StringPattern> stringPatterns = _userPreferences.getUserStringPatterns();
        if (stringPatterns.remove(sp)) {
            for (StringPatternChangeListener listener : _stringPatternListeners) {
                listener.onRemove(sp);
            }
            for(ReferenceDataChangeListener<StringPattern> listener : _stringPatternV2Listeners){
                listener.onRemove(sp);
            }
        }
        if (externalize) {
            _configurationWriter.removeStringPattern(sp.getName());
            _userPreferences.save();
        }
    }

    public void changeStringPattern(StringPattern oldPattern, StringPattern newPattern){
        changeStringPattern(oldPattern, newPattern, true);
    }
    
    public void changeStringPattern(StringPattern oldPattern, StringPattern newPattern, boolean externalize){
        // The old reference is removed from user preferences and we add a new
        // patern with the same name but with a
        // different expression value
        final List<StringPattern> stringPatterns = _userPreferences.getUserStringPatterns();
        stringPatterns.remove(oldPattern);
        if (externalize) {
            _configurationWriter.removeStringPattern(oldPattern.getName());
            _userPreferences.save();
        }   
        addStringPatternInternal(newPattern, externalize);

        for (ReferenceDataChangeListener<StringPattern> listener : _stringPatternV2Listeners) {
            listener.onChange(oldPattern, newPattern);
        }
        for (StringPatternChangeListener listener : _stringPatternListeners) {
            listener.onRemove(oldPattern);
            listener.onAdd(newPattern);
        }
    }
    
    @Override
    public Dictionary getDictionary(String name) {
        if (name != null) {
            for (Dictionary dict : _userPreferences.getUserDictionaries()) {
                if (name.equals(dict.getName())) {
                    return dict;
                }
            }
        }
        return null;
    }

    @Override
    public String[] getSynonymCatalogNames() {
        return _userPreferences.getUserSynonymCatalogs().stream().map(d -> d.getName()).toArray(
                size -> new String[size]);
    }

    public void addSynonymCatalog(SynonymCatalog sc) {
        addSynonymCatalog(sc, true);
    }

    public void addSynonymCatalog(SynonymCatalog sc, boolean externalize) {
        addSynonymCatalogInternal(sc, externalize);
        for (SynonymCatalogChangeListener listener : _synonymCatalogListeners) {
            listener.onAdd(sc);
        }
        for (ReferenceDataChangeListener<SynonymCatalog> listener: _synonymCatalogV2Listeners){
            listener.onAdd(sc);
        }
    }

    private void addSynonymCatalogInternal(SynonymCatalog sc, boolean externalize) {
        String name = sc.getName();
        if (Strings.isNullOrEmpty(name)) {
            throw new IllegalArgumentException("SynonymCatalog has no name!");
        }
        final List<SynonymCatalog> synonymCatalogs = _userPreferences.getUserSynonymCatalogs();
        for (SynonymCatalog synonymCatalog : synonymCatalogs) {
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
    
    public void removeSynonymCatalog(SynonymCatalog sc) {
        removeSynonymCatalog(sc, true);
    }

    public void removeSynonymCatalog(SynonymCatalog sc, boolean externalize) {
        final List<SynonymCatalog> synonymCatalogs = _userPreferences.getUserSynonymCatalogs();
        if (synonymCatalogs.remove(sc)) {
            for (SynonymCatalogChangeListener listener : _synonymCatalogListeners) {
                listener.onRemove(sc);
            }
            for (ReferenceDataChangeListener<SynonymCatalog> listener: _synonymCatalogV2Listeners){
                listener.onRemove(sc);
            }
        }
        if (externalize) {
            _configurationWriter.removeSynonymCatalog(sc.getName());
            _userPreferences.save();
        }
    }

    public void changeSynonymCatalog(SynonymCatalog oldSynonymcatalog, SynonymCatalog newSynonymCatalog) {
        changeSynonymCatalog(oldSynonymcatalog, newSynonymCatalog, true);
    }
    
   public void changeSynonymCatalog(SynonymCatalog oldSynonymcatalog, SynonymCatalog newSynonymCatalog, boolean externalize){
        
       final List<SynonymCatalog> synonymCatalogs = _userPreferences.getUserSynonymCatalogs();
       synonymCatalogs.remove(oldSynonymcatalog);
       if (externalize) {
           _configurationWriter.removeSynonymCatalog(oldSynonymcatalog.getName());
           _userPreferences.save();
       }   
       addSynonymCatalogInternal(newSynonymCatalog, externalize);
       
       for (ReferenceDataChangeListener<SynonymCatalog> listener: _synonymCatalogV2Listeners){
           listener.onChange(oldSynonymcatalog, newSynonymCatalog);
       }
       for (SynonymCatalogChangeListener listener : _synonymCatalogListeners) {
           listener.onRemove(oldSynonymcatalog);
           listener.onAdd(newSynonymCatalog);
       }
    }

    @Override
    public SynonymCatalog getSynonymCatalog(String name) {
        if (name != null) {
            for (SynonymCatalog sc : _userPreferences.getUserSynonymCatalogs()) {
                if (name.equals(sc.getName())) {
                    return sc;
                }
            }
        }
        return null;
    }

    @Override
    public String[] getStringPatternNames() {
        return _userPreferences.getUserStringPatterns().stream().map(d -> d.getName()).toArray(
                size -> new String[size]);
    }

    @Override
    public StringPattern getStringPattern(String name) {
        if (name != null) {
            for (StringPattern sp : _userPreferences.getUserStringPatterns()) {
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
    
    public void addDictionaryListener(ReferenceDataChangeListener<Dictionary> listener){
        _dictionaryV2Listeners.add(listener);
    }
    
    public void removeDictionaryListener(ReferenceDataChangeListener<Dictionary> listener){
        _dictionaryV2Listeners.remove(listener);
    }

    public void addSynonymCatalogListener(SynonymCatalogChangeListener listener) {
        _synonymCatalogListeners.add(listener);
    }

    public void removeSynonymCatalogListener(SynonymCatalogChangeListener listener) {
        _synonymCatalogListeners.remove(listener);
    }
    
    public void addSynonymCatalogListener(ReferenceDataChangeListener<SynonymCatalog> listener){
        _synonymCatalogV2Listeners.add(listener); 
    }
    
    public void removeSynonymCatalogListener(ReferenceDataChangeListener<SynonymCatalog> listener){
        _synonymCatalogV2Listeners.remove(listener);
    }

    public void addStringPatternListener(StringPatternChangeListener listener) {
        _stringPatternListeners.add(listener);
    }

    public void removeStringPatternListener(StringPatternChangeListener listener) {
        _stringPatternListeners.remove(listener);
    }
    
    public void addStringPatternListener(ReferenceDataChangeListener<StringPattern> listener){
        _stringPatternV2Listeners.add(listener);
    }
    
    public void removeStringPatternListener(ReferenceDataChangeListener<StringPattern> listener){
        _stringPatternV2Listeners.remove(listener);
    }
}
