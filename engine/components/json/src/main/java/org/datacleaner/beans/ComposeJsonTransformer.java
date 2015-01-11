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

import org.datacleaner.beans.api.Categorized;
import org.datacleaner.beans.api.Configured;
import org.datacleaner.beans.api.Description;
import org.datacleaner.beans.api.Initialize;
import org.datacleaner.beans.api.OutputColumns;
import org.datacleaner.beans.api.Transformer;
import javax.inject.Named;
import org.datacleaner.beans.categories.DataStructuresCategory;
import org.datacleaner.data.InputColumn;
import org.datacleaner.data.InputRow;

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

    public ComposeJsonTransformer(InputColumn<?> data) {
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
    public String[] transform(InputRow row) {
        try {
            Object value = row.getValue(data);
            final String json = writer.writeValueAsString(value);
            return new String[] { json };
        } catch (Exception e) {
            throw new IllegalStateException("Exception while creating JSON representation", e);
        }
    }

}
