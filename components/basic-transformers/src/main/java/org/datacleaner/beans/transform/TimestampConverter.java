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
package org.datacleaner.beans.transform;

import java.util.Date;

import javax.inject.Named;

import org.apache.metamodel.util.HasName;
import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.Transformer;
import org.datacleaner.components.categories.DateAndTimeCategory;
import org.datacleaner.components.convert.ConvertToNumberTransformer;

@Named("Timestamp converter")
@Description("Convert a timestamp (string or number) to a date field. Epoch is assumed to be 1970-01-01.")
@Categorized(DateAndTimeCategory.class)
public class TimestampConverter implements Transformer {

    public enum Unit implements HasName {
        DAYS("Days", 24 * 60 * 60 * 1000), HOURS("Hours", 60 * 60 * 1000), MINUTES("Minutes", 60 * 1000),
        SECONDS("Seconds", 1000), MILLIS("Milliseconds", 1);

        private final String _name;
        private final int _millisPerUnit;

        Unit(final String name, final int millisPerUnit) {
            _name = name;
            _millisPerUnit = millisPerUnit;
        }

        @Override
        public String getName() {
            return _name;
        }

        public int getMillisPerUnit() {
            return _millisPerUnit;
        }
    }

    @Configured
    InputColumn<?> timestampColumn;

    @Configured
    @Description("The unit of measure for comparing the timestamp to the epoch")
    Unit unit = Unit.SECONDS;

    @Override
    public OutputColumns getOutputColumns() {
        return new OutputColumns(Date.class, timestampColumn.getName() + " (as date)");
    }

    @Override
    public Date[] transform(final InputRow inputRow) {
        final Date[] result = new Date[1];
        final Object value = inputRow.getValue(timestampColumn);
        final Number number = ConvertToNumberTransformer.transformValue(value);

        if (number != null) {
            final Date timestampedDate = new Date(number.longValue() * unit.getMillisPerUnit());
            result[0] = timestampedDate;
        }

        return result;
    }
}
