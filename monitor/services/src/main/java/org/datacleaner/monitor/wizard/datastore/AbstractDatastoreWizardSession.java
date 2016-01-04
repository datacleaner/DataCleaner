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
package org.datacleaner.monitor.wizard.datastore;

import javax.xml.parsers.DocumentBuilder;

import org.datacleaner.monitor.configuration.TenantContext;
import org.datacleaner.monitor.server.dao.DatastoreDao;
import org.datacleaner.monitor.server.dao.DatastoreDaoImpl;
import org.datacleaner.util.xml.XmlUtils;
import org.w3c.dom.Element;

/**
 * Represents the typical abstractly implemented session of creating a datastore
 */
public abstract class AbstractDatastoreWizardSession implements DatastoreWizardSession {

    private final DatastoreWizardContext _wizardContext;

    public AbstractDatastoreWizardSession(DatastoreWizardContext wizardContext) {
        _wizardContext = wizardContext;
    }

    @Override
    public final DatastoreWizardContext getWizardContext() {
        return _wizardContext;
    }

    @Override
    public Integer getPageCount() {
        return _wizardContext.getDatastoreWizard().getExpectedPageCount();
    }

    @Override
    public String finished() {
        final DocumentBuilder documentBuilder = XmlUtils.createDocumentBuilder();

        final TenantContext tenantContext = _wizardContext.getTenantContext();
        final Element datastoreNode = createDatastoreElement(documentBuilder);

        final DatastoreDao datastoreDao = getDatastoreDao();
        String datastoreName = datastoreDao.addDatastore(tenantContext, datastoreNode);
        
        
        return datastoreName;
    }

    protected DatastoreDao getDatastoreDao() {
        return new DatastoreDaoImpl();
    }

    /**
     * Creates the final datastore node (to be inserted into conf.xml) as
     * prescribed by the wizard. This method will be invoked when no more pages
     * are available and the wizard has ended.
     * 
     * @param documentBuilder
     * 
     * @return
     */
    protected abstract Element createDatastoreElement(DocumentBuilder documentBuilder);
}
