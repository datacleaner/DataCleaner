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
package org.eobjects.analyzer.storage;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MockInputRow;

public class SqlDatabaseStorageProviderTest extends TestCase {

	private StorageProvider h2sp = new H2StorageProvider();
	private StorageProvider hsqlsp = new HsqldbStorageProvider();

	public void testCreateList() throws Exception {
		testCreateList(h2sp);
		testCreateList(hsqlsp);
	}

	private void testCreateList(StorageProvider sp) throws Exception {
		List<String> list = sp.createList(String.class);
		assertEquals(0, list.size());
		assertTrue(list.isEmpty());

		list.add("hello");
		list.add("world");
		assertEquals(2, list.size());

		assertEquals("world", list.get(1));

		assertEquals("[hello, world]", Arrays.toString(list.toArray()));

		list.remove(1);

		assertEquals("[hello]", Arrays.toString(list.toArray()));

		list.remove("foobar");
		list.remove("hello");

		assertEquals("[]", Arrays.toString(list.toArray()));
	}

	public void testCreateMap() throws Exception {
		testCreateMap(h2sp);
		testCreateMap(hsqlsp);
	}

	private void testCreateMap(StorageProvider sp) throws Exception {
		Map<Integer, String> map = sp.createMap(Integer.class, String.class);

		map.put(1, "hello");
		map.put(2, "world");
		map.put(5, "foo");

		assertEquals("world", map.get(2));
		assertNull(map.get(3));

		assertEquals(3, map.size());

		// override 5
		map.put(5, "bar");

		assertEquals(3, map.size());

		Iterator<Entry<Integer, String>> it = map.entrySet().iterator();
		Entry<Integer, String> next;
		assertTrue(it.hasNext());
		next = it.next();
		assertEquals(1, next.getKey().intValue());
		assertEquals("hello", next.getValue());
		assertTrue(it.hasNext());
		next = it.next();
		assertEquals(2, next.getKey().intValue());
		assertEquals("world", next.getValue());
		next.setValue("universe");
		assertEquals(next.getKey().hashCode(), next.hashCode());
		assertTrue(it.hasNext());
		next = it.next();
		assertEquals(5, next.getKey().intValue());
		assertEquals("bar", next.getValue());

		assertFalse(it.hasNext());

		assertEquals("universe", map.get(2));
		
		map.remove(2);
		assertNull(map.get(2));
		assertFalse(map.containsKey(2));
	}

	public void testCreateSet() throws Exception {
		testCreateSet(h2sp);
		testCreateSet(hsqlsp);
	}

	private void testCreateSet(StorageProvider sp) throws Exception {
		Set<Long> set = sp.createSet(Long.class);

		assertTrue(set.isEmpty());
		assertEquals(0, set.size());

		set.add(1l);

		assertEquals(1, set.size());

		set.add(2l);
		set.add(3l);

		assertEquals(3, set.size());

		set.add(3l);

		assertEquals(3, set.size());

		Iterator<Long> it = set.iterator();
		assertTrue(it.hasNext());
		assertEquals(Long.valueOf(1l), it.next());

		assertTrue(it.hasNext());
		assertEquals(Long.valueOf(2l), it.next());

		assertTrue(it.hasNext());
		assertEquals(Long.valueOf(3l), it.next());

		assertFalse(it.hasNext());

		assertFalse(it.hasNext());

		it = set.iterator();

		assertTrue(it.hasNext());
		assertEquals(Long.valueOf(1l), it.next());

		// remove 1
		it.remove();

		assertTrue(it.hasNext());
		assertEquals(Long.valueOf(2l), it.next());

		assertTrue(it.hasNext());
		assertEquals(Long.valueOf(3l), it.next());

		assertFalse(it.hasNext());

		assertEquals("[2, 3]", Arrays.toString(set.toArray()));
	}

	public void testFinalize() throws Exception {
		testFinalize(h2sp);
		testFinalize(hsqlsp);
	}

