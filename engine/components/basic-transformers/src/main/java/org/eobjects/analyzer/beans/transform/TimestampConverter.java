/**
 * AnalyzerBeans
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
package org.eobjects.analyzer.beans.transform;

import java.util.Date;

import org.eobjects.analyzer.beans.api.Categorized;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.beans.categories.DateAndTimeCategory;
import org.eobjects.analyzer.beans.convert.ConvertToNumberTransformer;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.apache.metamodel.util.HasName;

@TransformerBean("Timestamp converter")
@Description("Convert a timestamp (string or number) to a date field. Epoch is assumed to be 1970-01-01.")
@Categorized({ DateAndTimeCategory.class })
public class TimestampConverter implements Transformer<Date> {

	public static enum Unit implements HasName {
		DAYS("Days", 24 * 60 * 60 * 1000), HOURS("Hours", 60 * 60 * 1000), MINUTES("Minutes", 60 * 1000), SECONDS("Seconds",
				1000), MILLIS("Milliseconds", 1);

		private final String _name;
		private final int _millisPerUnit;

		private Unit(String name, int millisPerUnit) {
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
		return new OutputColumns(timestampColumn.getName() + " (as date)");
	}

	@Override
	public Date[] transform(InputRow inputRow) {
		final Date[] result = new Date[1];
		final Object value = inputRow.getValue(timestampColumn);
		final Number number = ConvertToNumberTransformer.transformValue(value);

		if (number != null) {
			Date timestampedDate = new Date(number.longValue() * unit.getMillisPerUnit());
			result[0] = timestampedDate;
		}

		return result;
	}
}
