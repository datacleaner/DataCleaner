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
package org.datacleaner.beans;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.Initialize;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.Transformer;
import org.datacleaner.beans.categories.DataStructuresCategory;
import org.datacleaner.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

@Named("Read & parse JSON document")
@Description("Parses a JSON document (as a string) and materializes the data structure it represents")
@Categorized(DataStructuresCategory.class)
public class ParseJsonTransformer implements Transformer {

    @Inject
    @Configured(order = 1)
    @Description("Column containing JSON documents to parse")
    InputColumn<String> json;

    @Inject
    @Configured(order = 2)
    Class<?> dataType = Map.class;

    private ObjectMapper mapper;
    private ObjectReader reader;

    public ParseJsonTransformer() {

    }

    public ParseJsonTransformer(InputColumn<String> json) {
        this.json = json;
    }

    @Initialize
    public void init() {
        this.mapper = new ObjectMapper();
        this.reader = mapper.reader().withType(dataType);
    }

    @Override
    public OutputColumns getOutputColumns() {
        String[] names = new String[] { json.getName() + " (as Map)" };
        Class<?>[] types = new Class[] { dataType };
        return new OutputColumns(names, types);
    }

    @Override
    public Object[] transform(InputRow inputRow) {
        final String jsonString = inputRow.getValue(json);
        final Object result = parse(jsonString, dataType, reader);

        return new Object[] { result };
    }

    public static <E> E parse(final String jsonString, final Class<E> dataType, final ObjectMapper objectMapper) {
        if (StringUtils.isNullOrEmpty(jsonString)) {
            return null;
        }

        final ObjectReader reader = objectMapper.reader().withType(dataType);
        return parse(jsonString, dataType, reader);
    }

    public static <E> E parse(final String jsonString, final Class<E> dataType, ObjectReader reader) {
        if (StringUtils.isNullOrEmpty(jsonString)) {
            return null;
        }

        try {
            return reader.readValue(jsonString);
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new IllegalStateException("Exception occurred while parsing JSON", e);
        }
    }

    public void setDataType(Class<?> dataType) {
        this.dataType = dataType;
    }

    public void setJson(InputColumn<String> json) {
        this.json = json;
    }
}
