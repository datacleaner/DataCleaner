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

import java.util.Collection;

import junit.framework.TestCase;

public class QueryParameterizableMetricTest extends TestCase {

    final QueryParameterizableMetric metric = new QueryParameterizableMetric() {
        
        @Override
        public Collection<String> getParameterSuggestions() {
            return null;
        }

        @Override
        public int getTotalCount() {
            return 900;
        }

        @Override
        public int getInstanceCount(String instance) {
            if ("foo".equals(instance)) {
                return 200;
            }
            if ("bar".equals(instance)) {
                return 300;
            }
            if ("baz".equals(instance)) {
                return 400;
            }
            return 0;
        }
    };

    public void testSingleQueryParam() throws Exception {
        assertEquals(200, metric.getValue("foo"));
        assertEquals(0, metric.getValue("foobar"));
    }

    public void testInClause() throws Exception {
        assertEquals(500, metric.getValue("IN [foo,bar]"));
        assertEquals(500, metric.getValue("IN [bar,foo]"));
        assertEquals(600, metric.getValue("IN [foo,baz]"));
    }
    
    public void testDanglingWhitespaces() throws Exception {
        assertEquals(500, metric.getValue("  IN [foo,bar]  "));
        assertEquals(400, metric.getValue("  NOT IN [foo,bar] \t"));
    }
    
    public void testCaseInsensitiveMatch() throws Exception {
        assertEquals(600, metric.getValue("iN [foo,baz]"));
        assertEquals(300, metric.getValue("noT In [foo,baz]"));
    }

    public void testNotInClause() throws Exception {
        assertEquals(400, metric.getValue("NOT IN [foo,bar]"));
        assertEquals(400, metric.getValue("NOT IN [bar,foo]"));
        assertEquals(300, metric.getValue("NOT IN [foo,baz]"));
    }
}
