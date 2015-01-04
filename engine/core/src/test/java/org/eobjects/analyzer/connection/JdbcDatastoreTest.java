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
package org.eobjects.analyzer.connection;

import org.apache.commons.lang.SerializationUtils;

import junit.framework.TestCase;

public class JdbcDatastoreTest extends TestCase {

	public void testEquals() throws Exception {
		JdbcDatastore ds1 = new JdbcDatastore("hello", "url", "driver", "username", "pw", true);
		JdbcDatastore ds2;

		ds2 = new JdbcDatastore("hello", "url", "driver", "username", "pw", true);
		assertEquals(ds1, ds2);

		ds2 = new JdbcDatastore("hello1", "url", "driver", "username", "pw", true);
		assertFalse(ds1.equals(ds2));

		ds2 = new JdbcDatastore("hello", "url2", "driver", "username", "pw", true);
		assertFalse(ds1.equals(ds2));
	}

	public void testSerializationAndDeserialization() throws Exception {
		JdbcDatastore ds = new JdbcDatastore("name", "url", "driver", "username", "pw", true);

		Object clone = SerializationUtils.clone(ds);
		assertEquals(ds, clone);
	}

	public void testGetters() throws Exception {
		JdbcDatastore ds = new JdbcDatastore("name", "url", "driver", "username", "pw", true);

		assertEquals(null, ds.getDatasourceJndiUrl());
		assertEquals("name", ds.getName());
		assertEquals("url", ds.getJdbcUrl());
		assertEquals("driver", ds.getDriverClass());
		assertEquals("username", ds.getUsername());
		assertEquals("pw", ds.getPassword());
		assertEquals(true, ds.isMultipleConnections());

		assertEquals("JdbcDatastore[name=name,url=url]", ds.toString());

		ds = new JdbcDatastore("name1", "url1", "driver1", "username1", "pw1", false);

		assertEquals(null, ds.getDatasourceJndiUrl());
		assertEquals("name1", ds.getName());
		assertEquals("url1", ds.getJdbcUrl());
		assertEquals("driver1", ds.getDriverClass());
		assertEquals("username1", ds.getUsername());
		assertEquals("pw1", ds.getPassword());
		assertEquals(false, ds.isMultipleConnections());

		assertEquals("JdbcDatastore[name=name1,url=url1]", ds.toString());
		
		ds = new JdbcDatastore("name2","jndi2");

		assertEquals("jndi2", ds.getDatasourceJndiUrl());
		assertEquals("name2", ds.getName());
		assertEquals(null, ds.getJdbcUrl());
		assertEquals(null, ds.getDriverClass());
		assertEquals(null, ds.getUsername());
		assertEquals(null, ds.getPassword());
		assertEquals(false, ds.isMultipleConnections());

		assertEquals("JdbcDatastore[name=name2,jndi=jndi2]", ds.toString());
	}

	public void testToStringDataSource() throws Exception {
		JdbcDatastore ds = new JdbcDatastore("foo", "bar");

		assertEquals("JdbcDatastore[name=foo,jndi=bar]", ds.toString());
	}
}
