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

/**
 * Class ComponentsStoreTest
 *
 * @author k.houzvicka
 * @since 28.7.15
 */
public class ComponentsStoreTest {

//    @Test
//    public void testStore() throws Exception {
//        File tempFolder = Files.createTempDir();
//        tempFolder.deleteOnExit();
//
//        FileRepositoryFolder repo = new FileRepositoryFolder(null, tempFolder);
//        Repository repository = new FileRepository(repo.getFile());
//        ComponentsStore store = new ComponentsStore(repository);
//
//        ComponentsCacheConfigWrapper conf1 = createWrapper("id1");
//        store.storeConfiguration(conf1);
//        File componentFolder = new File(tempFolder, ComponentsStore.FOLDER_NAME);
//        Assert.assertTrue(componentFolder.exists());
//        File confFile = new File(componentFolder, "id1");
//        Assert.assertTrue(confFile.exists());
//
//        ComponentsCacheConfigWrapper conf2 = store.getConfiguration("id1");
//        Assert.assertEquals(conf1.componentConfigHolder.componentId, conf2.componentConfigHolder.componentId);
//        Assert.assertEquals(conf1.componentConfigHolder.timeoutMs, conf2.componentConfigHolder.timeoutMs);
//        Assert.assertEquals(conf1.expirationTime, conf2.expirationTime);
//        Assert.assertEquals(null, store.getConfiguration("id2"));
//
//    }
//
//    private ComponentsCacheConfigWrapper createWrapper(String componentId) {
//        ComponentConfiguration configuration = new ComponentConfiguration();
//        configuration.setComponentName("TestName");
//        configuration.setComponentType(ComponentConfiguration.ComponentType.ANALYZER);
//        configuration.setId(1);
//        configuration.setStatus(ComponentStatus.CREATED);
//        configuration.setPropertiesMap(new HashMap<String, String>());
//        configuration.getPropertiesMap().put("propertyA", "a");
//
//        ComponentDescriptor descriptor = EasyMock.createMock(ComponentDescriptor.class);
//        LifeCycleHelper lifeCycleHelper = null;
//        Component component = EasyMock.createMock(Component.class);
//
//        ComponentConfigHolder configHolder = new ComponentConfigHolder(10 * 1000, componentId, configuration, descriptor, lifeCycleHelper, component);
//        ComponentsCacheConfigWrapper wrapper = new ComponentsCacheConfigWrapper(configHolder);
//        return wrapper;
//    }
}
