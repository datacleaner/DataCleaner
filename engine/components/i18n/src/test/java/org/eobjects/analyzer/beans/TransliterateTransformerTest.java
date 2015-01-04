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
package org.eobjects.analyzer.beans;

import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MockInputRow;

import junit.framework.TestCase;

public class TransliterateTransformerTest extends TestCase {

	private TransliterateTransformer t;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		t = new TransliterateTransformer();
		t.column = new MockInputColumn<String>("foo", String.class);
	}

	public void testGetOutputColumns() throws Exception {
		assertEquals("OutputColumns[foo (transliterated)]", t.getOutputColumns().toString());
	}

	public void testTransliterateToAscii() throws Exception {
		assertEquals("DataCleaner", t.transform(new MockInputRow().put(t.column, "DataCleaner"))[0]);
		assertEquals("DataClaenor", t.transform(new MockInputRow().put(t.column, "DåtåClænør"))[0]);
        assertEquals("Dannyecistogo", t.transform(new MockInputRow().put(t.column, "Данныечи�?того"))[0]);
        assertEquals("shu ju qing jie", t.transform(new MockInputRow().put(t.column, "數據清潔"))[0]);
        assertEquals("byanat alanzf", t.transform(new MockInputRow().put(t.column, "بيانات الأنظ�?"))[0]);
		assertEquals("du lieu sach hon", t.transform(new MockInputRow().put(t.column, "dữ liệu sạch hơn"))[0]);
	}

	public void testTransliterateToLatin() throws Exception {
		t.latinToAscii = false;
		assertEquals("DataCleaner", t.transform(new MockInputRow().put(t.column, "DataCleaner"))[0]);
		assertEquals("DåtåClænør", t.transform(new MockInputRow().put(t.column, "DåtåClænør"))[0]);
        assertEquals("Dannye�?istogo", t.transform(new MockInputRow().put(t.column, "Данныечи�?того"))[0]);
        assertEquals("shù jù qīng jié", t.transform(new MockInputRow().put(t.column, "數據清潔"))[0]);
        assertEquals("byạnạt ạlạ̉nẓf", t.transform(new MockInputRow().put(t.column, "بيانات الأنظ�?"))[0]);
		assertEquals("dữ liệu sạch hơn", t.transform(new MockInputRow().put(t.column, "dữ liệu sạch hơn"))[0]);
	}

	public void testNull() throws Exception {
		assertEquals(null, t.transform(new MockInputRow().put(t.column, null))[0]);
		assertEquals("", t.transform(new MockInputRow().put(t.column, ""))[0]);
	}
}