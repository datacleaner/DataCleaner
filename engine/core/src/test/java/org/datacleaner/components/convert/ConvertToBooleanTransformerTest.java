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
package org.datacleaner.components.convert;

import org.datacleaner.api.OutputColumns;
import org.datacleaner.components.convert.ConvertToBooleanTransformer;
import org.datacleaner.data.MockInputColumn;

import junit.framework.TestCase;

public class ConvertToBooleanTransformerTest extends TestCase {

	public void testGetOutputColumns() throws Exception {
		ConvertToBooleanTransformer t = new ConvertToBooleanTransformer();
		t.setInput(new MockInputColumn<String>("my col", String.class));
		OutputColumns outputColumns = t.getOutputColumns();
		
		assertEquals(1, outputColumns.getColumnCount());
		assertEquals("my col (as boolean)", outputColumns.getColumnName(0));
	}

	public void testTransform() throws Exception {
		String[] f = ConvertToBooleanTransformer.DEFAULT_FALSE_TOKENS;
		String[] t = ConvertToBooleanTransformer.DEFAULT_TRUE_TOKENS;
		
		assertNull(ConvertToBooleanTransformer.transformValue(null, t, f));

		assertNull(ConvertToBooleanTransformer.transformValue("hello", t, f));
		assertNull(ConvertToBooleanTransformer.transformValue("", t, f));
		assertNull(ConvertToBooleanTransformer.transformValue(-1, t, f));
		assertNull(ConvertToBooleanTransformer.transformValue(5000, t, f));

		assertTrue(ConvertToBooleanTransformer.transformValue(true, t, f));
		assertTrue(ConvertToBooleanTransformer.transformValue(Boolean.TRUE, t, f));
		assertTrue(ConvertToBooleanTransformer.transformValue("true", t, f));
		assertTrue(ConvertToBooleanTransformer.transformValue(1, t, f));
		assertTrue(ConvertToBooleanTransformer.transformValue("yes", t, f));
		assertTrue(ConvertToBooleanTransformer.transformValue("tRUe", t, f));
		assertTrue(ConvertToBooleanTransformer.transformValue("1", t, f));

		assertFalse(ConvertToBooleanTransformer.transformValue(false, t, f));
		assertFalse(ConvertToBooleanTransformer.transformValue("false", t, f));
		assertFalse(ConvertToBooleanTransformer.transformValue(0, t, f));
		assertFalse(ConvertToBooleanTransformer.transformValue("no", t, f));
		assertFalse(ConvertToBooleanTransformer.transformValue("fALse", t, f));
		assertFalse(ConvertToBooleanTransformer.transformValue("0", t, f));
	}
}
