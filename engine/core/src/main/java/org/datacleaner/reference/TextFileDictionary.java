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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.datacleaner.api.Initialize;
import org.datacleaner.util.ReadObjectBuilder;
import org.datacleaner.util.filemonitor.FileMonitor;
import org.datacleaner.util.filemonitor.FileMonitorFactory;
import org.apache.metamodel.util.FileHelper;

/**
 * Dictionary based on a simple text file containing the values of the
 * dictionary. Each line of the file will be treated as a value within the
 * dictionary.
 * 
 * 
 */
public final class TextFileDictionary extends AbstractReferenceData implements Dictionary {

	private static final long serialVersionUID = 1L;

	private transient File _file;
	private transient FileMonitor _fileMonitor;
	private transient Set<String> _entries;

	private final String _filename;
	private final String _encoding;

	public TextFileDictionary(String name, String filename, String encoding) {
		super(name);
		_filename = filename;
		_encoding = encoding;
	}

	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
		ReadObjectBuilder.create(this, TextFileDictionary.class).readObject(stream);
	}

	@Override
	protected void decorateIdentity(List<Object> identifiers) {
		super.decorateIdentity(identifiers);
		identifiers.add(_filename);
		identifiers.add(_encoding);
	}

	@Override
	public String toString() {
		return "TextFileDictionary[name=" + getName() + ", filename=" + _filename + ", encoding=" + _encoding + "]";
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
			_fileMonitor = FileMonitorFactory.getFileMonitor(getFile());
		}
		return _fileMonitor;
	}

	public String getFilename() {
		return _filename;
	}

	public String getEncoding() {
		return _encoding;
	}

	@Initialize
	public void init() {
		if (getFileMonitor().hasChanged()) {
			_entries = loadEntries();
		}
	}

	public Set<String> getEntries() {
		if (_entries == null) {
			_entries = loadEntries();
		}
		return _entries;
	}

	private Set<String> loadEntries() {
		Set<String> entries = new HashSet<String>();
		BufferedReader reader = null;
		try {
			reader = FileHelper.getBufferedReader(getFile(), _encoding);
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				entries.add(line);
			}
		} catch (Exception e) {
			throw new IllegalStateException(e);
		} finally {
			FileHelper.safeClose(reader);
		}
		return entries;
	}

	@Override
	public boolean containsValue(String value) {
		if (value == null) {
			return false;
		}
		return getEntries().contains(value);
	}

	@Override
	public ReferenceValues<String> getValues() {
		return new SimpleStringReferenceValues(getEntries(), true);
	}
}
