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
import org.datacleaner.components.categories.DataStructuresCategory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

@Named("Compose & write JSON document")
@Description("Creates a string representation of a data structure as a JSON (JavaScript Object Notation) document")
@Categorized(DataStructuresCategory.class)
public class ComposeJsonTransformer implements Transformer {

    @Inject
    @Configured
    @Description("Column containing data structures to format")
    InputColumn<?> data;

    private ObjectMapper mapper;
    private ObjectWriter writer;

    public ComposeJsonTransformer() {
    }

    public ComposeJsonTransformer(final InputColumn<?> data) {
        this.data = data;
    }

    @Initialize
    public void init() {
        mapper = new ObjectMapper();
        writer = mapper.writer();
    }

    @Override
    public OutputColumns getOutputColumns() {
        return new OutputColumns(String.class, data.getName() + " (as JSON)");
    }

    @Override
    public String[] transform(final InputRow row) {
        try {
            final Object value = row.getValue(data);
            final String json = writer.writeValueAsString(value);
            return new String[] { json };
        } catch (final Exception e) {
            throw new IllegalStateException("Exception while creating JSON representation", e);
        }
    }

}
