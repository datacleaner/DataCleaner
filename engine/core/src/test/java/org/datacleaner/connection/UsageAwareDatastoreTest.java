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
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.SerializationUtils;

import junit.framework.TestCase;

public class UsageAwareDatastoreTest extends TestCase {

    public void testSerializationAndDeserializationOfAllDatastoreTypes() throws Exception {
        final List<UsageAwareDatastore<?>> datastores = new ArrayList<>();
        datastores.add(new AccessDatastore("access", "bar.mdb"));
        datastores.add(new CsvDatastore("csv", "bar.csv"));
        datastores.add(new DbaseDatastore("dbase", "bar.dbf"));
        datastores.add(new ExcelDatastore("excel", null, "bar.xls"));
        datastores.add(new JdbcDatastore("jdbc", "url"));
        datastores.add(new FixedWidthDatastore("fixedwidth", "foo.txt", "UTF8", new int[] { 1, 2, 3 }));
        datastores.add(new CompositeDatastore("foo",
                Arrays.asList(datastores.get(0), datastores.get(1), datastores.get(3))));

        for (final UsageAwareDatastore<?> ds : datastores) {
            System.out.println("Cloning datastore: " + ds);
            final Object clone = SerializationUtils.clone(ds);
            assertEquals(ds, clone);
        }
    }
}
