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

import com.google.common.io.Files;
import org.datacleaner.repository.Repository;
import org.datacleaner.repository.file.FileRepository;
import org.datacleaner.repository.file.FileRepositoryFolder;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

/**
 * Class ComponentsStoreTest
 * 
 * @since 28.7.15
 */
public class ComponentStoreTest {
    private final String tenantId = "tenant";
    private final String componentName = "componentName";
    private final String instanceId1 = "id1";
    private final String instanceId2 = "id2";

    @Test
    public void testStore() throws Exception {
        File tempFolder = Files.createTempDir();
        tempFolder.deleteOnExit();

        FileRepositoryFolder repo = new FileRepositoryFolder(null, tempFolder);
        Repository repository = new FileRepository(repo.getFile());
        repository.createFolder(tenantId);
        ComponentStore store = new ComponentStoreImpl(repository, tenantId);

        ComponentStoreHolder conf1 = createHolder(instanceId1);
        store.store(conf1);

        File tenantDir = new File(tempFolder, tenantId);
        File componentFolder = new File(tenantDir, ComponentStoreImpl.FOLDER_NAME);
        Assert.assertTrue(componentFolder.exists());
        File confFile = new File(componentFolder, instanceId1);
        Assert.assertTrue(confFile.exists());

        ComponentStoreHolder conf2 = store.get(instanceId1);
        Assert.assertEquals(conf1.getInstanceId(), conf2.getInstanceId());
        Assert.assertEquals(conf1.getTimeout(), conf2.getTimeout());
        Assert.assertEquals(null, store.get(instanceId2));
        store.remove(instanceId1);
        Assert.assertEquals(null, store.get(instanceId1));
    }

    private ComponentStoreHolder createHolder(String instanceId) {
        return new ComponentStoreHolder(10l, null, instanceId, componentName);
    }
}
