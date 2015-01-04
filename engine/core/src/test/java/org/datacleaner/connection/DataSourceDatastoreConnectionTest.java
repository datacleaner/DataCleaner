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
package org.datacleaner.connection;

import java.sql.Connection;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.sql.DataSource;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.datacleaner.test.TestHelper;
import org.datacleaner.util.SchemaNavigator;
import org.apache.metamodel.schema.Table;
import org.springframework.mock.jndi.SimpleNamingContext;

public class DataSourceDatastoreConnectionTest extends TestCase {

    public void testConstruction() throws Exception {
        DataSource dataSource = EasyMock.createMock(DataSource.class);

        EasyMock.expect(dataSource.getConnection()).andAnswer(new IAnswer<Connection>() {
            @Override
            public Connection answer() throws Throwable {
                return TestHelper.createSampleDatabaseDataSource().getConnection();
            }
        }).times(4);

        EasyMock.replay(dataSource);

        final SimpleNamingContext context = new SimpleNamingContext();
        context.bind("jdbc/mydatasource", dataSource);

        JdbcDatastore datastore = new JdbcDatastore("mydatasource", "jdbc/mydatasource") {
            private static final long serialVersionUID = 1L;

            @Override
            protected Context getJndiNamingContext() throws NamingException {
                return context;
            }
        };

        assertEquals("jdbc/mydatasource", datastore.getDatasourceJndiUrl());

        DatastoreConnection con = datastore.openConnection();

        assertEquals("mydatasource", con.getDatastore().getName());
        SchemaNavigator schemaNavigator = con.getSchemaNavigator();
        assertNotNull(schemaNavigator);
        assertEquals("PUBLIC", con.getDataContext().getDefaultSchema().getName());
        Table table = schemaNavigator.convertToTable("PUBLIC.EMPLOYEES");
        assertNotNull(table);
        assertEquals("EMPLOYEES", table.getName());

        EasyMock.verify(dataSource);
    }
}
