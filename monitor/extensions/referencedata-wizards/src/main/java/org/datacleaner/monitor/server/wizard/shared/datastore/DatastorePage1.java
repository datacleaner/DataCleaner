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

public abstract class DatastorePage1 extends AbstractFreemarkerWizardPage {
    public static final String TEMPLATE_PACKAGE = "/org/datacleaner/monitor/server/wizard/shared/datastore/";

    protected static final String PROPERTY_NAME = "name";
    protected static final String PROPERTY_NAME_LABEL = "nameLabel";
    protected static final String PROPERTY_DATASTORE = "datastore";
    protected static final String PROPERTY_DATASTORE_OPTIONS = "datastoreOptions";

    protected final DatastoreWizardSession _session;

    public DatastorePage1(DatastoreWizardSession session) {
        _session = session;
    }

    @Override
    public Integer getPageIndex() {
        return 0;
    }

    @Override
    protected String getTemplateFilename() {
        _templateConfiguration.setClassForTemplateLoading(this.getClass(), TEMPLATE_PACKAGE);
        return "DatastorePage1.html";
    }

    @Override
    protected Map<String, Object> getFormModel() {
        final Map<String, Object> model = new HashMap<>();
        model.put(PROPERTY_NAME, _session.getName());
        model.put(PROPERTY_NAME_LABEL, getNameLabel());
        model.put(PROPERTY_DATASTORE, _session.getDatastore());
        model.put(PROPERTY_DATASTORE_OPTIONS,
                DatastoreHelper.getDatastoreOptions(_session.getWizardContext().getTenantContext()));

        return model;
    }

    protected abstract String getNameLabel();
}
