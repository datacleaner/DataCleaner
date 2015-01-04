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
package org.datacleaner.connection;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.List;

import org.datacleaner.util.ReadObjectBuilder;
import org.apache.metamodel.DataContext;
import org.apache.metamodel.DataContextFactory;
import org.apache.metamodel.fixedwidth.FixedWidthConfiguration;

/**
 * Datastore based on fixed width files
 */
public class FixedWidthDatastore extends UsageAwareDatastore<DataContext> implements FileDatastore {

	private static final long serialVersionUID = 1L;

	private final String _filename;
	private final String _encoding;
	private final int _fixedValueWidth;
	private final int[] _valueWidths;
	private final boolean _failOnInconsistencies;
	private final int _headerLineNumber;

	public FixedWidthDatastore(String name, String filename, String encoding, int fixedValueWidth) {
		this(name, filename, encoding, fixedValueWidth, true);
	}

	public FixedWidthDatastore(String name, String filename, String encoding, int[] valueWidths) {
		this(name, filename, encoding, valueWidths, true);
	}

	public FixedWidthDatastore(String name, String filename, String encoding, int fixedValueWidth,
			boolean failOnInconsistencies) {
		this(name, filename, encoding, fixedValueWidth, failOnInconsistencies,
				FixedWidthConfiguration.DEFAULT_COLUMN_NAME_LINE);
	}

	public FixedWidthDatastore(String name, String filename, String encoding, int[] valueWidths,
			boolean failOnInconsistencies) {
		this(name, filename, encoding, valueWidths, failOnInconsistencies, FixedWidthConfiguration.DEFAULT_COLUMN_NAME_LINE);
	}

	public FixedWidthDatastore(String name, String filename, String encoding, int fixedValueWidth,
			boolean failOnInconsistencies, int headerLineNumber) {
		super(name);
		_filename = filename;
		_encoding = encoding;
		_fixedValueWidth = fixedValueWidth;
		_valueWidths = new int[0];
		_failOnInconsistencies = failOnInconsistencies;
		_headerLineNumber = headerLineNumber;
	}

	public FixedWidthDatastore(String name, String filename, String encoding, int[] valueWidths,
			boolean failOnInconsistencies, int headerLineNumber) {
		super(name);
		_filename = filename;
		_encoding = encoding;
		_fixedValueWidth = -1;
		_valueWidths = valueWidths;
		_failOnInconsistencies = failOnInconsistencies;
		_headerLineNumber = headerLineNumber;
	}

	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
		ReadObjectBuilder.create(this, FixedWidthDatastore.class).readObject(stream);
	}

	@Override
	public PerformanceCharacteristics getPerformanceCharacteristics() {
		return new PerformanceCharacteristicsImpl(false, true);
	}

	@Override
	protected UsageAwareDatastoreConnection<DataContext> createDatastoreConnection() {
		final File file = new File(_filename);
		assert file.exists();

		final FixedWidthConfiguration configuration;
		if (_fixedValueWidth == -1) {
			configuration = new FixedWidthConfiguration(_headerLineNumber, _encoding, _valueWidths, _failOnInconsistencies);
		} else {
			configuration = new FixedWidthConfiguration(_headerLineNumber, _encoding, _fixedValueWidth,
					_failOnInconsistencies);
		}

		DataContext dataContext = DataContextFactory.createFixedWidthDataContext(file, configuration);
		return new DatastoreConnectionImpl<DataContext>(dataContext, this);
	}

	public String getEncoding() {
		return _encoding;
	}

	public int getFixedValueWidth() {
		return _fixedValueWidth;
	}

	public int[] getValueWidths() {
		return _valueWidths;
	}

	public int getHeaderLineNumber() {
		return _headerLineNumber;
	}

	@Override
	public String getFilename() {
		return _filename;
	}

	public boolean isFailOnInconsistencies() {
		return _failOnInconsistencies;
	}

	@Override
	protected void decorateIdentity(List<Object> identifiers) {
		super.decorateIdentity(identifiers);
		identifiers.add(_filename);
		identifiers.add(_encoding);
		identifiers.add(_fixedValueWidth);
		identifiers.add(_valueWidths);
		identifiers.add(_headerLineNumber);
		identifiers.add(_failOnInconsistencies);
	}

	@Override
	public String toString() {
		return "FixedWidthDatastore[name=" + getName() + ", filename=" + _filename + ", encoding=" + _encoding
				+ ", headerLineNumber=" + _headerLineNumber + ", valueWidths=" + Arrays.toString(_valueWidths)
				+ ", fixedValueWidth=" + _fixedValueWidth + "]";
	}
}
