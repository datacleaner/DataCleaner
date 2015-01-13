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
package org.datacleaner.reference;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.datacleaner.util.CollectionUtils2;
import org.datacleaner.util.ReadObjectBuilder;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.filemonitor.FileMonitor;
import org.datacleaner.util.filemonitor.FileMonitorFactory;
import org.apache.metamodel.util.FileHelper;

import com.google.common.cache.Cache;

/**
 * Synonym catalog based on a text file.
 * 
 * Each line in the file should contain a master term with trailing
 * comma-separated synonyms.
 * 
 * Example:
 * 
 * <pre>
 * DK,Denmark,Danmark,Dänemark
 * NL,Holland,The Netherlands
 * FR,France
 * </pre>
 * 
 * 
 */
public final class TextFileSynonymCatalog extends AbstractReferenceData implements SynonymCatalog {

    private static final long serialVersionUID = 1L;

    private transient volatile Cache<String, String> _masterTermCache;
    private transient File _file;
    private transient FileMonitor _fileMonitor;

    private final String _filename;
    private final boolean _caseSensitive;
    private final String _encoding;

    public TextFileSynonymCatalog(String name, String filename, boolean caseSensitive, String encoding) {
        super(name);
        _filename = filename;
        _caseSensitive = caseSensitive;
        _encoding = encoding;
    }

    public TextFileSynonymCatalog(String name, File file, boolean caseSensitive, String encoding) {
        super(name);
        _filename = file.getPath();
        _caseSensitive = caseSensitive;
        _encoding = encoding;
    }

    @Override
    protected void decorateIdentity(List<Object> identifiers) {
        super.decorateIdentity(identifiers);
        identifiers.add(_filename);
        identifiers.add(_caseSensitive);
        identifiers.add(_encoding);
    }

    @Override
    public String toString() {
        return "TextFileSynonymCatalog[name=" + getName() + ", filename=" + _filename + ", caseSensitive="
                + _caseSensitive + ", encoding=" + _encoding + "]";
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        ReadObjectBuilder.create(this, TextFileSynonymCatalog.class).readObject(stream);
    }

    public String getEncoding() {
        return _encoding;
    }

    public String getFilename() {
        return _filename;
    }

    public boolean isCaseSensitive() {
        return _caseSensitive;
    }

    private File getFile() {
        if (_file == null) {
            synchronized (this) {
                if (_file == null) {
                    _file = new File(_filename);
                }
            }
        }
        return _file;
    }

    private FileMonitor getFileMonitor() {
        if (_fileMonitor == null) {
            synchronized (this) {
                if (_fileMonitor == null) {
                    _fileMonitor = FileMonitorFactory.getFileMonitor(getFile());
                }
            }
        }
        return _fileMonitor;
    }

    @Override
    public Collection<Synonym> getSynonyms() {
        BufferedReader reader = FileHelper.getBufferedReader(getFile(), _encoding);
        try {
            List<Synonym> synonyms = new ArrayList<Synonym>();
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                line = line.trim();
                synonyms.add(new TextFileSynonym(line, _caseSensitive));
            }
            return synonyms;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } finally {
            FileHelper.safeClose(reader);
        }
    }

    private Cache<String, String> getMasterTermCache() {
        if (_masterTermCache == null) {
            synchronized (this) {
                if (_masterTermCache == null) {
                    _masterTermCache = CollectionUtils2.createCache(10000, 5 * 60);
                }
            }
        } else {
            if (getFileMonitor().hasChanged()) {
                // reset the cache
                _masterTermCache.invalidateAll();
            }
        }
        return _masterTermCache;
    }

    @Override
    public String getMasterTerm(String term) {
        if (StringUtils.isNullOrEmpty(term)) {
            return null;
        }
        String masterTerm = getMasterTermCache().getIfPresent(term);
        if (masterTerm != null) {
            return masterTerm;
        }

        BufferedReader reader = FileHelper.getBufferedReader(getFile(), _encoding);
        try {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                line = line.trim();
                TextFileSynonym synonym = new TextFileSynonym(line, _caseSensitive);
                masterTerm = synonym.getMasterTerm();
                if (term.equals(masterTerm) || synonym.getSynonyms().containsValue(term)) {
                    getMasterTermCache().put(term, masterTerm);
                    return masterTerm;
                }
            }
            return null;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } finally {
            FileHelper.safeClose(reader);
        }
    }
}
