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
package org.datacleaner.util.convert;

import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Special date value, created by shifting "today" according to the String input in format: "+1d +2m -3y".
 */
public class ShiftedToday extends Date implements ExpressionDate {

    private static final long serialVersionUID = 1L;
    private final String _input;

    public ShiftedToday(final String input) {
        _input = validateInput(input);
        final int[] counts = parseCounts();

        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DAY_OF_MONTH, counts[0]);
        calendar.add(Calendar.MONTH, counts[1]);
        calendar.add(Calendar.YEAR, counts[2]);

        setTime(calendar.getTime().getTime());
    }

    private static String validateInput(final String input) {
        final String regexp = "shifted_today[(](.+)[)]";
        final Pattern pattern = Pattern.compile(regexp);
        final Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return input;
    }

    private int[] parseCounts() {
        final String regexp = "^.*?([-+]?[0-9]+)[dD] *([-+]?[0-9]+)[mM] *([-+]?[0-9]+)[yY].*$";
        final Pattern pattern = Pattern.compile(regexp);
        final Matcher matcher = pattern.matcher(_input);

        if (!matcher.find()) {
            throw new RuntimeException(
                    String.format("Specified value ('%s') does not match allowed format (1d 2m 3y). ", _input));
        }

        return new int[] { Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)),
                Integer.parseInt(matcher.group(3)) };
    }

    public String getInput() {
        return _input;
    }

    @Override
    public String getExpression() {
        return String.format("shifted_today(%s)", _input);
    }
}
