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
package org.datacleaner.beans.transform;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;
import org.joda.time.LocalDate;

import junit.framework.TestCase;

public class FormatDateTransformerTest extends TestCase {

    public void testScenario() throws Exception {
        final MockInputColumn<Date> col = new MockInputColumn<Date>("my date");

        final FormatDateTransformer transformer = new FormatDateTransformer();
        transformer.dateColumn = col;

        assertEquals("OutputColumns[my date (formatted)]", transformer.getOutputColumns().toString());

        assertEquals(null, transformer.transform(new MockInputRow().put(col, null))[0]);

        assertEquals("1970-01-01 00:00:00",
                transformer.transform(new MockInputRow().put(col, new LocalDate(1970, 1, 1).toDate()))[0]);

        Date date = new Date();
        assertEquals(new SimpleDateFormat(transformer.dateFormat).format(date),
                transformer.transform(new MockInputRow().put(col, date))[0]);
    }
}
