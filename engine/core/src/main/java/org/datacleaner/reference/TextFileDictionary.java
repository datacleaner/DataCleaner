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
import java.util.HashSet;
import java.util.Set;

import org.apache.metamodel.util.FileHelper;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.elasticsearch.common.base.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dictionary based on a simple text file containing the values of the
 * dictionary. Each line of the file will be treated as a value within the
 * dictionary.
 * 
 * 
 */
public final class TextFileDictionary extends AbstractReferenceData implements Dictionary {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(TextFileDictionary.class);

    private final String _filename;
    private final String _encoding;

    public TextFileDictionary(String name, String filename, String encoding) {
        super(name);
        _filename = filename;
        _encoding = encoding;
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            final TextFileDictionary other = (TextFileDictionary) obj;
            return Objects.equal(_filename, other._encoding) && Objects.equal(_encoding, other._encoding);
        }
        return false;
    }

    @Override
    public DictionaryConnection openConnection(DataCleanerConfiguration configuration) {
        final Set<String> values = new HashSet<>();
        final File file = new File(_filename);
        try (BufferedReader reader = FileHelper.getBufferedReader(file, getEncoding())) {
            String line = reader.readLine();
            while (line != null) {
                values.add(line);
                line = reader.readLine();
            }
        } catch (Exception e) {
            logger.error("Unexpected error while reading text file dictionary", e);
        }
        return new SimpleDictionary(getName(), values).openConnection(configuration);
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
}
