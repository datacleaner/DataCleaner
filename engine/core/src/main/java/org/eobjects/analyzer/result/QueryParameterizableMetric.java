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
package org.eobjects.analyzer.result;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eobjects.analyzer.beans.api.ParameterizableMetric;
import org.eobjects.analyzer.util.convert.StringConverter;

/**
 * An abstract parser of metric parameters which supports IN [...] and NOT IN
 * [...] expressions.
 * 
 * @see Metric#supportsInClause()
 */
public abstract class QueryParameterizableMetric implements ParameterizableMetric {

    private final Pattern _pattern;

    public QueryParameterizableMetric() {
        _pattern = Pattern.compile("(NOT )?IN (\\[.+\\])", Pattern.CASE_INSENSITIVE);
    }

    @Override
    public final Number getValue(final String parameter) {
        final String trimmedParameter = parameter.trim();
        final Matcher matcher = _pattern.matcher(trimmedParameter);
        if (matcher.matches()) {
            String group = matcher.group(2);
            StringConverter conv = new StringConverter(null);
            String[] values = conv.deserialize(group, String[].class);
            int sum = 0;
            for (String value : values) {
                sum += getInstanceCount(value);
            }
            if (trimmedParameter.toUpperCase().startsWith("NOT IN")) {
                return getTotalCount() - sum;
            } else {
                return sum;
            }
        }

        Integer count = getInstanceCount(parameter);
        if (count == null) {
            return 0;
        }
        return count;
    }

    /**
     * Returns the abstract "total count" of which all "NOT IN" elements will be
     * subtracted.
     * 
     * @return
     */
    protected abstract int getTotalCount();

    /**
     * Returns the metric count of a single value, either because it was queried
     * stand-alone or because it was a part of an IN [...] or NOT IN [...]
     * expression.
     * 
     * @param instance
     * @return
     */
    protected abstract int getInstanceCount(String instance);
}
