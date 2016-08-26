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
package org.datacleaner.monitor.server.wizard.shared.datastore;

import java.util.HashMap;
import java.util.Map;

import org.datacleaner.monitor.wizard.common.AbstractFreemarkerWizardPage;

public abstract class DatastorePage3 extends AbstractFreemarkerWizardPage {
    protected static final String PROPERTY_TABLE = "table";
    protected static final String PROPERTY_TABLE_OPTIONS = "tableOptions";

    protected final DatastoreWizardSession _session;

    public DatastorePage3(DatastoreWizardSession session) {
        _session = session;
    }

    @Override
    public Integer getPageIndex() {
        return 2;
    }

    @Override
    protected String getTemplateFilename() {
        _templateConfiguration.setClassForTemplateLoading(this.getClass(), DatastorePage1.TEMPLATE_PACKAGE);
        return "DatastorePage3.html";
    }

    @Override
    protected Map<String, Object> getFormModel() {
        final Map<String, Object> model = new HashMap<>();
        model.put(PROPERTY_TABLE, _session.getTable());
        model.put(PROPERTY_TABLE_OPTIONS, 
                DatastoreHelper.getTableOptions(_session.getWizardContext().getTenantContext(), 
                        _session.getDatastore(), _session.getSchema()));

        return model;
    }
}
