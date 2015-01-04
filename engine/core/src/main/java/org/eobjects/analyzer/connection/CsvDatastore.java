/**
 * AnalyzerBeans
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
package org.eobjects.analyzer.connection;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

import org.eobjects.analyzer.util.ReadObjectBuilder;
import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.csv.CsvConfiguration;
import org.apache.metamodel.csv.CsvDataContext;
import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.FileResource;
import org.apache.metamodel.util.Resource;
import org.apache.metamodel.util.SerializableRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Datastore implementation for CSV files.
 */
public final class CsvDatastore extends UsageAwareDatastore<UpdateableDataContext> implements FileDatastore,
        ResourceDatastore, UpdateableDatastore {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(CsvDatastore.class);

    /**
     * The value is '\\uFFFF', the "not a character" value which should not
     * occur in any valid Unicode string.
     */
    public static final char NOT_A_CHAR = '\uFFFF';

    public static final char DEFAULT_QUOTE_CHAR = NOT_A_CHAR;
    public static final char DEFAULT_SEPARATOR_CHAR = CsvConfiguration.DEFAULT_SEPARATOR_CHAR;

    private final SerializableRef<Resource> _resourceRef;
    private final String _filename;
    private final Character _quoteChar;
    private final Character _separatorChar;
    private final Character _escapeChar;
    private final String _encoding;
    private final boolean _failOnInconsistencies;
    private final boolean _multilineValues;
    private final int _headerLineNumber;

    public CsvDatastore(String name, Resource resource) {
        this(name, resource, resource.getName(), CsvConfiguration.DEFAULT_QUOTE_CHAR,
                CsvConfiguration.DEFAULT_SEPARATOR_CHAR, CsvConfiguration.DEFAULT_ESCAPE_CHAR,
                FileHelper.DEFAULT_ENCODING, true, CsvConfiguration.DEFAULT_COLUMN_NAME_LINE);
    }

    public CsvDatastore(String name, String filename) {
        this(name, filename, CsvConfiguration.DEFAULT_QUOTE_CHAR, CsvConfiguration.DEFAULT_SEPARATOR_CHAR,
                FileHelper.DEFAULT_ENCODING);
    }

    public CsvDatastore(String name, String filename, Character quoteChar, Character separatorChar, String encoding) {
        this(name, filename, quoteChar, separatorChar, encoding, true);
    }

    public CsvDatastore(String name, String filename, Character quoteChar, Character separatorChar, String encoding,
            boolean failOnInconsistencies) {
        this(name, filename, quoteChar, separatorChar, encoding, failOnInconsistencies,
                CsvConfiguration.DEFAULT_COLUMN_NAME_LINE);
    }

    public CsvDatastore(String name, String filename, Character quoteChar, Character separatorChar, String encoding,
            boolean failOnInconsistencies, int headerLineNumber) {
        this(name, null, filename, quoteChar, separatorChar, CsvConfiguration.DEFAULT_ESCAPE_CHAR, encoding,
                failOnInconsistencies, headerLineNumber);
    }

    public CsvDatastore(String name, Resource resource, CsvConfiguration c) {
        this(name, resource, resource.getName(), c.getQuoteChar(), c.getSeparatorChar(), c.getEscapeChar(), c
                .getEncoding(), c.isFailOnInconsistentRowLength(), c.isMultilineValues(), c.getColumnNameLineNumber());
    }

    public CsvDatastore(String name, Resource resource, String filename, Character quoteChar, Character separatorChar,
            Character escapeChar, String encoding, boolean failOnInconsistencies, int headerLineNumber) {
        this(name, resource, filename, quoteChar, separatorChar, escapeChar, encoding, failOnInconsistencies, true,
                headerLineNumber);
    }

    public CsvDatastore(String name, Resource resource, String filename, Character quoteChar, Character separatorChar,
            Character escapeChar, String encoding, boolean failOnInconsistencies, boolean multilineValues,
            int headerLineNumber) {
        super(name);
        _filename = filename;
        if (resource == null) {
            resource = new FileResource(filename);
        }
        _resourceRef = new SerializableRef<Resource>(resource);
        _quoteChar = quoteChar;
        _separatorChar = separatorChar;
        _escapeChar = escapeChar;
        _encoding = encoding;
        _failOnInconsistencies = failOnInconsistencies;
        _multilineValues = multilineValues;
        if (headerLineNumber < 0) {
            headerLineNumber = CsvConfiguration.NO_COLUMN_NAME_LINE;
        }
        _headerLineNumber = headerLineNumber;

    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        ReadObjectBuilder.create(this, CsvDatastore.class).readObject(stream);
    }

    public String getEncoding() {
        return _encoding;
    }

    @Override
    public String getFilename() {
        return _filename;
    }

    public Character getQuoteChar() {
        return _quoteChar;
    }

    public Character getEscapeChar() {
        return _escapeChar;
    }

    public Character getSeparatorChar() {
        return _separatorChar;
    }

    @Override
    public Resource getResource() {
        if (_resourceRef == null) {
            return null;
        }
        return _resourceRef.get();
    }

    @Override
    protected UsageAwareDatastoreConnection<UpdateableDataContext> createDatastoreConnection() {
        final UpdateableDataContext dataContext;
        final Resource resource = getResource();
        if (resource == null) {
            logger.warn("Resource was not available, a local file reference will be created with path: {}", _filename);
            dataContext = new CsvDataContext(new File(_filename), getCsvConfiguration());
        } else {
            dataContext = new CsvDataContext(resource, getCsvConfiguration());
        }

        return new UpdateableDatastoreConnectionImpl<UpdateableDataContext>(dataContext, this);
    }

    public CsvConfiguration getCsvConfiguration() {
        final char separatorChar = _separatorChar == null ? DEFAULT_SEPARATOR_CHAR : _separatorChar;
        final char quoteChar = _quoteChar == null ? DEFAULT_QUOTE_CHAR : _quoteChar;
        final char escapeChar = _escapeChar == null ? CsvConfiguration.DEFAULT_ESCAPE_CHAR : _escapeChar;
        final String encoding = _encoding == null ? FileHelper.UTF_8_ENCODING : _encoding;
        final CsvConfiguration configuration = new CsvConfiguration(_headerLineNumber, encoding, separatorChar,
                quoteChar, escapeChar, _failOnInconsistencies, _multilineValues);
        return configuration;
    }

    @Override
    public UpdateableDatastoreConnection openConnection() {
        DatastoreConnection connection = super.openConnection();
        return (UpdateableDatastoreConnection) connection;
    }

    @Override
    public PerformanceCharacteristics getPerformanceCharacteristics() {
        return new PerformanceCharacteristicsImpl(false, true);
    }

    public boolean isFailOnInconsistencies() {
        return _failOnInconsistencies;
    }
    
    public boolean isMultilineValues() {
        return _multilineValues;
    }

    public int getHeaderLineNumber() {
        return _headerLineNumber;
    }

    @Override
    protected void decorateIdentity(List<Object> identifiers) {
        super.decorateIdentity(identifiers);
        identifiers.add(_filename);
        identifiers.add(_encoding);
        identifiers.add(_quoteChar);
        identifiers.add(_escapeChar);
        identifiers.add(_separatorChar);
        identifiers.add(_failOnInconsistencies);
        identifiers.add(_multilineValues);
        identifiers.add(_headerLineNumber);
    }

    @Override
    public String toString() {
        return "CsvDatastore[name=" + getName() + ", filename=" + _filename + ", quoteChar='" + _quoteChar
                + "', separatorChar='" + _separatorChar + "', encoding=" + _encoding + ", headerLineNumber="
                + _headerLineNumber + "]";
    }
}
