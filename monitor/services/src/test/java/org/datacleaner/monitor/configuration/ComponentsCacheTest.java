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
 * Class ComponentsCacheTest
 *
 * @author k.houzvicka
 * @since 28.7.15
 */
public class ComponentsCacheTest {

//    @Test
//    public void testCache() throws Exception {
//        File tempFolder = Files.createTempDir();
//        tempFolder.deleteOnExit();
//
//        FileRepositoryFolder repo = new FileRepositoryFolder(null, tempFolder);
//        Repository repository = new FileRepository(repo.getFile());
//
//        ComponentsCache cache = new ComponentsCache(repository);
//        ComponentConfigHolder conf1 = createConfigHolder("C1");
//        ComponentConfigHolder conf2 = createConfigHolder("C2");
//        cache.putComponent(conf1);
//        cache.putComponent(conf2);
//        Assert.assertEquals(conf1, cache.getConfigHolder(conf1.componentId));
//        Assert.assertEquals(conf2, cache.getConfigHolder(conf2.componentId));
//        cache.close();
//    }
//
//    private ComponentConfigHolder createConfigHolder(String componentId) {
//        ComponentConfiguration configuration = new ComponentConfiguration();
//        configuration.setComponentName("TestName");
//        configuration.setComponentType(ComponentConfiguration.ComponentType.ANALYZER);
//        configuration.setId(1);
//        configuration.setStatus(ComponentStatus.CREATED);
//        configuration.setPropertiesMap(new HashMap<String, String>());
//        configuration.getPropertiesMap().put("propertyA", "a");
//        ComponentDescriptor descriptor = EasyMock.createMock(ComponentDescriptor.class);
//        LifeCycleHelper lifeCycleHelper = null;
//        Component component = EasyMock.createMock(Component.class);
//        return new ComponentConfigHolder(1000, componentId, configuration, descriptor, lifeCycleHelper, component);
//    }
}
