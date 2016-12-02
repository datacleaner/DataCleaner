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
package org.datacleaner.components.convert;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.inject.Inject;
import javax.inject.Named;

import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.Initialize;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.Transformer;
import org.datacleaner.api.Validate;
import org.datacleaner.components.categories.ConversionCategory;
import org.datacleaner.util.convert.NowDate;
import org.datacleaner.util.convert.ShiftedToday;
import org.datacleaner.util.convert.TodayDate;
import org.datacleaner.util.convert.YesterdayDate;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Attempts to convert anything to a Date value
 */
@Named("Convert to date")
@Description("Converts anything to a date (or null).")
@Categorized(ConversionCategory.class)
public class ConvertToDateTransformer implements Transformer {

    private static final String[] prototypePatterns = { "yyyy-MM-dd", "dd-MM-yyyy", "MM-dd-yyyy" };

    private static ConvertToDateTransformer internalInstance;

    @Inject
    @Configured(order = 1)
    InputColumn<?>[] input;

    @Inject
    @Configured(order = 2)
    @Description("Default time zone to use if the date mask does not itself specify the time zone.")
    String timeZone = TimeZone.getDefault().getID();

    @Inject
    @Configured(required = false, order = 3)
    @Description("What value to return when the string cannot be parsed using any of the date masks.")
    Date nullReplacement;

    @Inject
    @Configured(required = false, order = 4)
    @Description("A sequence of date masks that will be tested from first to last until a match is found.")
    String[] dateMasks;

    private DateTimeFormatter[] _dateTimeFormatters;
    private DateTimeFormatter _numberBasedDateTimeFormatterLong;
    private DateTimeFormatter _numberBasedDateTimeFormatterShort;

    public ConvertToDateTransformer() {
        dateMasks = getDefaultDateMasks();
    }

    public static ConvertToDateTransformer getInternalInstance() {
        if (internalInstance == null) {
            // because we are not synchronized, cannot assign to internalInstance directly,
            // to prevent usage of uninitialized instance.
            final ConvertToDateTransformer newInst = new ConvertToDateTransformer();
            newInst.init();
            internalInstance = newInst;
        }
        return internalInstance;
    }

    @Validate
    public void validate() {
        try {
            TimeZone.getTimeZone(timeZone);
        } catch (final Exception e) {
            throw new IllegalStateException("Time zone '" + timeZone + "' not recognized.");
        }
    }

    @Initialize
    public void init() {
        if (dateMasks == null) {
            dateMasks = getDefaultDateMasks();
        }

        final DateTimeZone zone = DateTimeZone.forID(timeZone);

        _numberBasedDateTimeFormatterLong = DateTimeFormat.forPattern("yyyyMMdd").withZone(zone);
        _numberBasedDateTimeFormatterShort = DateTimeFormat.forPattern("yyMMdd").withZone(zone);

        _dateTimeFormatters = new DateTimeFormatter[dateMasks.length];
        for (int i = 0; i < dateMasks.length; i++) {
            final String dateMask = dateMasks[i];
            _dateTimeFormatters[i] = DateTimeFormat.forPattern(dateMask).withZone(zone);
        }
    }

    @Override
    public OutputColumns getOutputColumns() {
        final String[] names = new String[input.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = input[i].getName() + " (as date)";
        }
        return new OutputColumns(Date.class, names);
    }

    @Override
    public Date[] transform(final InputRow inputRow) {
        final Date[] result = new Date[input.length];
        for (int i = 0; i < input.length; i++) {
            final Object value = inputRow.getValue(input[i]);
            Date d = transformValue(value);
            if (d == null) {
                d = nullReplacement;
            }
            result[i] = d;
        }
        return result;
    }

    public Date transformValue(final Object value) {
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
        if (value == null) {
            return null;
        }

        if ("now()".equalsIgnoreCase(value)) {
            return new NowDate();
        }
        if ("today()".equalsIgnoreCase(value)) {
            return new TodayDate();
        }
        if ("yesterday()".equalsIgnoreCase(value)) {
            return new YesterdayDate();
        }
        if (value.matches("shifted_today(.+)")) {
            return new ShiftedToday(value);
        }

        for (final DateTimeFormatter formatter : _dateTimeFormatters) {
            try {
                return formatter.parseDateTime(value).toDate();
            } catch (final Exception e) {
                // proceed to next formatter
            }
        }

        try {
            final long longValue = Long.parseLong(value);
            return convertFromNumber(longValue, false);
        } catch (final NumberFormatException e) {
            // do nothing, proceed to dateFormat parsing
        }

        // try also with SimpleDateFormat since it is more fault tolerant in
        // millisecond parsing
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.S");
        format.setTimeZone(TimeZone.getTimeZone(timeZone));
        try {
            return format.parse(value);
        } catch (final ParseException e) {
            // do nothing
        }

        return null;
    }

    protected Date convertFromNumber(final Number value) {
        return convertFromNumber(value, true);
    }

    protected Date convertFromNumber(final Number value, final boolean tryDateTimeFormatters) {
        final long longValue = value.longValue();

        final String stringValue = Long.toString(longValue);

        if (tryDateTimeFormatters) {
            for (int i = 0; i < _dateTimeFormatters.length; i++) {
                final String dateMask = dateMasks[i];
                final boolean isPotentialNumberDateMask =
                        !dateMask.contains("-") && !dateMask.contains(".") && !dateMask.contains("/");
                if (isPotentialNumberDateMask) {
                    final DateTimeFormatter formatter = _dateTimeFormatters[i];
                    try {
                        return formatter.parseDateTime(stringValue).toDate();
                    } catch (final Exception e) {
                        // proceed to next formatter
                    }
                }
            }
        }

        // test if the number is actually a format of the type yyyyMMdd
        if (stringValue.length() == 8 && (stringValue.startsWith("1") || stringValue.startsWith("2"))) {
            try {
                return _numberBasedDateTimeFormatterLong.parseDateTime(stringValue).toDate();
            } catch (final Exception e) {
                // do nothing, proceed to next method of conversion
            }
        }

        // test if the number is actually a format of the type yyMMdd
        if (stringValue.length() == 6) {
            try {
                return _numberBasedDateTimeFormatterShort.parseDateTime(stringValue).toDate();
            } catch (final Exception e) {
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
        final List<String> defaultDateMasks = new ArrayList<>();

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

    public void setNullReplacement(final Date nullReplacement) {
        this.nullReplacement = nullReplacement;
    }
}
