/* DataCleaner (community edition)
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
package org.datacleaner.metamodel.datahub.utils;

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.metamodel.data.Row;
import org.apache.metamodel.schema.Column;
import org.datacleaner.metamodel.datahub.DatahubDataSet;

import com.fasterxml.jackson.core.JsonParseException;

public class JsonQueryResultParserHelperTest extends TestCase{
    public void testShouldParseQueryResult() throws JsonParseException, IOException {
        String result = "{\"table\":{\"header\":[\"CUSTOMERNUMBER\",\"CUSTOMERNAME\"],\"rows\":[[\"bla1\", \"blieb1\"],[\"bla2\", \"blieb2\"]]}}";
        JsonQueryResultParserHelper parser = new JsonQueryResultParserHelper();
        Column[] columns = new Column[2];
        columns[0] = new DatahubColumnBuilder().withName("CUSTOMERNUMBER").withNumber(1).build();
        columns[1] = new DatahubColumnBuilder().withName("CUSTOMERNAME").withNumber(2).build();
        DatahubDataSet dataset = parser.parseQueryResult(result, columns);
        assertNotNull(dataset);
        assertTrue( dataset.next());
        Row row = dataset.getRow();
        assertEquals(2, row.size());
        assertEquals("bla1", row.getValue(0));
        assertEquals("blieb1", row.getValue(1));
        assertTrue( dataset.next());
        row = dataset.getRow();
        assertEquals(2, row.size());
        assertEquals("bla2", row.getValue(0));
        assertEquals("blieb2", row.getValue(1));
    }
}
