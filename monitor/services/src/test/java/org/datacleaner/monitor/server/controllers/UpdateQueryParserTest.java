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
package org.datacleaner.monitor.server.controllers;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.apache.metamodel.UpdateableDataContext;
import org.hamcrest.collection.IsMapContaining;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UpdateQueryParserTest {
    
    private static final String TABLE_NAME = "tablename";
    private static final String COLUMN1 = "column1";
    private static final String COLUMN2 = "column2";
    private static final String COLUMN3 = "column3";
    private static final Object VALUE1 = " value1";
    private static final Object VALUE2 = " value2";
    private static final Object VALUE3 = " value3";
    
    private static final String QUERY1 = new StringBuilder()
            .append("UPDATE ") //
            .append(TABLE_NAME) //
            .append(" SET ") //
            .append(COLUMN1) //
            .append(" = ") //
            .append(VALUE1) //
            .append(", ") //
            .append(COLUMN2) //
            .append(" = ") //
            .append(VALUE2) //
            .append(" WHERE " ) //
            .append(COLUMN3) //
            .append(" = ") //
            .append(VALUE3).toString(); //
    
    private static final String QUERY2 = "INSERT INTO tablename VALUES (value1, value2, value3)";
    
    @Mock
    private UpdateableDataContext dataContext;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    
    @Test
    public void dataContextShouldNotBeNull() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("DataContext cannot be null");
        new UpdateQueryParser(null, "xxx");
    }
    
    @Test
    public void updateQueryShouldNotBeNull() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Query string cannot be null");
        new UpdateQueryParser(dataContext, null);
    }
    
    @Test
    public void shouldBuildUpdateQueriesSuccessfully() {
        UpdateQueryParser parser = new UpdateQueryParser(dataContext, QUERY1);
        UpdateQuery updateQuery = parser.parseUpdate();
        assertThat(updateQuery.getTable(), is(equalTo(TABLE_NAME)));
        Map<String, Object> updateColumns = updateQuery.getUpdateColumns();
        assertThat(updateColumns, allOf(hasEntry(COLUMN2, VALUE2), hasEntry(COLUMN1, VALUE1)));
    }

}
