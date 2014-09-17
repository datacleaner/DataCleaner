/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.user;

import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.SalesforceDatastore;

/**
 * Contains information about the initial configuration with demo datastores,
 * jobs etc.
 */
public class DemoConfiguration {

    public static final String DATASTORE_FILE_CONTACTDATA = "datastores/contactdata.txt";

    public static final String JOB_CUSTOMER_PROFILING = "jobs/Customer profiling.analysis.xml";
    public static final String JOB_SFDC_DUPLICATE_DETECTION = "jobs/Salesforce duplicate detection.analysis.xml";
    public static final String JOB_SFDC_DUPLICATE_TRAINING = "jobs/Salesforce dedup training.analysis.xml";
    public static final String JOB_ADDRESS_CLEANSING = "jobs/Address cleansing with EasyDQ.analysis.xml";
    public static final String JOB_PHONE_CLEANSING = "jobs/Phone number analysis with EasyDQ.analysis.xml";
    public static final String JOB_EXPORT_ORDERS_DATA = "jobs/Export of Orders data mart.analysis.xml";
    public static final String JOB_COPY_EMPLOYEES_TO_CUSTOMERS = "jobs/Copy employees to customer table.analysis.xml";
    public static final String JOB_ORDERDB_DUPLICATE_DETECTION = "jobs/OrderDB Customers Duplicate detection.analysis.xml";
    public static final String JOB_ORDERDB_DUPLICATE_TRAINING = "jobs/OrderDB Customers dedup Training.analysis.xml";

    public static final String OTHER_DEDUP_MODEL_SFDC_USERS = "jobs/sfdc_dupe_model_users.dedupmodel.xml";
    public static final String OTHER_DEDUP_MODEL_ORDERDB_CUSTOMERS = "jobs/orderdb_customers_dupe_model.dedupmodel.xml";
    public static final String OTHER_DEDUP_REFERENCE_ORDERDB_CUSTOMERS = "jobs/orderdb_customers_dupe_reference.txt";
    
    public static boolean isUnconfiguredDemoDatastore(Datastore ds) {
        if (ds instanceof SalesforceDatastore) {
            final SalesforceDatastore sfdcDatastore = (SalesforceDatastore) ds;
            if ("username".equals(sfdcDatastore.getUsername())) {
                return true;
            }
        }
        return false;
    }
}
