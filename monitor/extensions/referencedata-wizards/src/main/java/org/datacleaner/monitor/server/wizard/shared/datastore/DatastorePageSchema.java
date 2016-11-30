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

public abstract class DatastorePageSchema extends AbstractFreemarkerWizardPage {
    protected static final String PROPERTY_SCHEMA = "schema";
    protected static final String PROPERTY_SCHEMA_OPTIONS = "schemaOptions";

    protected final DatastoreWizardSession _session;

    public DatastorePageSchema(final DatastoreWizardSession session) {
        _session = session;
    }

    @Override
    public Integer getPageIndex() {
        return 1;
    }

    @Override
    protected String getTemplateFilename() {
        _templateConfiguration.setClassForTemplateLoading(this.getClass(), DatastorePageDatastore.TEMPLATE_PACKAGE);
        return "DatastorePageSchema.html";
    }

    @Override
    protected Map<String, Object> getFormModel() {
        final Map<String, Object> model = new HashMap<>();
        model.put(PROPERTY_SCHEMA, _session.getSchema());
        model.put(PROPERTY_SCHEMA_OPTIONS, DatastoreHelper
                .getSchemaOptions(_session.getWizardContext().getTenantContext(), _session.getDatastore()));

        return model;
    }
}
