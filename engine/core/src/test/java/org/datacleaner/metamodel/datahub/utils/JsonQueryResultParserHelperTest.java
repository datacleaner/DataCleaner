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

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import org.apache.metamodel.schema.Column;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;

public class JsonQueryResultParserHelperTest {

    @Test
    public void testShouldParseQueryResult()
            throws JsonParseException, IOException {
        String result = "{\"table\":{\"header\":[\"CUSTOMERNUMBER\",\"CUSTOMERNAME\",\"LINKAGE\"],\"rows\":[[\"bla1\",null,\"[{source_name=SRCA1, source_id=316013}, {source_name=SRCA1, source_id=394129}]\"],[\"bla2\",\"blieb2\",\"[{source_name=SRCA2, source_id=316013}, {source_name=SRCA2, source_id=394129}]\"]]}}";
        InputStream is = new ByteArrayInputStream(result.getBytes());
        JsonQueryDatasetResponseParser parser = new JsonQueryDatasetResponseParser();
        Column[] columns = new Column[3];
        columns[0] = new DatahubColumnBuilder().withName("CUSTOMERNUMBER")
                .withNumber(1).build();
        columns[1] = new DatahubColumnBuilder().withName("CUSTOMERNAME")
                .withNumber(2).build();
        columns[2] = new DatahubColumnBuilder().withName("LINKAGE")
                .withNumber(3).build();
        List<Object[]> queryResult = parser.parseQueryResult(is);
        Iterator<Object[]> iterator = queryResult.iterator();

        Object[] record = iterator.next();
        assertEquals("bla1", record[0]);
        assertEquals(null, record[1]);
        assertEquals(
                "[{source_name=SRCA1, source_id=316013}, {source_name=SRCA1, source_id=394129}]",
                record[2]);

        record = iterator.next();
        assertEquals("bla2", record[0]);
        assertEquals("blieb2", record[1]);
        assertEquals(
                "[{source_name=SRCA2, source_id=316013}, {source_name=SRCA2, source_id=394129}]",
                record[2]);

    }

}