	private void testFinalize(StorageProvider sp) throws Exception {
		Connection connectionMock = EasyMock.createMock(Connection.class);
		Statement statementMock = EasyMock.createMock(Statement.class);

		EasyMock.expect(connectionMock.createStatement()).andReturn(statementMock);
		EasyMock.expect(statementMock.executeUpdate("CREATE CACHED TABLE MY_TABLE (set_value VARCHAR PRIMARY KEY)"))
				.andReturn(0);
		EasyMock.expect(statementMock.isClosed()).andReturn(false);
		statementMock.close();

		EasyMock.expect(connectionMock.createStatement()).andReturn(statementMock);
		EasyMock.expect(statementMock.executeUpdate("DROP TABLE MY_TABLE")).andReturn(0);
		EasyMock.expect(statementMock.isClosed()).andReturn(false);
		statementMock.close();

		EasyMock.replay(statementMock, connectionMock);

		SqlDatabaseSet<String> set = new SqlDatabaseSet<String>(connectionMock, "MY_TABLE", "VARCHAR");
		assertEquals(0, set.size());
		set = null;

		Thread.sleep(500);
		System.gc();
		Thread.sleep(500);
		System.runFinalization();

		EasyMock.verify(statementMock, connectionMock);
	}

	public void testCreateRowAnnotationFactory() throws Exception {
		testCreateRowAnnotationFactory(h2sp);
		testCreateRowAnnotationFactory(hsqlsp);
	}

	private void testCreateRowAnnotationFactory(StorageProvider sp) throws Exception {
		RowAnnotationFactory f = sp.createRowAnnotationFactory();

		RowAnnotation a1 = f.createAnnotation();
		RowAnnotation a2 = f.createAnnotation();

		InputColumn<String> col1 = new MockInputColumn<String>("foo", String.class);
		InputColumn<Integer> col2 = new MockInputColumn<Integer>("bar", Integer.class);
		InputColumn<Boolean> col3 = new MockInputColumn<Boolean>("w00p", Boolean.class);

		MockInputRow row1 = new MockInputRow(1).put(col1, "1").put(col2, 1).put(col3, true);
		MockInputRow row2 = new MockInputRow(2).put(col1, "2");
		MockInputRow row3 = new MockInputRow(3).put(col1, "3").put(col2, 3).put(col3, true);
		MockInputRow row4 = new MockInputRow(4).put(col1, "4").put(col2, 4).put(col3, false);

		InputRow[] rows = f.getRows(a1);
		assertEquals(0, rows.length);

		f.annotate(row1, 3, a1);
		assertEquals(3, a1.getRowCount());

		rows = f.getRows(a1);
		assertEquals(1, rows.length);
		assertEquals("1", rows[0].getValue(col1));
		assertEquals(Integer.valueOf(1), rows[0].getValue(col2));
		assertEquals(Boolean.TRUE, rows[0].getValue(col3));

		// repeat the same annotate call - should do nothing
		f.annotate(row1, 3, a1);
		assertEquals(3, a1.getRowCount());

		assertEquals(1, rows.length);
		assertEquals("1", rows[0].getValue(col1));

		f.annotate(row2, 2, a1);
		f.annotate(row2, 2, a1);
		f.annotate(row2, 2, a1);
		f.annotate(row2, 2, a1);
		assertEquals(5, a1.getRowCount());

		rows = f.getRows(a1);
		assertEquals(2, rows.length);
		assertEquals("1", rows[0].getValue(col1));
		assertEquals(Integer.valueOf(1), rows[0].getValue(col2));
		assertEquals(Boolean.TRUE, rows[0].getValue(col3));
		assertEquals("2", rows[1].getValue(col1));
		assertEquals(null, rows[1].getValue(col2));
		assertEquals(null, rows[1].getValue(col3));

		assertEquals(0, a2.getRowCount());

		f.annotate(row1, 3, a2);

		assertEquals(5, a1.getRowCount());
		assertEquals(3, a2.getRowCount());

		f.annotate(row3, 6, a2);
		f.annotate(row4, 7, a2);

		assertEquals(5, a1.getRowCount());
		assertEquals(16, a2.getRowCount());

		rows = f.getRows(a1);
		assertEquals(2, rows.length);

		rows = f.getRows(a2);
		assertEquals(3, rows.length);
	}
}
