package org.datacleaner.monitor.configuration;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.datacleaner.configuration.RemoteComponentsConfigurationImpl;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Class RemoteControllersConfBeanDefinitionParser
 * 
 * @author k.houzvicka
 * @since 18.9.15
 */
public class RemoteComponentsConfBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    private static final String NAME_SPACE = "http://www.datacleaner.org/schema/remoteComponentsConfiguration";

    protected Class getBeanClass(Element element) {
        return RemoteComponentsConfigurationImpl.class;
    }

    protected void doParse(Element element, BeanDefinitionBuilder bean) {

        Node includes = element.getElementsByTagNameNS(NAME_SPACE, "includes").item(0);
        Node excludes = element.getElementsByTagNameNS(NAME_SPACE, "excludes").item(0);
        Node defaults = element.getElementsByTagNameNS(NAME_SPACE, "defaults").item(0);

        Set<String> includesSet = parseListFromNode(includes);
        Set<String> excludeSet = parseListFromNode(excludes);

        Map<String, List<RemoteComponentsConfigurationImpl.Property>> properties = parseDefaults(defaults);

        bean.addConstructorArgValue(includesSet);
        bean.addConstructorArgValue(excludeSet);
        bean.addConstructorArgValue(properties);
    }

    private Map<String, List<RemoteComponentsConfigurationImpl.Property>> parseDefaults(Node node) {
        Map<String, List<RemoteComponentsConfigurationImpl.Property>> properties = new HashMap<>();
        if (node == null) {
            return properties;
        }
        NodeList components = node.getChildNodes();
        if (components != null) {
            for (int i = 0; i < components.getLength(); i++) {
                Node component = components.item(i);
                NamedNodeMap attributes = component.getAttributes();
                if (attributes != null) {
                    String componentName = attributes.getNamedItem("name").getTextContent();
                    NodeList propertiesNodeList = component.getChildNodes();
                    if (propertiesNodeList != null) {
                        Node propertiesNode = propertiesNodeList.item(1);
                        List<RemoteComponentsConfigurationImpl.Property> propertyList = parseProperties(propertiesNode);
                        if (!propertyList.isEmpty()) {
                            properties.put(componentName, propertyList);
                        }
                    }
                }
            }
        }

        return properties;
    }

    private List<RemoteComponentsConfigurationImpl.Property> parseProperties(Node propertiesNode) {
        List<RemoteComponentsConfigurationImpl.Property> propertyList = new ArrayList<>();
        if (propertiesNode != null) {
            NodeList propertyNodeList = propertiesNode.getChildNodes();
            for (int j = 0; j < propertyNodeList.getLength(); j++) {
                NamedNodeMap propertyAttr = propertyNodeList.item(j).getAttributes();
                if (propertyAttr != null) {
                    String propertyName = propertyAttr.getNamedItem("name").getTextContent().trim();
                    String propertyValue = nodeToString(propertyNodeList.item(j));
                    boolean isSimpleString = isSimpleString(propertyNodeList.item(j));
                    propertyList.add(new RemoteComponentsConfigurationImpl.Property(propertyName, propertyValue, isSimpleString));
                }
            }

        }
        return propertyList;
    }

    /**
     * Reads content from element.
     * 
     * @param node
     * @return
     */
    private String nodeToString(Node node) {
        if (node == null) {
            return null;
        }
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i).getLocalName() != null) {
                StringWriter sw = new StringWriter();
                try {
                    Transformer transformer = TransformerFactory.newInstance().newTransformer();
                    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                    transformer.transform(new DOMSource(childNodes.item(i)), new StreamResult(sw));
                } catch (TransformerException te) {
                    continue;
                }
                return sw.toString();
            }
        }
        return node.getTextContent().trim();
    }

    /**
     * Check the element. If it is only string --> true, The xml object -> false
     *
     * @param node
     * @return
     */
    private boolean isSimpleString(Node node) {
        if (node == null) {
            return true;
        }
        return node.getLocalName().equals("property");
    }

    private Set<String> parseListFromNode(Node node) {
        Set<String> set = new HashSet<>();
        if (node != null) {
            NodeList childNodes = node.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node iChild = childNodes.item(i);
                NamedNodeMap attributes = iChild.getAttributes();
                if (attributes != null) {
                    String oneName = attributes.getNamedItem("name").getTextContent().trim();
                    set.add(oneName);
                }
            }
        }
        return set;

    }

}
