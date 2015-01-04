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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.datacleaner.beans.api.Categorized;
import org.datacleaner.beans.api.Configured;
import org.datacleaner.beans.api.Description;
import org.datacleaner.beans.api.OutputColumns;
import org.datacleaner.beans.api.Transformer;
import org.datacleaner.beans.api.TransformerBean;
import org.datacleaner.beans.categories.DateAndTimeCategory;
import org.datacleaner.data.InputColumn;
import org.datacleaner.data.InputRow;

@TransformerBean("Format date")
@Description("Allows you to format a date as a string by applying your own date format.")
@Categorized({ DateAndTimeCategory.class })
public class FormatDateTransformer implements Transformer<String> {

    @Configured("Date")
    InputColumn<Date> dateColumn;

    @Configured
    String dateFormat = "yyyy-MM-dd HH:mm:ss";

    @Override
    public OutputColumns getOutputColumns() {
        return new OutputColumns(dateColumn.getName() + " (formatted)");
    }

    @Override
    public String[] transform(InputRow inputRow) {
        Date date = inputRow.getValue(dateColumn);
        if (date == null) {
            return new String[] { null };
        }

        SimpleDateFormat format = new SimpleDateFormat(dateFormat);
        return new String[] { format.format(date) };
    }

}
