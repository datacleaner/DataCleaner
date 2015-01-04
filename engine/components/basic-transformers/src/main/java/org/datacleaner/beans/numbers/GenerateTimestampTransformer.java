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

import org.datacleaner.beans.api.Categorized;
import org.datacleaner.beans.api.Configured;
import org.datacleaner.beans.api.Description;
import org.datacleaner.beans.api.OutputColumns;
import org.datacleaner.beans.api.Transformer;
import org.datacleaner.beans.api.TransformerBean;
import org.datacleaner.beans.categories.DateAndTimeCategory;
import org.datacleaner.beans.categories.NumbersCategory;
import org.datacleaner.data.InputColumn;
import org.datacleaner.data.InputRow;
import org.apache.metamodel.util.HasName;

@TransformerBean("Generate timestamp")
@Description("Generates a timestamp representing the millisecond or nanosecond of processing the record")
@Categorized({ NumbersCategory.class, DateAndTimeCategory.class })
public class GenerateTimestampTransformer implements Transformer<Long> {

    public static enum Unit implements HasName {
        SECOND("Second"), MILLISECOND("Millisecond"), NANOSECOND("Nanosecond");

        private final String _name;

        private Unit(String name) {
            _name = name;
        }

        @Override
        public String getName() {
            return _name;
        }
    }

    @Configured
    @Description("A column which represent the scope for which the ID will be generated. "
            + "If eg. a source column is selected, an ID will be generated for each source record. "
            + "If a transformed column is selected, an ID will be generated for each record generated that has this column.")
    InputColumn<?> columnInScope;

    @Configured
    Unit unit = Unit.SECOND;

    @Override
    public OutputColumns getOutputColumns() {
        return new OutputColumns("Generated timestamp");
    }

    @Override
    public Long[] transform(InputRow inputRow) {
        final long result;
        if (unit == Unit.NANOSECOND) {
            result = System.nanoTime();
        } else if (unit == Unit.SECOND) {
            result = System.currentTimeMillis() / 1000;
        } else {
            result = System.currentTimeMillis();
        }
        return new Long[] { result };
    }

}
