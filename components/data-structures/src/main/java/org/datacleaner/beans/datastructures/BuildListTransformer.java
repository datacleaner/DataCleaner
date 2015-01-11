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
package org.datacleaner.beans.datastructures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.Transformer;
import org.datacleaner.components.categories.DataStructuresCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transformer for building lists based on values in a row.
 */
@Named("Build list")
@Description("Build a list containing a variable amount of elements. Adds the capability to save multiple values in a single field.")
@Categorized(DataStructuresCategory.class)
public class BuildListTransformer implements Transformer {

    private static final Logger logger = LoggerFactory.getLogger(BuildMapTransformer.class);

    @Inject
    @Configured
    InputColumn<?>[] values;

    @Inject
    @Configured
    boolean includeNullValues;

    @Inject
    @Configured(required = false)
    @Description("Add elements to this (optional) existing list")
    InputColumn<List<Object>> addToExistingList;

    public void setIncludeNullValues(boolean includeNullValues) {
        this.includeNullValues = includeNullValues;
    }

    public void setValues(InputColumn<?>[] values) {
        this.values = values;
    }

    @Override
    public OutputColumns getOutputColumns() {
        StringBuilder sb = new StringBuilder("List: ");
        for (int i = 0; i < values.length; i++) {
            String key = values[i].getName();
            sb.append(key);
            if (sb.length() > 30) {
                sb.append("...");
                break;
            }

            if (i + 1 < values.length) {
                sb.append(",");
            }
        }
        OutputColumns outputColumns = new OutputColumns(new String[] { sb.toString() }, new Class[] { List.class });
        return outputColumns;
    }

    @Override
    public List<?>[] transform(InputRow row) {
        final List<Object> existingList;
        if (addToExistingList != null) {
            existingList = row.getValue(addToExistingList);
        } else {
            existingList = Collections.emptyList();
        }

        final List<Object> list = new ArrayList<Object>(existingList);

        for (InputColumn<?> column : values) {
            final Object value = row.getValue(column);
            if (!includeNullValues && value == null) {
                logger.debug("Ignoring null value for {} in row: {}", column.getName(), row);
            } else {
                list.add(value);
            }
        }

        final List<?>[] result = new List[] { list };
        return result;
    }

}
