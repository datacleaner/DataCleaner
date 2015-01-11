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
package org.datacleaner.beans.convert;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.datacleaner.beans.api.Categorized;
import org.datacleaner.beans.api.Configured;
import org.datacleaner.beans.api.Description;
import org.datacleaner.beans.api.Initialize;
import org.datacleaner.beans.api.OutputColumns;
import org.datacleaner.beans.api.Transformer;
import org.datacleaner.beans.categories.ConversionCategory;
import org.datacleaner.beans.categories.DateAndTimeCategory;
import org.datacleaner.data.InputColumn;
import org.datacleaner.data.InputRow;
import org.datacleaner.util.convert.NowDate;
import org.datacleaner.util.convert.TodayDate;
import org.datacleaner.util.convert.YesterdayDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Attempts to convert anything to a Date value
 */
@Named("Convert to date")
@Description("Converts anything to a date (or null).")
@Categorized({ ConversionCategory.class, DateAndTimeCategory.class })
public class ConvertToDateTransformer implements Transformer {

    private static final String[] prototypePatterns = { "yyyy-MM-dd", "dd-MM-yyyy", "MM-dd-yyyy" };

    private static final DateTimeFormatter NUMBER_BASED_DATE_FORMAT_LONG = DateTimeFormat.forPattern("yyyyMMdd");
    private static final DateTimeFormatter NUMBER_BASED_DATE_FORMAT_SHORT = DateTimeFormat.forPattern("yyMMdd");

    private static ConvertToDateTransformer internalInstance;

    @Inject
    @Configured(order = 1)
    InputColumn<?>[] input;

    @Inject
    @Configured(required = false, order = 2)
    Date nullReplacement;

    @Inject
    @Configured(required = false, order = 3)
    String[] dateMasks;

    private DateTimeFormatter[] _dateTimeFormatters;

    public static ConvertToDateTransformer getInternalInstance() {
        if (internalInstance == null) {
            internalInstance = new ConvertToDateTransformer();
            internalInstance.init();
        }
        return internalInstance;
    }

    public ConvertToDateTransformer() {
        dateMasks = getDefaultDateMasks();
    }

    @Initialize
    public void init() {
        if (dateMasks == null) {
            dateMasks = getDefaultDateMasks();
        }

        _dateTimeFormatters = new DateTimeFormatter[dateMasks.length];
        for (int i = 0; i < dateMasks.length; i++) {
            final String dateMask = dateMasks[i];
            _dateTimeFormatters[i] = DateTimeFormat.forPattern(dateMask);
        }
    }

    @Override
    public OutputColumns getOutputColumns() {
        String[] names = new String[input.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = input[i].getName() + " (as date)";
        }
        return new OutputColumns(Date.class, names);
    }

    @Override
    public Date[] transform(InputRow inputRow) {
        Date[] result = new Date[input.length];
        for (int i = 0; i < input.length; i++) {
            Object value = inputRow.getValue(input[i]);
            Date d = transformValue(value);
            if (d == null) {
                d = nullReplacement;
            }
            result[i] = d;
        }
        return result;
    }

    public Date transformValue(Object value) {
        Date d = null;
        if (value != null) {
            if (value instanceof Date) {
                d = (Date) value;
            } else if (value instanceof Calendar) {
                d = ((Calendar) value).getTime();
            } else if (value instanceof String) {
                d = convertFromString((String) value);
            } else if (value instanceof Number) {
                d = convertFromNumber((Number) value, true);
            }
        }
        return d;
    }

    protected Date convertFromString(final String value) {
        if ("now()".equalsIgnoreCase(value)) {
            return new NowDate();
        }
        if ("today()".equalsIgnoreCase(value)) {
            return new TodayDate();
        }
        if ("yesterday()".equalsIgnoreCase(value)) {
            return new YesterdayDate();
        }

        for (DateTimeFormatter formatter : _dateTimeFormatters) {
            try {
                return formatter.parseDateTime(value).toDate();
            } catch (Exception e) {
                // proceed to next formatter
            }
        }

        try {
            long longValue = Long.parseLong(value);
            return convertFromNumber(longValue, false);
        } catch (NumberFormatException e) {
            // do nothing, proceed to dateFormat parsing
        }

        // try also with SimpleDateFormat since it is more fault tolerant in
        // millisecond parsing
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.S");
        try {
            return format.parse(value);
        } catch (ParseException e) {
            // do nothing
        }

        return null;
    }

    protected Date convertFromNumber(Number value) {
        return convertFromNumber(value, true);
    }

    protected Date convertFromNumber(Number value, boolean tryDateTimeFormatters) {
        Number numberValue = (Number) value;
        long longValue = numberValue.longValue();

        String stringValue = Long.toString(longValue);

        if (tryDateTimeFormatters) {
            for (int i = 0; i < _dateTimeFormatters.length; i++) {
                String dateMask = dateMasks[i];
                boolean isPotentialNumberDateMask = dateMask.indexOf("-") == -1 && dateMask.indexOf(".") == -1
                        && dateMask.indexOf("/") == -1;
                if (isPotentialNumberDateMask) {
                    DateTimeFormatter formatter = _dateTimeFormatters[i];
                    try {
                        return formatter.parseDateTime(stringValue).toDate();
                    } catch (Exception e) {
                        // proceed to next formatter
                    }
                }
            }
        }

        // test if the number is actually a format of the type yyyyMMdd
        if (stringValue.length() == 8 && (stringValue.startsWith("1") || stringValue.startsWith("2"))) {
            try {
                return NUMBER_BASED_DATE_FORMAT_LONG.parseDateTime(stringValue).toDate();
            } catch (Exception e) {
                // do nothing, proceed to next method of conversion
            }
        }

        // test if the number is actually a format of the type yyMMdd
        if (stringValue.length() == 6) {
            try {
                return NUMBER_BASED_DATE_FORMAT_SHORT.parseDateTime(stringValue).toDate();
            } catch (Exception e) {
                // do nothing, proceed to next method of conversion
            }
        }

        if (longValue > 5000000) {
            // this number is most probably amount of milliseconds since
            // 1970
            return new Date(longValue);
        } else {
            // this number is most probably the amount of days since
            // 1970
            return new Date(longValue * 1000 * 60 * 60 * 24);
        }
    }

    private String[] getDefaultDateMasks() {
        final List<String> defaultDateMasks = new ArrayList<String>();

        defaultDateMasks.add("yyyy-MM-dd HH:mm:ss.S");
        defaultDateMasks.add("yyyy-MM-dd HH:mm:ss");
        defaultDateMasks.add("yyyy-MM-dd HH:mm");
        defaultDateMasks.add("yyyyMMddHHmmssZ");
        defaultDateMasks.add("yyMMddHHmmssZ");

        for (String string : prototypePatterns) {
            defaultDateMasks.add(string);
            string = string.replaceAll("\\-", "\\.");
            defaultDateMasks.add(string);
            string = string.replaceAll("\\.", "\\/");
            defaultDateMasks.add(string);
        }

        return defaultDateMasks.toArray(new String[defaultDateMasks.size()]);
    }

    public String[] getDateMasks() {
        return dateMasks;
    }

    public Date getNullReplacement() {
        return nullReplacement;
    }

    public void setNullReplacement(Date nullReplacement) {
        this.nullReplacement = nullReplacement;
    }
}
