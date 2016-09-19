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
package org.datacleaner.monitor.wizard.referencedata;

import javax.xml.parsers.DocumentBuilder;

import org.apache.metamodel.util.Resource;
import org.datacleaner.configuration.DomConfigurationWriter;
import org.datacleaner.monitor.configuration.TenantContext;
import org.datacleaner.monitor.server.dao.ReferenceDataDao;
import org.datacleaner.monitor.server.dao.ReferenceDataDaoImpl;
import org.datacleaner.util.xml.XmlUtils;
import org.w3c.dom.Element;

/**
 * Represents the typical abstractly implemented session of creating reference data. 
 */
public abstract class AbstractReferenceDataWizardSession implements ReferenceDataWizardSession {

    private final ReferenceDataWizardContext _wizardContext;
    protected final DomConfigurationWriter _writer;

    public AbstractReferenceDataWizardSession(ReferenceDataWizardContext wizardContext) {
        _wizardContext = wizardContext;
        final Resource resource = getWizardContext().getTenantContext().getConfigurationFile().toResource();
        _writer = new DomConfigurationWriter(resource);
    }

    @Override
    public final ReferenceDataWizardContext getWizardContext() {
        return _wizardContext;
    }

    @Override
    public Integer getPageCount() {
        return _wizardContext.getReferenceDataWizard().getExpectedPageCount();
    }

    @Override
    public String finished() {
        final DocumentBuilder documentBuilder = XmlUtils.createDocumentBuilder();
        final TenantContext tenantContext = _wizardContext.getTenantContext();
        final Element updatedReferenceDataSubSection = getUpdatedReferenceDataSubSection(documentBuilder);
        final ReferenceDataDao referenceDataDao = getReferenceDataDao();
        
        return referenceDataDao.updateReferenceDataSubSection(tenantContext, updatedReferenceDataSubSection);
    }

    protected ReferenceDataDao getReferenceDataDao() {
        return new ReferenceDataDaoImpl();
    }

    /**
     * Returns updated reference data sub-section (dictionaries, synonym-catalogs, string-patterns).
     * @param documentBuilder
     * 
     * @return
     */
    protected abstract Element getUpdatedReferenceDataSubSection(DocumentBuilder documentBuilder);
}
