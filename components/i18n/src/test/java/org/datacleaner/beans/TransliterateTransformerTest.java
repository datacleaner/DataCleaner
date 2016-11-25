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
package org.datacleaner.beans;

import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;

import junit.framework.TestCase;

public class TransliterateTransformerTest extends TestCase {

    private TransliterateTransformer _transformer;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        _transformer = new TransliterateTransformer();
        _transformer.column = new MockInputColumn<>("foo", String.class);
    }

    public void testGetOutputColumns() throws Exception {
        assertEquals("OutputColumns[foo (transliterated)]", _transformer.getOutputColumns().toString());
    }

    public void testTransliterateToAscii() throws Exception {
        assertEquals("DataCleaner", _transformer.transform(new MockInputRow().put(_transformer.column, "DataCleaner"))[0]);
        assertEquals("DataClaenor", _transformer.transform(new MockInputRow().put(_transformer.column, "DåtåClænør"))[0]);
        assertEquals("Dannyecistogo", _transformer.transform(new MockInputRow().put(_transformer.column, "Данныечистого"))[0]);
        assertEquals("shu ju qing jie", _transformer.transform(new MockInputRow().put(_transformer.column, "數據清潔"))[0]);
        assertEquals("byanat alanzf", _transformer.transform(new MockInputRow().put(_transformer.column, "بيانات الأنظف"))[0]);
        assertEquals("du lieu sach hon", _transformer
                .transform(new MockInputRow().put(_transformer.column, "dữ liệu sạch hơn"))[0]);
    }

    public void testTransliterateToLatin() throws Exception {
        _transformer.latinToAscii = false;
        assertEquals("DataCleaner", _transformer.transform(new MockInputRow().put(_transformer.column, "DataCleaner"))[0]);
        assertEquals("DåtåClænør", _transformer.transform(new MockInputRow().put(_transformer.column, "DåtåClænør"))[0]);
        assertEquals("Dannyečistogo", _transformer.transform(new MockInputRow().put(_transformer.column, "Данныечистого"))[0]);
        assertEquals("shù jù qīng jié", _transformer.transform(new MockInputRow().put(_transformer.column, "數據清潔"))[0]);
        assertEquals("byạnạt ạlạ̉nẓf", _transformer
                .transform(new MockInputRow().put(_transformer.column, "بيانات الأنظف"))[0]);
        assertEquals("dữ liệu sạch hơn", _transformer
                .transform(new MockInputRow().put(_transformer.column, "dữ liệu sạch hơn"))[0]);
    }

    public void testNull() throws Exception {
        assertEquals(null, _transformer.transform(new MockInputRow().put(_transformer.column, null))[0]);
        assertEquals("", _transformer.transform(new MockInputRow().put(_transformer.column, ""))[0]);
    }
}
