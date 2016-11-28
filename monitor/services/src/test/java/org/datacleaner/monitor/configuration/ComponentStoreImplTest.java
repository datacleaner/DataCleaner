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

import static org.easymock.EasyMock.*;

import org.datacleaner.repository.Repository;
import org.datacleaner.repository.RepositoryFile;
import org.datacleaner.repository.RepositoryFolder;
import org.junit.Before;
import org.junit.Test;

public class ComponentStoreImplTest {
    private ComponentStoreImpl componentsStoreImpl = null;
    private String tenantId = "demo";
    private String instanceId = "instanceId";

    @Before
    public void setUp() {
        componentsStoreImpl = new ComponentStoreImpl(getRepositoryMock(), tenantId);
    }

    private Repository getRepositoryMock() {
        final Repository repository = createNiceMock(Repository.class);
        expect(repository.getFolder(tenantId)).andReturn(getRepositoryFolderMock()).anyTimes();
        replay(repository);

        return repository;
    }

    private RepositoryFolder getRepositoryFolderMock() {
        final RepositoryFolder repositoryFolder = createNiceMock(RepositoryFolder.class);
        expect(repositoryFolder.getOrCreateFolder(ComponentStoreImpl.FOLDER_NAME)).andReturn(getComponentsFolderMock())
                .anyTimes();
        replay(repositoryFolder);

        return repositoryFolder;
    }

    private RepositoryFolder getComponentsFolderMock() {
        final RepositoryFolder repositoryFolder = createNiceMock(RepositoryFolder.class);
        expect(repositoryFolder.getFile(instanceId)).andReturn(getRepositoryFileMock()).anyTimes();
        replay(repositoryFolder);

        return repositoryFolder;
    }

    private RepositoryFile getRepositoryFileMock() {
        final RepositoryFile repositoryFile = createNiceMock(RepositoryFile.class);
        replay(repositoryFile);

        return repositoryFile;
    }

    @Test
    public void testGetConfiguration() throws Exception {
        componentsStoreImpl.get(instanceId);
    }

    @Test
    public void testStoreConfiguration() throws Exception {
        componentsStoreImpl.store(getComponentsStoreHolderMock());
    }

    private ComponentStoreHolder getComponentsStoreHolderMock() {
        final ComponentStoreHolder componentStoreHolder = createNiceMock(ComponentStoreHolder.class);
        expect(componentStoreHolder.getInstanceId()).andReturn(instanceId).anyTimes();
        replay(componentStoreHolder);

        return componentStoreHolder;
    }

    @Test
    public void testRemoveConfiguration() throws Exception {
        componentsStoreImpl.remove(instanceId);
    }
}
