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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eobjects.analyzer.beans.api.Categorized;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.beans.categories.DateAndTimeCategory;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

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
