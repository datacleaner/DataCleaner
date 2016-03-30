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
package org.datacleaner.spark.utils;

import org.apache.metamodel.csv.CsvConfiguration;
import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.HdfsResource;
import org.apache.metamodel.util.Resource;
import org.datacleaner.connection.CsvDatastore;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.JsonDatastore;
import org.datacleaner.util.StringUtils;

/**
 * Utility class to check the validity of a datastore when trying to run job on
 * Hadoop with Spark.
 *
 */
public class HadoopConfigurationUtils {

    public static boolean isValidConfiguration(Datastore datastore) {

        if (datastore instanceof CsvDatastore) {
            final CsvDatastore csvDatastore = (CsvDatastore) datastore;
            final CsvConfiguration csvConfiguration = csvDatastore.getCsvConfiguration();
            final String encoding = csvConfiguration.getEncoding();
            if (!encoding.equals(FileHelper.UTF_8_ENCODING)) {
                return false;
            }

            if (csvConfiguration.isMultilineValues()) {
                return false;
            }

            final Resource resource = csvDatastore.getResource();
            if (!isHdfsResource(resource)) {
                return false;
            }
        } else if (datastore instanceof JsonDatastore) {
            final JsonDatastore jsonDatastore = (JsonDatastore) datastore;
            final Resource resource = jsonDatastore.getResource();
            if (!isHdfsResource(resource)) {
                return false;
            }
        } else {
            // other type of datastore
            return false;
        }
        return true;
    }

    private static boolean isHdfsResource(Resource resource) {
        if (resource instanceof HdfsResource) {
            return true;
        }
        return false;
    }

    public static boolean isSparkHomeSet() {
        final String property = System.getProperty("SPARK_HOME");
        if (StringUtils.isNullOrEmpty(property)) {
            return false;
        }
        return true;
    }
}
