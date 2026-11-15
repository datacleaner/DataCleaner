/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Free Software Foundation, Inc.
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
package org.datacleaner.connection;

import java.io.File;
import org.datacleaner.metamodel.query.Query;
import java.sql.Statement;

import org.datacleaner.metamodel.UpdateScript;
import org.datacleaner.metamodel.schema.Schema;
import org.datacleaner.metamodel.schema.Table;

import junit.framework.TestCase;

public class DuckDbDatastoreTest extends TestCase {

    public void testConnectAndExploreAndUpdate() throws Exception {
        final File dbFile = new File("target/test_duckdb_datastore.db");
        dbFile.delete();

        final String url = "jdbc:duckdb:" + dbFile.getAbsolutePath();
        final DuckDbDatastore ds = new DuckDbDatastore("my duckdb", url);

        try (UpdateableDatastoreConnection con = ds.openConnection()) {
            final java.sql.Connection jdbcConnection =
                    ((org.datacleaner.metamodel.jdbc.JdbcDataContext) con.getUpdateableDataContext()).getConnection();
            try (Statement stmt = jdbcConnection.createStatement()) {
                stmt.execute("CREATE TABLE foo (id INTEGER, name VARCHAR)");
                stmt.execute("INSERT INTO foo VALUES (1, 'hello'), (2, 'world')");
            }
            jdbcConnection.commit();
        }

        try (DatastoreConnection con = ds.openConnection()) {
            final Schema schema = con.getSchemaNavigator().getDefaultSchema();
            assertEquals("main", schema.getName());
            assertEquals("[foo]", schema.getTableNames().toString());

            final Table table = schema.getTableByName("foo");
            assertEquals("[id, name]", table.getColumnNames().toString());

            final Query q = con.getDataContext().query().from(table).selectCount().toQuery();
            final Number count = (Number) con.getDataContext().executeQuery(q).toObjectArrays().get(0)[0];
            assertEquals(2, count.intValue());
        }
    }
}
