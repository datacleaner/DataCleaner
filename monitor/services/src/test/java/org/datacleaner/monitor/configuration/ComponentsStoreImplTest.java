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
package org.datacleaner.monitor.configuration;

import org.datacleaner.repository.Repository;
import org.datacleaner.repository.RepositoryFile;
import org.datacleaner.repository.RepositoryFolder;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.*;

public class ComponentsStoreImplTest {
    private ComponentsStoreImpl componentsStoreImpl = null;
    private String tenantId = "demo";
    private String componentId = "componentId";

    @Before
    public void setUp() {
        componentsStoreImpl = new ComponentsStoreImpl(getRepositoryMock(), tenantId);
    }

    private Repository getRepositoryMock() {
        Repository repository = createNiceMock(Repository.class);
        expect(repository.getFolder(tenantId)).andReturn(getRepositoryFolderMock()).anyTimes();
        replay(repository);

        return repository;
    }

    private RepositoryFolder getRepositoryFolderMock() {
        RepositoryFolder repositoryFolder = createNiceMock(RepositoryFolder.class);
        expect(repositoryFolder.getFolder(ComponentsStoreImpl.FOLDER_NAME)).andReturn(getComponentsFolderMock()).anyTimes();
        replay(repositoryFolder);

        return repositoryFolder;
    }

    private RepositoryFolder getComponentsFolderMock() {
        RepositoryFolder repositoryFolder = createNiceMock(RepositoryFolder.class);
        expect(repositoryFolder.getFile(componentId)).andReturn(getRepositoryFileMock()).anyTimes();
        replay(repositoryFolder);

        return repositoryFolder;
    }

    private RepositoryFile getRepositoryFileMock() {
        RepositoryFile repositoryFile = createNiceMock(RepositoryFile.class);
        replay(repositoryFile);

        return repositoryFile;
    }

    @Test
    public void testGetConfiguration() throws Exception {
        componentsStoreImpl.getConfiguration(componentId);
    }

    @Test
    public void testStoreConfiguration() throws Exception {
        componentsStoreImpl.storeConfiguration(getComponentsStoreHolderMock());
    }

    private ComponentsStoreHolder getComponentsStoreHolderMock() {
        ComponentsStoreHolder componentsStoreHolder = createNiceMock(ComponentsStoreHolder.class);
        expect(componentsStoreHolder.getComponentId()).andReturn(componentId).anyTimes();
        replay(componentsStoreHolder);

        return componentsStoreHolder;
    }

    @Test
    public void testRemoveConfiguration() throws Exception {
        componentsStoreImpl.removeConfiguration(componentId);
    }
}