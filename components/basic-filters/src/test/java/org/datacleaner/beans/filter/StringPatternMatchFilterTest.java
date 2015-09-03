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

import junit.framework.TestCase;

import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;
import org.datacleaner.descriptors.Descriptors;
import org.datacleaner.descriptors.FilterDescriptor;
import org.datacleaner.lifecycle.LifeCycleHelper;
import org.datacleaner.reference.RegexStringPattern;
import org.datacleaner.reference.StringPattern;

public class StringPatternMatchFilterTest extends TestCase {

    private final DataCleanerConfiguration configuration = new DataCleanerConfigurationImpl();

    private final FilterDescriptor<StringPatternFilter, ValidationCategory> descriptor = Descriptors
            .ofFilter(StringPatternFilter.class);

    public void testFilterSinglePattern() throws Exception {
        final LifeCycleHelper lifeCycleHelper = new LifeCycleHelper(new DataCleanerConfigurationImpl(), null, true);

        final StringPattern stringPattern = new RegexStringPattern("very simple email pattern", ".+@.+", true);
        final MockInputColumn<String> column = new MockInputColumn<String>("my col", String.class);
        StringPatternFilter filter = new StringPatternFilter(column, new StringPattern[] { stringPattern },
                MatchFilterCriteria.ANY, configuration);

        lifeCycleHelper.initialize(descriptor, filter);

        assertEquals(ValidationCategory.VALID, filter.categorize(new MockInputRow().put(column, "kasper@eobjects.org")));
        assertEquals(ValidationCategory.INVALID, filter.categorize(new MockInputRow().put(column, "kasper@")));

        lifeCycleHelper.close(descriptor, filter, true);

        // it shouldn't matter if ANY or ALL criteria is being used
        filter = new StringPatternFilter(column, new StringPattern[] { stringPattern }, MatchFilterCriteria.ALL,
                configuration);
        filter.init();
        assertEquals(ValidationCategory.VALID, filter.categorize(new MockInputRow().put(column, "kasper@eobjects.org")));
        assertEquals(ValidationCategory.INVALID, filter.categorize(new MockInputRow().put(column, "kasper@")));
        filter.close();
    }

    public void testFilterMultiplePatterns() throws Exception {
        StringPattern stringPattern1 = new RegexStringPattern("very simple email pattern", ".+@.+", true);
        StringPattern stringPattern2 = new RegexStringPattern("something with 'kas'", ".*kas.*", true);
        MockInputColumn<String> column = new MockInputColumn<String>("my col", String.class);
        StringPatternFilter filter = new StringPatternFilter(column, new StringPattern[] { stringPattern1,
                stringPattern2 }, MatchFilterCriteria.ANY, configuration);
        filter.init();
        assertEquals(ValidationCategory.VALID, filter.categorize(new MockInputRow().put(column, "kasper@eobjects.org")));
        assertEquals(ValidationCategory.VALID, filter.categorize(new MockInputRow().put(column, "kasper@")));
        assertEquals(ValidationCategory.INVALID, filter.categorize(new MockInputRow().put(column, "ankit@")));
        filter.close();

        filter = new StringPatternFilter(column, new StringPattern[] { stringPattern1, stringPattern2 },
                MatchFilterCriteria.ALL, configuration);
        filter.init();
        assertEquals(ValidationCategory.VALID, filter.categorize(new MockInputRow().put(column, "kasper@eobjects.org")));
        assertEquals(ValidationCategory.INVALID, filter.categorize(new MockInputRow().put(column, "kasper@")));
        assertEquals(ValidationCategory.INVALID, filter.categorize(new MockInputRow().put(column, "ankit@")));
        filter.close();
    }
}
