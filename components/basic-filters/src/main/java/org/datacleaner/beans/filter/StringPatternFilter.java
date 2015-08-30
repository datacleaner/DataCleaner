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
package org.datacleaner.beans.filter;

import javax.inject.Named;

import org.datacleaner.api.Alias;
import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.Filter;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.Provided;
import org.datacleaner.components.categories.FilterCategory;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.reference.StringPattern;
import org.datacleaner.reference.StringPatternConnection;

@Named("Validate with string pattern")
@Alias("String pattern match")
@Description("Filters values that matches and does not match string patterns")
@Categorized(FilterCategory.class)
public class StringPatternFilter implements Filter<ValidationCategory> {

    @Configured
    InputColumn<String> column;

    @Configured
    StringPattern[] stringPatterns;

    @Configured
    @Description("Require values to match all or just any of the string patterns?")
    MatchFilterCriteria matchCriteria = MatchFilterCriteria.ANY;

    @Provided
    DataCleanerConfiguration configuration;

    private StringPatternConnection[] stringPatternConnections;

    public StringPatternFilter(InputColumn<String> column, StringPattern[] stringPatterns,
            MatchFilterCriteria matchCriteria, DataCleanerConfiguration configuration) {
        this();
        this.column = column;
        this.stringPatterns = stringPatterns;
        this.matchCriteria = matchCriteria;
        this.configuration = configuration;
    }

    public StringPatternFilter() {
    }

    public void init() {
        stringPatternConnections = new StringPatternConnection[stringPatterns.length];
        for (int i = 0; i < stringPatterns.length; i++) {
            stringPatternConnections[i] = stringPatterns[i].openConnection(configuration);
        }
    }

    public void close() {
        if (stringPatternConnections != null) {
            for (StringPatternConnection connection : stringPatternConnections) {
                connection.close();
            }
            stringPatternConnections = null;
        }
    }

    @Override
    public ValidationCategory categorize(InputRow inputRow) {
        String value = inputRow.getValue(column);
        if (value != null) {
            int matches = 0;
            for (StringPatternConnection connection : stringPatternConnections) {
                if (connection.matches(value)) {
                    matches++;
                    if (matchCriteria == MatchFilterCriteria.ANY) {
                        return ValidationCategory.VALID;
                    }
                }
            }
            if (matchCriteria == MatchFilterCriteria.ALL) {
                return ValidationCategory.valueOf(matches == stringPatterns.length);
            }
        }
        return ValidationCategory.INVALID;
    }

}
