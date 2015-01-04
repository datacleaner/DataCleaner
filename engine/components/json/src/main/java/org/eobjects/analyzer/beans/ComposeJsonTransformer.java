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
package org.eobjects.analyzer.beans;

import javax.inject.Inject;

import org.eobjects.analyzer.beans.api.Categorized;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.Initialize;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.beans.categories.DataStructuresCategory;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

@TransformerBean("Compose & write JSON document")
@Description("Creates a string representation of a data structure as a JSON (JavaScript Object Notation) document")
@Categorized(DataStructuresCategory.class)
public class ComposeJsonTransformer implements Transformer<String> {

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
        return new OutputColumns(data.getName() + " (as JSON)");
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
