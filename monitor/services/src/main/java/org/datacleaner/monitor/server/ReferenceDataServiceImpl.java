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
package org.datacleaner.monitor.server;

import java.util.HashSet;
import java.util.Set;

import org.datacleaner.monitor.configuration.TenantContextFactory;
import org.datacleaner.monitor.referencedata.ReferenceDataItem;
import org.datacleaner.monitor.referencedata.ReferenceDataService;
import org.datacleaner.monitor.server.dao.ReferenceDataDao;
import org.datacleaner.monitor.server.dao.ReferenceDataDaoImpl;
import org.datacleaner.monitor.shared.model.TenantIdentifier;
import org.datacleaner.reference.ReferenceDataCatalog;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component("RefereceDataService")
public class ReferenceDataServiceImpl implements ReferenceDataService, ApplicationContextAware {
    @Autowired
    TenantContextFactory _contextFactory;
    private ApplicationContext _applicationContext;

    @Override
    public Set<ReferenceDataItem> getDictionaries(final TenantIdentifier tenant) {
        return namesToList(ReferenceDataItem.Type.DICTIONARY, getReferenceDataCatalog(tenant).getDictionaryNames());
    }

    @Override
    public Set<ReferenceDataItem> getSynonymCatalogs(final TenantIdentifier tenant) {
        return namesToList(ReferenceDataItem.Type.SYNONYM_CATALOG,
                getReferenceDataCatalog(tenant).getSynonymCatalogNames());
    }

    @Override
    public Set<ReferenceDataItem> getStringPatterns(final TenantIdentifier tenant) {
        return namesToList(ReferenceDataItem.Type.STRING_PATTERN,
                getReferenceDataCatalog(tenant).getStringPatternNames());
    }

    @Override
    public boolean removeItem(final TenantIdentifier tenant, final ReferenceDataItem.Type type, final String name) {
        final ReferenceDataDao dao = new ReferenceDataDaoImpl();

        if (type.equals(ReferenceDataItem.Type.DICTIONARY) &&
                getReferenceDataCatalog(tenant).containsDictionary(name)) {
            dao.removeDictionary(_contextFactory.getContext(tenant),
                    getReferenceDataCatalog(tenant).getDictionary(name));
            return true;
        }

        if (type.equals(ReferenceDataItem.Type.SYNONYM_CATALOG) &&
                getReferenceDataCatalog(tenant).containsSynonymCatalog(name)) {
            dao.removeSynonymCatalog(_contextFactory.getContext(tenant),
                    getReferenceDataCatalog(tenant).getSynonymCatalog(name));
            return true;
        }

        if (type.equals(ReferenceDataItem.Type.STRING_PATTERN) &&
                getReferenceDataCatalog(tenant).containsStringPattern(name)) {
            dao.removeStringPattern(_contextFactory.getContext(tenant),
                    getReferenceDataCatalog(tenant).getStringPattern(name));
            return true;
        }

        return false;
    }

    private ReferenceDataCatalog getReferenceDataCatalog(final TenantIdentifier tenant) {
        return _contextFactory.getContext(tenant).getConfiguration().getReferenceDataCatalog();
    }

    private Set<ReferenceDataItem> namesToList(final ReferenceDataItem.Type type, final String[] allNames) {
        final Set<ReferenceDataItem> set = new HashSet<>();

        for (final String name : allNames) {
            set.add(new ReferenceDataItem(type, name));
        }

        return set;
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        System.out.println("The application context has been initialized: " + applicationContext.getDisplayName());
        _applicationContext = applicationContext;
    }
}
