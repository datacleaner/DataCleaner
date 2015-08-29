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
import org.datacleaner.api.Close;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.Filter;
import org.datacleaner.api.Initialize;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.Provided;
import org.datacleaner.components.categories.FilterCategory;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.reference.Dictionary;
import org.datacleaner.reference.DictionaryConnection;

@Named("Validate in dictionary")
@Alias("Dictionary lookup")
@Description("Filters values based on their existence in a dictionary")
@Categorized(FilterCategory.class)
public class DictionaryFilter implements Filter<DictionaryFilter.Category> {

    public static enum Category {
        VALID, INVALID;
    }

    @Configured
    InputColumn<String> column;

    @Configured
    Dictionary dictionary;

    @Provided
    DataCleanerConfiguration configuration;

    private DictionaryConnection dictionaryConnection;

    public DictionaryFilter() {
    }

    public DictionaryFilter(InputColumn<String> column, Dictionary dictionary) {
        this();
        this.column = column;
        this.dictionary = dictionary;
    }
    
    @Initialize
    public void init() {
        dictionaryConnection = dictionary.openConnection(configuration);
    }
    
    @Close
    public void close() {
        if (dictionaryConnection != null) {
            dictionaryConnection.close();
            dictionaryConnection = null;
        }
    }

    @Override
    public Category categorize(InputRow inputRow) {
        String value = inputRow.getValue(column);
        if (value != null) {
            if (dictionaryConnection.containsValue(value)) {
                return Category.VALID;
            }
        }
        return Category.INVALID;
    }

}
