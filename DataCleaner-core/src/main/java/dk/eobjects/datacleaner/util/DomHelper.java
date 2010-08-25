/**
 *  This file is part of DataCleaner.
 *
 *  DataCleaner is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DataCleaner is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DataCleaner.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.eobjects.datacleaner.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Schema;
import dk.eobjects.metamodel.schema.Table;

/**
 * Helper class with methods to serialize to and deserialize from the XML
 * Document Object Model (DOM).
 */
public class DomHelper {

	public static List<Node> getChildNodesByName(Node parentNode,
			String childNodeName) {
		List<Node> result = new ArrayList<Node>();
		if (childNodeName != null) {
			NodeList childNodes = parentNode.getChildNodes();
			for (int i = 0; i < childNodes.getLength(); i++) {
				Node childNode = childNodes.item(i);
				if (childNodeName.equals(childNode.getNodeName())) {
					result.add(childNode);
				}
			}
		}
		return result;
	}

	public static String getAttributeValue(Node node, String attributeName) {
		Attr attr = (Attr) node.getAttributes().getNamedItem(attributeName);
		if (attr == null) {
			return null;
		}
		return attr.getValue();
	}

	public static void addColumnNodes(Document doc, Node superNode,
			Column[] columns) {
		for (Column column : columns) {
			if (column != null) {
				Element columnElement = doc.createElement("column");
				columnElement.setTextContent(column.getName());
				
				Table table = column.getTable();
				if (table != null) {

					if (table.getName() != null) {
						columnElement.setAttribute("table", table.getName());
					}
				
					Schema schema = table.getSchema();
					if (schema != null && schema.getName() != null) {
						columnElement.setAttribute("schema", schema.getName());
					}
				}
				
				superNode.appendChild(columnElement);
			}
		}
	}

	public static void addPropertyNodes(Document doc, Node superNode,
			Map<String, String> properties) {
		ArrayList<Entry<String, String>> entryList = new ArrayList<Entry<String, String>>(
				properties.entrySet());
		Comparator<Entry<String, String>> comparator = new Comparator<Entry<String, String>>() {
			public int compare(Entry<String, String> o1,
					Entry<String, String> o2) {
				String key1 = o1.getKey();
				if (key1 != null) {
					return key1.compareTo(o2.getKey());
				}
				return -1;
			}
		};
		Collections.sort(entryList, comparator);
		for (Entry<String, String> entry : entryList) {
			addPropertyNode(doc, superNode, entry.getKey(), entry.getValue());
		}
	}

	public static void addPropertyNode(Document doc, Node superNode,
			String key, String value) {
		Element propertyElement = doc.createElement("property");
		propertyElement.setAttribute("name", key);
		propertyElement.setTextContent(value);

		superNode.appendChild(propertyElement);
	}

	public static Map<String, String> getPropertiesFromChildNodes(Node node) {
		Map<String, String> properties = new HashMap<String, String>();
		List<Node> propertyNodes = DomHelper.getChildNodesByName(node,
				"property");
		for (Node propertyNode : propertyNodes) {
			String propertyName = DomHelper.getAttributeValue(propertyNode,
					"name");
			String propertyValue = propertyNode.getTextContent();
			if (propertyValue != null) {
				propertyValue = propertyValue.trim();
			}
			properties.put(propertyName, propertyValue);
		}
		return properties;
	}

	public static List<Column> getColumnsFromChildNodes(Node node,
			DataContext dataContext) {
		List<Node> columnNodes = DomHelper.getChildNodesByName(node, "column");
		List<Column> columns = new ArrayList<Column>(columnNodes.size());
		for (Node columnNode : columnNodes) {
			String schemaName = DomHelper.getAttributeValue(columnNode,
					"schema");
			String tableName = DomHelper.getAttributeValue(columnNode, "table");
			String columnName = columnNode.getTextContent();
			if (columnName != null) {
				columnName = columnName.trim();
			}
			try {
				Schema schema = dataContext.getSchemaByName(schemaName);
				Table table = schema.getTableByName(tableName);
				Column column = table.getColumnByName(columnName);
				columns.add(column);
			} catch (NullPointerException e) {
				throw new IllegalArgumentException("Could not resolve column '"
						+ columnName + "'", e);
			}
		}
		return columns;
	}

	public static DocumentBuilder getDocumentBuilder() {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setIgnoringComments(true);
			DocumentBuilder db = dbf.newDocumentBuilder();
			return db;
		} catch (Exception e) {
			// This shouldn't be possible
			throw new RuntimeException(e);
		}
	}

	public static void transform(Node node, Result result) {
		try {
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer trans = tf.newTransformer();
			trans.setOutputProperty(OutputKeys.INDENT, "yes");
			trans.transform(new DOMSource(node), result);
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static String getChildNodeText(Node node, String childNodeName) {
		List<Node> childNodes = getChildNodesByName(node, childNodeName);
		if (childNodes.isEmpty()) {
			return null;
		}
		if (childNodes.size() > 1) {
			throw new IllegalArgumentException("The node " + node
					+ " contains several childNodes named " + childNodeName);
		}
		return getText(childNodes.get(0));
	}

	public static String getText(Node node) {
		Element element = (Element) node;
		return element.getTextContent();
	}
}