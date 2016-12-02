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
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.Resource;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.util.ReadObjectBuilder;
import org.datacleaner.util.ReadObjectBuilder.Adaptor;
import org.datacleaner.util.convert.ResourceConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dictionary based on a simple text file containing the values of the
 * dictionary. Each line of the file will be treated as a value within the
 * dictionary.
 */
public final class TextFileDictionary extends AbstractReferenceData implements Dictionary {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(TextFileDictionary.class);

    private final String _filename;
    private final String _encoding;
    private final boolean _caseSensitive;

    public TextFileDictionary(final String name, final String filename, final String encoding) {
        this(name, filename, encoding, true);
    }

    public TextFileDictionary(final String name, final String filename, final String encoding,
            final boolean caseSensitive) {
        super(name);
        _filename = filename;
        _encoding = encoding;
        _caseSensitive = caseSensitive;
    }

    private void readObject(final ObjectInputStream stream) throws IOException, ClassNotFoundException {
        final Adaptor adaptor = (getField, serializable) -> {
            final boolean caseSensitive = getField.get("_caseSensitive", true);
            final Field field = TextFileDictionary.class.getDeclaredField("_caseSensitive");
            field.setAccessible(true);
            field.set(serializable, caseSensitive);
        };
        ReadObjectBuilder.create(this, TextFileDictionary.class).readObject(stream, adaptor);
    }

    @Override
    public boolean equals(final Object obj) {
        if (super.equals(obj)) {
            final TextFileDictionary other = (TextFileDictionary) obj;
            return Objects.equals(_filename, other._filename) && Objects.equals(_encoding, other._encoding) && Objects
                    .equals(_caseSensitive, other._caseSensitive);
        }
        return false;
    }

    @Override
    public DictionaryConnection openConnection(final DataCleanerConfiguration configuration) {
        final ResourceConverter rc = new ResourceConverter(configuration);
        final Resource resource = rc.fromString(Resource.class, _filename);
        final Set<String> values = resource.read(in -> {
            final Set<String> values1 = new HashSet<>();
            final BufferedReader reader = FileHelper.getBufferedReader(in, getEncoding());
            try {
                String line = reader.readLine();
                while (line != null) {
                    if (!_caseSensitive) {
                        line = line.toLowerCase();
                    }
                    values1.add(line);
                    line = reader.readLine();
                }
            } catch (final IOException e) {
                logger.error("Failed to read line from resource: {}", resource, e);
            } finally {
                FileHelper.safeClose(reader);
            }
            return values1;
        });

        final SimpleDictionary simpleDictionary = new SimpleDictionary(getName(), values, _caseSensitive);
        return simpleDictionary.openConnection(configuration);
    }

    @Override
    public String toString() {
        return "TextFileDictionary[name=" + getName() + ", filename=" + _filename + ", encoding=" + _encoding + "]";
    }

    public String getFilename() {
        return _filename;
    }

    public String getEncoding() {
        return _encoding;
    }

    @Override
    public boolean isCaseSensitive() {
        return _caseSensitive;
    }
}
