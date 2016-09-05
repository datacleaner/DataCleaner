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
package org.datacleaner.monitor.server.wizard.shared;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.metamodel.util.Func;
import org.apache.metamodel.util.Resource;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.monitor.configuration.TenantContext;
import org.datacleaner.monitor.configuration.TenantContextFactory;
import org.datacleaner.monitor.configuration.TenantContextFactoryImpl;
import org.datacleaner.monitor.job.JobEngineManager;
import org.datacleaner.monitor.server.job.SimpleJobEngineManager;
import org.datacleaner.monitor.wizard.referencedata.ReferenceDataWizardContext;
import org.datacleaner.reference.ReferenceDataCatalog;
import org.datacleaner.repository.Repository;
import org.datacleaner.repository.RepositoryFile;
import org.datacleaner.repository.file.FileRepository;
import org.easymock.EasyMock;

public class TestHelper {
    public static TenantContext getTenantContext() {
        Repository repository = new FileRepository("src/test/resources/example_repo");
        JobEngineManager jobEngineManager = new SimpleJobEngineManager();
        TenantContextFactory tenantContextFactory = new TenantContextFactoryImpl(repository,
                new DataCleanerEnvironmentImpl(), jobEngineManager);
        return tenantContextFactory.getContext("demo"); 
    }
    
    public static Map<String, List<String>> getFormParameters() {
        final Map<String, List<String>> formParameters = new HashMap<>();
        formParameters.put("name", getListWithValue("name-value"));
        formParameters.put("datastore", getListWithValue("datastore-value"));
        
        return formParameters;
    }
    
    private static List<String> getListWithValue(String value) {
        List<String> list = new ArrayList<>();
        list.add(value);
 
        return list;
    }

    public static ReferenceDataWizardContext getReferenceDataWizardContextMock() {
        final ReferenceDataWizardContext contextMock = EasyMock.createMock(ReferenceDataWizardContext.class);
        EasyMock.expect(contextMock.getTenantContext()).andReturn(getTenantContextMock()).anyTimes();
        EasyMock.replay(contextMock);
        
        return contextMock;
    }
    
    private static TenantContext getTenantContextMock() {
        final TenantContext tenantContextMock = EasyMock.createMock(TenantContext.class);
        EasyMock.expect(tenantContextMock.getConfigurationFile()).andReturn(getConfigurationFileMock());
        EasyMock.expect(tenantContextMock.getConfiguration()).andReturn(getConfigurationMock());
        EasyMock.replay(tenantContextMock); 
        
        return tenantContextMock;
    }
    
    private static RepositoryFile getConfigurationFileMock() {
        final RepositoryFile repositoryFileMock = EasyMock.createMock(RepositoryFile.class);
        EasyMock.expect(repositoryFileMock.toResource()).andReturn(getResourceMock());
        EasyMock.replay(repositoryFileMock);

        return repositoryFileMock; 
    }

    private static DataCleanerConfiguration getConfigurationMock() {
        final DataCleanerConfiguration configurationMock = EasyMock.createMock(DataCleanerConfiguration.class);
        EasyMock.expect(configurationMock.getReferenceDataCatalog()).andReturn(getReferenceDataCatalogMock());
        EasyMock.replay(configurationMock);

        return configurationMock;
    }
    
    private static ReferenceDataCatalog getReferenceDataCatalogMock() {
        final ReferenceDataCatalog referenceDataCatalogMock = EasyMock.createMock(ReferenceDataCatalog.class);

        return referenceDataCatalogMock;
    }
    
    private static Resource getResourceMock() {
        final Resource resourceMock = EasyMock.createMock(Resource.class);
        EasyMock.expect(resourceMock.read(EasyMock.anyObject(Func.class))).andReturn(null);
        EasyMock.replay(resourceMock);

        return resourceMock;
    }
}