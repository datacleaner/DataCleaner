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
package org.datacleaner.beans.numbers;

import java.util.UUID;

import javax.inject.Named;

import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.Transformer;
import org.datacleaner.beans.categories.NumbersCategory;

@Named("Generate UUID")
@Description("Generates a universally unique ID")
@Categorized(NumbersCategory.class)
public class GenerateUUIDTransformer implements Transformer {

    @Configured
    @Description("A column which represent the scope for which the ID will be generated. "
            + "If eg. a source column is selected, an ID will be generated for each source record. "
            + "If a transformed column is selected, an ID will be generated for each record generated that has this column.")
    InputColumn<?> columnInScope;

    @Configured(required = false, order = 10)
    String prefix;

    @Configured(required = false, order = 11)
    String postfix;

    @Override
    public OutputColumns getOutputColumns() {
        return new OutputColumns(String.class, "Generated UUID");
    }

    @Override
    public String[] transform(InputRow inputRow) {
        final String uuid = UUID.randomUUID().toString();
        final String result = (prefix == null ? "" : prefix) + uuid + (postfix == null ? "" : postfix);
        return new String[] { result };
    }

}
