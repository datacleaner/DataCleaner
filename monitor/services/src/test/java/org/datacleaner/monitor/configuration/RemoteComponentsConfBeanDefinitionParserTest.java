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

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Class RemoteComponentsConfBeanDefinitionParserTest
 * 
 * @since 23.9.15
 */
public class RemoteComponentsConfBeanDefinitionParserTest {

    @Test
    public void testDoParseOneDefault() throws Exception {
        ConstructorObject constructorObj = readXmlFile("remote_config/remoteConfigContextTest1.xml");
        Assert.assertEquals(0, constructorObj.includeSet.size());
        Assert.assertEquals(0, constructorObj.excludeSet.size());
        Assert.assertEquals(1, constructorObj.properties.size());
        Assert.assertEquals(2, constructorObj.properties.get("Concatenator").size());
        Assert.assertEquals("Separator", constructorObj.properties.get("Concatenator").get(0).getName());
        Assert.assertEquals("XXX", constructorObj.properties.get("Concatenator").get(0).getValue());
        Assert.assertEquals("Separator2", constructorObj.properties.get("Concatenator").get(1).getName());
        Assert.assertEquals("<a xmlns=\"http://www.datacleaner.org/schema/remoteComponentsConfiguration\">A</a>",
                constructorObj.properties.get("Concatenator").get(1).getValue());
    }

    @Test
    public void testDoParseEmpty() throws Exception {
        ConstructorObject constructorObj = readXmlFile("remote_config/remoteConfigContextTest2.xml");
        Assert.assertEquals(0, constructorObj.includeSet.size());
        Assert.assertEquals(0, constructorObj.excludeSet.size());
        Assert.assertEquals(0, constructorObj.properties.size());
    }

    @Test
    public void testDoParseIncludes() throws Exception {
        ConstructorObject constructorObj = readXmlFile("remote_config/remoteConfigContextTest3.xml");
        Assert.assertEquals(2, constructorObj.includeSet.size());
        Assert.assertEquals("[Component1, Component2]", constructorObj.includeSet.toString());
        Assert.assertEquals(0, constructorObj.excludeSet.size());
        Assert.assertEquals(0, constructorObj.properties.size());
    }

    @Test
    public void testDoParseExcludes() throws Exception {
        ConstructorObject constructorObj = readXmlFile("remote_config/remoteConfigContextTest4.xml");
        Assert.assertEquals(0, constructorObj.includeSet.size());
        Assert.assertEquals(2, constructorObj.excludeSet.size());
        Assert.assertEquals("[Component1, Component2]", constructorObj.excludeSet.toString());
        Assert.assertEquals(0, constructorObj.properties.size());
    }

    private ConstructorObject readXmlFile(String resource) throws ParserConfigurationException, IOException,
            SAXException {
        BeanDefinitionBuilder builder = EasyMock.createNiceMock(BeanDefinitionBuilder.class);
        builder.addConstructorArgValue(anyObject());
        final ArrayList<Object> constructorList = new ArrayList<>();
        expectLastCall().andAnswer(new IAnswer<Object>() {
            @Override
            public Object answer() throws Throwable {
                Object value = EasyMock.getCurrentArguments()[0];
                constructorList.add(value);
                return null;
            }
        }).anyTimes();

        replay(builder);

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(resource).getFile());
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(file);
        RemoteComponentsConfBeanDefinitionParser parser = new RemoteComponentsConfBeanDefinitionParser();
        parser.doParse(document.getDocumentElement(), builder);
        ConstructorObject constructorObject = new ConstructorObject((Set<String>) constructorList.get(0),
                (Set<String>) constructorList.get(1),
                (Map<String, List<RemoteComponentsConfigurationImpl.Property>>) constructorList.get(2));
        Assert.assertNotNull(constructorObject.includeSet);
        Assert.assertNotNull(constructorObject.excludeSet);
        Assert.assertNotNull(constructorObject.properties);

        return constructorObject;
    }

    private class ConstructorObject {
        public Set<String> includeSet;
        public Set<String> excludeSet;
        public Map<String, List<RemoteComponentsConfigurationImpl.Property>> properties;

        private ConstructorObject(Set<String> includeSet, Set<String> excludeSet,
                Map<String, List<RemoteComponentsConfigurationImpl.Property>> properties) {
            this.includeSet = includeSet;
            this.excludeSet = excludeSet;
            this.properties = properties;
        }
    }
}
