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
package org.datacleaner.test.mock;

import org.apache.metamodel.query.Query;
import org.easymock.IArgumentMatcher;
import org.junit.Assert;

/**
 * An EasyMock matcher for checking that a {@link Query} matches a certain string.
 */
public class QueryMatcher implements IArgumentMatcher {

    private String queryToString;

    public QueryMatcher(final String queryToString) {
        if (queryToString == null) {
            throw new NullPointerException();
        }
        this.queryToString = queryToString;
    }

    @Override
    public boolean matches(final Object argument) {
        final Query q = (Query) argument;
        final String sql = q.toString();
        Assert.assertEquals(queryToString, sql);
        return true;
    }

    @Override
    public void appendTo(final StringBuffer buffer) {
        buffer.append("QueryMatcher(" + queryToString + ")");
    }
}
