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
package org.datacleaner.util.convert;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.metamodel.csv.CsvConfiguration;
import org.apache.metamodel.csv.CsvWriter;
import org.datacleaner.api.Converter;
import org.datacleaner.util.LabelUtils;
import org.datacleaner.util.ReflectionUtils;

import au.com.bytecode.opencsv.CSVParser;

import com.google.common.base.Splitter;

/**
 * A {@link Converter} for maps.
 */
public class MapStringToStringConverter implements Converter<Map<?, ?>> {

    private final CsvConfiguration configuration = new CsvConfiguration(1, "UTF-8", '=', '"',
            '\\');

    @Override
    public Map<?, ?> fromString(Class<?> type, String serializedForm) {
        try {
            final CSVParser csvParser = new CSVParser(configuration.getSeparatorChar(), configuration.getQuoteChar(),
                    configuration.getEscapeChar());

            final Map<String, String> map = new LinkedHashMap<>();
            final Iterable<String> lines = Splitter.on('\n').split(serializedForm);
            for (String line : lines) {
                final String[] values = csvParser.parseLine(line);
                if (values.length == 2) {
                    String value = values[1];
                    if (LabelUtils.NULL_LABEL.equals(value)) {
                        value = null;
                    }
                    map.put(values[0], value);
                }
            }
            return map;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString(final Map<?, ?> instance) {
        final CsvWriter csvWriter = new CsvWriter(configuration);
        final StringBuilder sb = new StringBuilder();

        final Set<?> keys = instance.keySet();
        for (final Object key : keys) {
            final Object value = instance.get(key);
            final String valueStr = Objects.toString(value, LabelUtils.NULL_LABEL);
            final String keyStr = Objects.toString(key, LabelUtils.NULL_LABEL);
            final String line = csvWriter.buildLine(new String[] { keyStr, valueStr });
            sb.append(line);
        }
        return sb.toString();
    }

    @Override
    public boolean isConvertable(Class<?> type) {
        return ReflectionUtils.is(type, Map.class);
    }

}
