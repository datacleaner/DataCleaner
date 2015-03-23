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
package org.datacleaner.user;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.metamodel.util.CollectionUtils;
import org.apache.metamodel.util.Predicate;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.SalesforceDatastore;

/**
 * Contains information about the initial configuration with demo datastores,
 * jobs etc.
 */
public class DemoConfiguration {

    private static final List<String> ALL_FILES = new ArrayList<>();

    public static final String DATASTORE_FILE_CUSTOMERS = addFile("datastores/customers.csv");

    public static final String JOB_CUSTOMER_PROFILING = addFile("jobs/Customer profiling.analysis.xml");
    public static final String JOB_CUSTOMER_AGE_ANALYSIS = addFile("jobs/Customer age analysis.analysis.xml");
    public static final String JOB_CUSTOMER_JOB_TITLE_ANALYTICS = addFile("jobs/Job title analytics.analysis.xml");
    public static final String JOB_CUSTOMER_DEDUP_TRAINING = addFile("jobs/dedup_customers_training.analysis.xml");
    public static final String JOB_CUSTOMER_DEDUP_DETECTION = addFile("jobs/dedup_customers_detection.analysis.xml");
    public static final String JOB_SFDC_DUPLICATE_TRAINING = addFile("jobs/Salesforce dedup training.analysis.xml");
    public static final String JOB_EXPORT_ORDERS_DATA = addFile("jobs/Export of Orders data mart.analysis.xml");
    public static final String JOB_COPY_EMPLOYEES_TO_CUSTOMERS = addFile("jobs/Copy employees to customer table.analysis.xml");
    public static final String JOB_ORDERDB_DUPLICATE_DETECTION = addFile("jobs/OrderDB Customers Duplicate detection.analysis.xml");
    public static final String JOB_ORDERDB_DUPLICATE_TRAINING = addFile("jobs/OrderDB Customers dedup Training.analysis.xml");
    public static final String JOB_US_CUSTOMER_STATE_ANALYSIS = addFile("jobs/US Customer STATE check.analysis.xml");

    public static final String OTHER_DEDUP_MODEL_SFDC_USERS = addFile("jobs/sfdc_dupe_model_users.dedupmodel.xml");
    public static final String OTHER_DEDUP_MODEL_ORDERDB_CUSTOMERS = addFile("jobs/orderdb_customers_dupe_model.dedupmodel.xml");
    public static final String OTHER_DEDUP_REFERENCE_ORDERDB_CUSTOMERS = addFile("jobs/orderdb_customers_dupe_reference.txt");
    public static final String OTHER_DEDUP_MODEL_CUSTOMERS = addFile("jobs/dedup_customers.dedupmodel.xml");
    public static final String OTHER_RESULT_DEDUP_CUSTOMERS = addFile("jobs/dedup_customers.analysis.result.dat");
    public static final String OTHER_SYNONYM_CATALOG_JOB_TITLES = addFile("datastores/job_title_synonyms.txt");

    public static boolean isUnconfiguredDemoDatastore(Datastore ds) {
        if (ds instanceof SalesforceDatastore) {
            final SalesforceDatastore sfdcDatastore = (SalesforceDatastore) ds;
            if ("username".equals(sfdcDatastore.getUsername())) {
                return true;
            }
        }
        return false;
    }

    private static String addFile(String filePath) {
        ALL_FILES.add(filePath);
        return filePath;
    }

    public static List<String> getAllFilePaths() {
        return Collections.unmodifiableList(ALL_FILES);
    }

    public static List<String> getAllJobFilePaths() {
        return CollectionUtils.filter(ALL_FILES, new Predicate<String>() {
            @Override
            public Boolean eval(String path) {
                return path.startsWith("jobs/") && path.endsWith(".analysis.xml");
            }
        });
    }
}
