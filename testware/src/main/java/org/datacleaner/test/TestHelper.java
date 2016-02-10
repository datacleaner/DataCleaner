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
package org.datacleaner.test;

import java.io.File;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.metamodel.util.FileHelper;
import org.datacleaner.connection.Datastore;
import org.junit.Assert;

public class TestHelper {

    public static DataSource createSampleDatabaseDataSource() {
        BasicDataSource _dataSource = new BasicDataSource();
        _dataSource.setDriverClassName("org.hsqldb.jdbcDriver");
        _dataSource.setUrl("jdbc:hsqldb:res:orderdb;readonly=true");
        _dataSource.setMaxActive(-1);
        _dataSource.setDefaultAutoCommit(false);
        return _dataSource;
    }

    public static Datastore createSampleDatabaseDatastore(String name) {
        return new TestDatastore(name, createSampleDatabaseDataSource());
    }

    public static void assertXmlFilesEquals(File benchmarkFile, File outputFile) {
        final String output = FileHelper.readFileAsString(outputFile);

        if (!benchmarkFile.exists()) {
            Assert.assertEquals("No benchmark file '" + benchmarkFile + "' exists!", output);
        }

        final String benchmark = FileHelper.readFileAsString(benchmarkFile);
        Assert.assertEquals(trimXmlForComparison(benchmark), trimXmlForComparison(output));
    }

    public static String trimXmlForComparison(String xml) {
        if (xml == null) {
            return null;
        }
        return xml.trim().replace("\r\n", "\n");
    }
}
