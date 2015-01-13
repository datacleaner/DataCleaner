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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.metamodel.pojo.MapTableDataProvider;
import org.apache.metamodel.pojo.TableDataProvider;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.util.SimpleTableDef;

public class PojoDatastoreTest extends TestCase {

    public void testSimpleInteractions() throws Exception {
        final SimpleTableDef tableDef = new SimpleTableDef("foo", new String[] { "col1", "col2" });
        final Collection<Map<String, ?>> maps = new ArrayList<Map<String, ?>>();

        final TableDataProvider<?> tableDataProvider = new MapTableDataProvider(tableDef, maps);
        maps.add(new HashMap<String, Object>());

        final List<TableDataProvider<?>> tableDataProviders = new ArrayList<TableDataProvider<?>>();
        tableDataProviders.add(tableDataProvider);

        final PojoDatastore ds = new PojoDatastore("foobar", tableDataProviders);

        assertEquals("foobar", ds.getName());

        assertFalse(ds.getPerformanceCharacteristics().isQueryOptimizationPreferred());

        ds.setDescription("foo");
        assertEquals("foo", ds.getDescription());

        final UpdateableDatastoreConnection con = ds.openConnection();
        assertNotNull(con);

        final Column col = con.getSchemaNavigator().convertToColumn("foo.col1");
        assertEquals("Column[name=col1,columnNumber=0,type=VARCHAR,nullable=true,"
                + "nativeType=null,columnSize=null]", col.toString());
    }
}
