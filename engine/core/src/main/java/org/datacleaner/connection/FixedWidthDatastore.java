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
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.metamodel.DataContext;
import org.apache.metamodel.DataContextFactory;
import org.apache.metamodel.fixedwidth.EbcdicConfiguration;
import org.apache.metamodel.fixedwidth.FixedWidthConfiguration;
import org.apache.metamodel.schema.naming.AlphabeticColumnNamingStrategy;
import org.apache.metamodel.schema.naming.ColumnNamingStrategy;
import org.apache.metamodel.schema.naming.CustomColumnNamingStrategy;
import org.apache.metamodel.util.FileResource;
import org.apache.metamodel.util.Resource;
import org.apache.metamodel.util.SerializableRef;
import org.datacleaner.util.ReadObjectBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Datastore based on fixed width files
 */
public class FixedWidthDatastore extends UsageAwareDatastore<DataContext> implements FileDatastore, ResourceDatastore {

    public static final String EBCDIC_PREFIX = "IBM0";
    private static final long serialVersionUID = 1L;
    private static Logger logger = LoggerFactory.getLogger(FixedWidthDatastore.class);
    private final String _filename;
    private final String _encoding;
    private final int _fixedValueWidth;
    private final int[] _valueWidths;
    private final boolean _failOnInconsistencies;
    private final boolean _skipEbcdicHeader;
    private final boolean _eolPresent;
    private final int _headerLineNumber;
    private final List<String> _customColumnNames;
    private final SerializableRef<Resource> _resourceRef;

    public FixedWidthDatastore(final String name, final String filename, final String encoding,
            final int fixedValueWidth) {
        this(name, filename, encoding, fixedValueWidth, true, false, true);
    }

    public FixedWidthDatastore(final String name, final String filename, final String encoding,
            final int[] valueWidths) {
        this(name, filename, encoding, valueWidths, true, false, true);
    }

    public FixedWidthDatastore(final String name, final String filename, final String encoding,
            final int fixedValueWidth, final boolean failOnInconsistencies, final boolean skipEbcdicHeader,
            final boolean eolPresent) {
        this(name, filename, encoding, fixedValueWidth, failOnInconsistencies, skipEbcdicHeader, eolPresent,
                FixedWidthConfiguration.DEFAULT_COLUMN_NAME_LINE);
    }

    public FixedWidthDatastore(final String name, final String filename, final String encoding, final int[] valueWidths,
            final boolean failOnInconsistencies, final boolean skipEbcdicHeader, final boolean eolPresent) {
        this(name, filename, encoding, valueWidths, failOnInconsistencies, skipEbcdicHeader, eolPresent,
                FixedWidthConfiguration.DEFAULT_COLUMN_NAME_LINE);
    }

    public FixedWidthDatastore(final String name, final String filename, final String encoding,
            final int fixedValueWidth, final boolean failOnInconsistencies, final boolean skipEbcdicHeader,
            final boolean eolPresent, final int headerLineNumber) {
        this(name, null, filename, encoding, fixedValueWidth, failOnInconsistencies, skipEbcdicHeader, eolPresent,
                headerLineNumber);
    }

    public FixedWidthDatastore(final String name, Resource resource, final String filename, final String encoding,
            final int fixedValueWidth, final boolean failOnInconsistencies, final boolean skipEbcdicHeader,
            final boolean eolPresent, final int headerLineNumber) {
        super(name);
        _filename = filename;
        if (resource == null) {
            resource = new FileResource(filename);
        }
        _resourceRef = new SerializableRef<>(resource);
        _encoding = encoding;
        _fixedValueWidth = fixedValueWidth;
        _valueWidths = new int[0];
        _failOnInconsistencies = failOnInconsistencies;
        _skipEbcdicHeader = skipEbcdicHeader;
        _eolPresent = eolPresent;
        _headerLineNumber = headerLineNumber;
        _customColumnNames = null;
    }

    public FixedWidthDatastore(final String name, final String filename, final String encoding, final int[] valueWidths,
            final boolean failOnInconsistencies, final boolean skipEbcdicHeader, final boolean eolPresent,
            final int headerLineNumber) {
        this(name, filename, encoding, valueWidths, failOnInconsistencies, skipEbcdicHeader, eolPresent,
                headerLineNumber, null);
    }

    public FixedWidthDatastore(final String name, final String filename, final String encoding, final int[] valueWidths,
            final boolean failOnInconsistencies, final boolean skipEbcdicHeader, final boolean eolPresent,
            final int headerLineNumber, final List<String> customColumnNames) {
        this(name, null, filename, encoding, valueWidths, failOnInconsistencies, skipEbcdicHeader, eolPresent,
                headerLineNumber, customColumnNames);
    }

    public FixedWidthDatastore(final String name, Resource resource, final String filename, final String encoding,
            final int[] valueWidths, final boolean failOnInconsistencies, final boolean skipEbcdicHeader,
            final boolean eolPresent, final int headerLineNumber, final List<String> customColumnNames) {
        super(name);
        _filename = filename;
        if (resource == null) {
            resource = new FileResource(filename);
        }
        _resourceRef = new SerializableRef<>(resource);
        _encoding = encoding;
        _fixedValueWidth = -1;
        _valueWidths = valueWidths;
        _failOnInconsistencies = failOnInconsistencies;
        _skipEbcdicHeader = skipEbcdicHeader;
        _eolPresent = eolPresent;
        _headerLineNumber = headerLineNumber;
        _customColumnNames = customColumnNames;
    }

