/**
 * AnalyzerBeans
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
package org.eobjects.analyzer.storage;

import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MockInputRow;

import junit.framework.TestCase;

public class InMemoryRowAnnotationFactoryTest extends TestCase {

	public void testGetValueCounts() throws Exception {
		InMemoryRowAnnotationFactory f = new InMemoryRowAnnotationFactory();
		RowAnnotation a = f.createAnnotation();

		MockInputColumn<String> col1 = new MockInputColumn<String>("greeting", String.class);
		MockInputColumn<String> col2 = new MockInputColumn<String>("greeter", String.class);

		f.annotate(new MockInputRow(1).put(col1, "hello").put(col2, "world"), 3, a);

		assertEquals(3, f.getValueCounts(a, col1).get("hello").intValue());
		assertEquals(3, f.getValueCounts(a, col2).get("world").intValue());
		
		f.annotate(new MockInputRow(2).put(col1, "hi").put(col2, "world"), 2, a);
		
		assertEquals(3, f.getValueCounts(a, col1).get("hello").intValue());
		assertEquals(2, f.getValueCounts(a, col1).get("hi").intValue());
		assertEquals(5, f.getValueCounts(a, col2).get("world").intValue());
		
		f.reset(a);
		
		assertEquals(0, f.getRows(a).length);
		assertEquals(0, f.getValueCounts(a, col1).size());
	}
	
	public void testCountingAboveThreshold() throws Exception {
		InMemoryRowAnnotationFactory f = new InMemoryRowAnnotationFactory(5);
		RowAnnotation a = f.createAnnotation();
		
		f.annotate(new MockInputRow(), 1, a);
		f.annotate(new MockInputRow(), 1, a);
		f.annotate(new MockInputRow(), 1, a);
		f.annotate(new MockInputRow(), 1, a);
		
		assertEquals(4, a.getRowCount());
		
		f.annotate(new MockInputRow(), 1, a);
		
		assertEquals(5, a.getRowCount());
		
		f.annotate(new MockInputRow(), 1, a);
		
		assertEquals(6, a.getRowCount());
		
		f.annotate(new MockInputRow(), 1, a);
		
		assertEquals(7, a.getRowCount());
	}
}
