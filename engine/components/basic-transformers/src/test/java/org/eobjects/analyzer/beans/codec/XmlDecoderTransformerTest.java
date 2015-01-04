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
package org.eobjects.analyzer.beans.codec;

import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.codec.XmlDecoderTransformer;
import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MockInputRow;

import junit.framework.TestCase;

public class XmlDecoderTransformerTest extends TestCase {

	private XmlDecoderTransformer transformer;
	private MockInputColumn<String> column;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		column = new MockInputColumn<String>("mock", String.class);
		transformer = new XmlDecoderTransformer(column);
	}

	public void testGetOutputColumns() throws Exception {
		OutputColumns outputColumns = transformer.getOutputColumns();
		assertEquals(1, outputColumns.getColumnCount());
		assertEquals("mock (XML decoded)", outputColumns.getColumnName(0));
	}

	public void testNull() throws Exception {
		String[] result = transformer.transform(new MockInputRow());
		assertEquals(1, result.length);
		assertEquals(null, result[0]);
	}

	public void testNoXml() throws Exception {
		String[] result = transformer.transform(new MockInputRow().put(column, "hello world"));
		assertEquals(1, result.length);
		assertEquals("hello world", result[0]);
	}

	public void testSimpleAmp() throws Exception {
		String[] result = transformer.transform(new MockInputRow().put(column, "hello gartner &amp; forrester"));
		assertEquals(1, result.length);
		assertEquals("hello gartner & forrester", result[0]);
	}

	public void testFullDoc() throws Exception {
		String[] result = transformer.transform(new MockInputRow().put(column,
				"<greeting lang=\"en\">Hello</greeting><who>World</who>"));
		assertEquals(1, result.length);
		assertEquals("<greeting lang=\"en\">Hello</greeting><who>World</who>", result[0]);
	}
}