    private void readObject(final ObjectInputStream stream) throws IOException, ClassNotFoundException {
        final ReadObjectBuilder.Adaptor adaptor = (getField, serializable) -> {
            final String filename = (String) getField.get("_filename", "");
            final Field field = FixedWidthDatastore.class.getDeclaredField("_resourceRef");
            field.setAccessible(true);
            final FileResource fileResource = new FileResource(filename);
            final SerializableRef<Resource> resourceRef = new SerializableRef<>(fileResource);
            field.set(serializable, resourceRef);
        };
        ReadObjectBuilder.create(this, FixedWidthDatastore.class).readObject(stream, adaptor);
    }

    @Override
    public PerformanceCharacteristics getPerformanceCharacteristics() {
        return new PerformanceCharacteristicsImpl(false, true);
    }

    @Override
    protected UsageAwareDatastoreConnection<DataContext> createDatastoreConnection() {
        final FixedWidthConfiguration configuration = getConfiguration();
        final Resource resource = _resourceRef.get();
        final DataContext dataContext;
        if (resource == null) {
            logger.warn("Resource was not available, a local file reference will be created with path: {}", _filename);
            dataContext = DataContextFactory.createFixedWidthDataContext(new File(_filename), configuration);
        } else {
            dataContext = DataContextFactory.createFixedWidthDataContext(resource, configuration);
        }
        return new DatastoreConnectionImpl<>(dataContext, this);
    }

    public FixedWidthConfiguration getConfiguration() {
        final FixedWidthConfiguration configuration;

        if (isEbcdic()) {
            if (_fixedValueWidth == -1) {
                configuration = new EbcdicConfiguration(_headerLineNumber, _encoding, _valueWidths,
                        _failOnInconsistencies, _skipEbcdicHeader, _eolPresent);
            } else {
                configuration = new EbcdicConfiguration(_headerLineNumber, _encoding, _fixedValueWidth,
                        _failOnInconsistencies, _skipEbcdicHeader, _eolPresent);
            }
        } else {
            if (_fixedValueWidth == -1) {
                final ColumnNamingStrategy columnNamingStrategy;
                if (_headerLineNumber > 0) {
                    columnNamingStrategy = null;
                } else {
                    if (_customColumnNames != null && !_customColumnNames.isEmpty()) {
                        columnNamingStrategy = new CustomColumnNamingStrategy(_customColumnNames);
                    } else {
                        columnNamingStrategy = new AlphabeticColumnNamingStrategy();
                    }
                }
                configuration = new FixedWidthConfiguration(_headerLineNumber, columnNamingStrategy, _encoding,
                        _valueWidths, _failOnInconsistencies);
            } else {
                configuration = new FixedWidthConfiguration(_headerLineNumber, _encoding, _fixedValueWidth,
                        _failOnInconsistencies);
            }
        }
        return configuration;
    }

    public String getEncoding() {
        return _encoding;
    }

    public int getFixedValueWidth() {
        return _fixedValueWidth;
    }

    public int[] getValueWidths() {
        return _valueWidths == null ? new int[0] : _valueWidths;
    }

    public int getHeaderLineNumber() {
        return _headerLineNumber;
    }

    @Override
    public String getFilename() {
        return _filename;
    }

    @Override
    public Resource getResource() {
        if (_resourceRef == null) {
            return null;
        }
        return _resourceRef.get();
    }

    public boolean isFailOnInconsistencies() {
        return _failOnInconsistencies;
    }

    public boolean isSkipEbcdicHeader() {
        return _skipEbcdicHeader;
    }

    public boolean isEolPresent() {
        return _eolPresent;
    }

    public List<String> getCustomColumnNames() {
        return _customColumnNames == null ? Collections.emptyList() : _customColumnNames;
    }

    @Override
    protected void decorateIdentity(final List<Object> identifiers) {
        super.decorateIdentity(identifiers);
        identifiers.add(_filename);
        identifiers.add(_encoding);
        identifiers.add(_fixedValueWidth);
        identifiers.add(_valueWidths);
        identifiers.add(_headerLineNumber);
        identifiers.add(_failOnInconsistencies);
        identifiers.add(_skipEbcdicHeader);
        identifiers.add(_eolPresent);
    }

    private boolean isEbcdic() {
        // This is just a way how to differentiate between EBCDIC and normal FixedWidth configuration.
        // Perhaps there is a better way how to do this.
        return getEncoding().startsWith(EBCDIC_PREFIX);
    }

    @Override
    public String toString() {
        return "FixedWidthDatastore[name=" + getName() + ", filename=" + _filename + ", encoding=" + _encoding
                + ", headerLineNumber=" + _headerLineNumber + ", valueWidths=" + Arrays.toString(_valueWidths)
                + ", fixedValueWidth=" + _fixedValueWidth + "]";
    }
}
