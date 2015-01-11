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
package org.datacleaner.configuration;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.JAXBElement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.metamodel.DataContext;
import org.apache.metamodel.MetaModelHelper;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.Row;
import org.apache.metamodel.pojo.ArrayTableDataProvider;
import org.apache.metamodel.pojo.TableDataProvider;
import org.apache.metamodel.query.Query;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.ColumnTypeImpl;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.CollectionUtils;
import org.apache.metamodel.util.SimpleTableDef;
import org.datacleaner.configuration.jaxb.AbstractDatastoreType;
import org.datacleaner.configuration.jaxb.PojoDatastoreType;
import org.datacleaner.configuration.jaxb.PojoTableType;
import org.datacleaner.configuration.jaxb.PojoTableType.Columns;
import org.datacleaner.configuration.jaxb.PojoTableType.Rows;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.connection.PojoDatastore;
import org.datacleaner.util.CollectionUtils2;
import org.datacleaner.util.ReflectionUtils;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.convert.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Convenient utility class for reading and writing POJO datastores from and to
 * XML (JAXB) elements.
 */
public class JaxbPojoDatastoreAdaptor {

    private static final Logger logger = LoggerFactory.getLogger(JaxbPojoDatastoreAdaptor.class);

    private final StringConverter _converter;

    public JaxbPojoDatastoreAdaptor() {
        _converter = new StringConverter(null);
    }

    public PojoDatastore read(PojoDatastoreType pojoDatastore) {
        final String name = pojoDatastore.getName();
        final String schemaName = (pojoDatastore.getSchemaName() == null ? name : pojoDatastore.getSchemaName());

        final List<TableDataProvider<?>> tableDataProviders = new ArrayList<TableDataProvider<?>>();
        final List<PojoTableType> tables = pojoDatastore.getTable();
        for (PojoTableType table : tables) {
            final String tableName = table.getName();

            final List<Columns.Column> columns = table.getColumns().getColumn();
            final int columnCount = columns.size();
            final String[] columnNames = new String[columnCount];
            final ColumnType[] columnTypes = new ColumnType[columnCount];

            for (int i = 0; i < columnCount; i++) {
                final Columns.Column column = columns.get(i);
                columnNames[i] = column.getName();
                columnTypes[i] = ColumnTypeImpl.valueOf(column.getType());
            }

            final SimpleTableDef tableDef = new SimpleTableDef(tableName, columnNames, columnTypes);

            final Collection<Object[]> arrays = new ArrayList<Object[]>();
            final Rows rowsType = table.getRows();
            if (rowsType != null) {
                final List<Rows.Row> rows = rowsType.getRow();
                for (Rows.Row row : rows) {
                    final List<Object> values = row.getV();
                    if (values.size() != columnCount) {
                        throw new IllegalStateException("Row value count is not equal to column count in datastore '"
                                + name + "'. Expected " + columnCount + " values, found " + values.size() + " (table "
                                + tableName + ", row no. " + arrays.size() + ")");
                    }
                    final Object[] array = new Object[columnCount];
                    for (int i = 0; i < array.length; i++) {

                        final Class<?> expectedClass = columnTypes[i].getJavaEquivalentClass();

                        final Object rawValue = values.get(i);
                        final Object value = deserializeValue(rawValue, expectedClass);
                        array[i] = value;
                    }
                    arrays.add(array);
                }
            }

            final TableDataProvider<?> tableDataProvider = new ArrayTableDataProvider(tableDef, arrays);
            tableDataProviders.add(tableDataProvider);
        }

        final PojoDatastore ds = new PojoDatastore(name, schemaName, tableDataProviders);
        return ds;
    }

    private Object deserializeValue(final Object value, Class<?> expectedClass) {
        if (value == null) {
            return null;
        }

        if (value instanceof Node) {
            final Node node = (Node) value;
            logger.debug("Value is a DOM node: {}", node);
            return getNodeValue(node, expectedClass);
        }

        if (value instanceof JAXBElement) {
            final JAXBElement<?> element = (JAXBElement<?>) value;
            logger.debug("Value is a JAXBElement: {}", element);
            final Object jaxbValue = element.getValue();
            return deserializeValue(jaxbValue, expectedClass);
        }

        if (value instanceof String) {
            String str = (String) value;
            return _converter.deserialize(str, expectedClass);
        } else {
            throw new UnsupportedOperationException("Unknown value type: " + value);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T getNodeValue(Node node, Class<T> expectedClass) {
        if (node.getNodeType() == Node.TEXT_NODE) {
            final String str = node.getNodeValue();
            if (expectedClass == null) {
                // we will fall back to string class
                expectedClass = (Class<T>) String.class;
            }
            final Object result = _converter.deserialize(str, determineExpectedClass(node, expectedClass));
            return (T) result;
        }

        // a top-level value
        final List<Node> childNodes = getChildNodes(node);
        if (childNodes.isEmpty()) {
            return null;
        } else if (childNodes.size() == 1 && childNodes.get(0).getNodeType() == Node.TEXT_NODE) {
            expectedClass = (Class<T>) determineExpectedClass(node, expectedClass);
            final Node child = childNodes.get(0);
            return getNodeValue(child, expectedClass);
        }

        if (expectedClass == null) {
            final Node firstChild = childNodes.get(0);
            if ("i".equals(firstChild.getNodeName())) {
                final List<Object> list = getNodeList(childNodes);
                return (T) list;
            } else if ("e".equals(firstChild.getNodeName())) {
                final Map<String, Object> map = getNodeMap(childNodes);
                return (T) map;
            } else {
                throw new UnsupportedOperationException("Unexpected child nodes. First child: " + printNode(firstChild));
            }
        } else if (ReflectionUtils.is(expectedClass, List.class)) {
            final List<Object> list = getNodeList(childNodes);
            return (T) list;
        } else if (ReflectionUtils.is(expectedClass, Map.class)) {
            final Map<String, Object> map = getNodeMap(childNodes);
            return (T) map;
        } else if (expectedClass.isArray()) {
            final List<Object> list = getNodeList(childNodes);
            final Class<?> componentType = expectedClass.getComponentType();
            return (T) CollectionUtils2.toArray(list, componentType);
        }

        throw new UnsupportedOperationException("Not a value (v) node type: " + printNode(node));
    }

    private Class<?> determineExpectedClass(Node node, Class<?> fallbackType) {
        NamedNodeMap attributes = node.getAttributes();
        if (attributes != null) {
            final Node attribute = attributes.getNamedItem("class");
            if (attribute != null) {
                final String className = attribute.getTextContent();
                if (!StringUtils.isNullOrEmpty(className)) {
                    try {
                        final Class<?> cls = Class.forName(className);
                        return cls;
                    } catch (ClassNotFoundException e) {
                        logger.error("Could not load class: " + className + ". Falling back to String type.", e);
                    }
                }
            }
        }

        return fallbackType;
    }

    private List<Object> getNodeList(List<Node> childNodes) {
        final List<Object> list = new ArrayList<Object>();
        for (Node childNode : childNodes) {
            Object value = getNodeValue(childNode, null);
            list.add(value);
        }
        return list;
    }

    private List<Node> getChildNodes(Node node) {
        final List<Node> list = new ArrayList<Node>();
        final NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            final Node child = childNodes.item(i);
            switch (child.getNodeType()) {
            case Node.ELEMENT_NODE:
                list.add(child);
            case Node.TEXT_NODE:
                String text = child.getNodeValue();
                if (!StringUtils.isNullOrEmpty(text)) {
                    list.add(child);
                }
            default: // ignore
            }
        }
        return list;
    }

    private Map<String, Object> getNodeMap(List<Node> entryNodes) {
        final Map<String, Object> map = new LinkedHashMap<String, Object>();
        for (Node entryNode : entryNodes) {
            final String entryNodeName = entryNode.getNodeName();
            if (!"e".equals(entryNodeName)) {
                throw new UnsupportedOperationException(
                        "Node passed as Map entry does not appear to be the right type: " + printNode(entryNode));
            }

            String key = null;
            Object value = null;

            final List<Node> keyOrValueNodes = getChildNodes(entryNode);

            assert keyOrValueNodes.size() == 2;

            for (Node keyOrValueNode : keyOrValueNodes) {
                final String keyOrValueNodeName = keyOrValueNode.getNodeName();
                if ("k".equals(keyOrValueNodeName)) {
                    key = getNodeValue(keyOrValueNode, String.class);
                } else if ("v".equals(keyOrValueNodeName)) {
                    value = getNodeValue(keyOrValueNode, null);
                }
            }

            if (key == null) {
                throw new UnsupportedOperationException("Map key (k) node not set in entry: " + printNode(entryNode));
            }

            map.put(key, value);
        }
        return map;
    }

    private String printNode(Node node) {
        try {
            // Set up the output transformer
            TransformerFactory transfac = TransformerFactory.newInstance();
            Transformer trans = transfac.newTransformer();
            trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            trans.setOutputProperty(OutputKeys.INDENT, "yes");

            // Print the DOM node
            StringWriter sw = new StringWriter();
            StreamResult result = new StreamResult(sw);
            DOMSource source = new DOMSource(node);
            trans.transform(source, result);
            String xmlString = sw.toString();
            return xmlString;
        } catch (TransformerException e) {
            logger.warn("Could not transform node '" + node + "' to pretty string: " + e.getMessage(), e);
            return node.toString();
        }
    }

    private org.datacleaner.configuration.jaxb.PojoTableType.Rows.Row createPojoRow(Row row, Document document) {
        final org.datacleaner.configuration.jaxb.PojoTableType.Rows.Row rowType = new org.datacleaner.configuration.jaxb.PojoTableType.Rows.Row();
        final Object[] values = row.getValues();
        for (Object value : values) {
            final Element elem = document.createElement("v");
            createPojoValue(value, elem, document, false);
            rowType.getV().add(elem);
        }
        return rowType;
    }

    private void createPojoValue(Object value, Element elem, Document document, boolean explicitType) {
        if (value == null) {
            // return an empty element
            return;
        }

        if (value.getClass().isArray()) {
            Class<?> componentType = value.getClass().getComponentType();
            if (componentType.isPrimitive() || componentType == String.class) {
                // leave the array to be serialized using the string converter -
                // it
                // will take up much less space.
            } else {
                value = CollectionUtils.toList(value);
            }
        }

        if (value instanceof List) {
            List<?> list = (List<?>) value;
            for (Object item : list) {
                final Element itemElement = document.createElement("i");
                createPojoValue(item, itemElement, document, true);
                elem.appendChild(itemElement);
            }
            return;
        }

        if (value instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) value;
            for (Entry<?, ?> entry : map.entrySet()) {
                final Element keyElement = document.createElement("k");
                createPojoValue(entry.getKey(), keyElement, document, true);

                final Element valueElement = document.createElement("v");
                createPojoValue(entry.getValue(), valueElement, document, true);

                final Element entryElement = document.createElement("e");
                entryElement.appendChild(keyElement);
                entryElement.appendChild(valueElement);

                elem.appendChild(entryElement);
            }
            return;
        }

        try {
            final String stringValue = _converter.serialize(value);
            elem.setTextContent(stringValue);
            if (explicitType) {
                elem.setAttribute("class", value.getClass().getName());
            }
        } catch (RuntimeException e) {
            logger.warn("Failed to serialize value: " + value + ". Returning null.", e);
        }
        return;
    }

    protected DocumentBuilder createDocumentBuilder() {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("Failed to create DocumentBuilder", e);
        }
    }

    private org.datacleaner.configuration.jaxb.PojoTableType.Columns.Column createPojoColumn(String name,
            ColumnType type) {
        org.datacleaner.configuration.jaxb.PojoTableType.Columns.Column columnType = new org.datacleaner.configuration.jaxb.PojoTableType.Columns.Column();
        columnType.setName(name);
        columnType.setType(type.toString());
        return columnType;
    }

    public PojoTableType createPojoTable(final DataContext dataContext, final Table table, final Column[] usedColumns,
            final int maxRows) {
        final PojoTableType tableType = new PojoTableType();
        tableType.setName(table.getName());

        // read columns
        final Columns columnsType = new Columns();
        for (Column column : usedColumns) {
            columnsType.getColumn().add(createPojoColumn(column.getName(), column.getType()));
        }
        tableType.setColumns(columnsType);

        if (maxRows > 0) {
            // read values
            final Query q = dataContext.query().from(table).select(usedColumns).toQuery();
            q.setMaxRows(maxRows);

            final DocumentBuilder documentBuilder = createDocumentBuilder();
            final Document document = documentBuilder.newDocument();
            final Rows rowsType = new Rows();
            try (final DataSet ds = dataContext.executeQuery(q)) {
                while (ds.next()) {
                    final Row row = ds.getRow();
                    rowsType.getRow().add(createPojoRow(row, document));
                }
            }

            tableType.setRows(rowsType);
        }

        return tableType;
    }

    public AbstractDatastoreType createPojoDatastore(final String datastoreName, final String schemaName,
            final Collection<PojoTableType> tables) {
        final PojoDatastoreType datastoreType = new PojoDatastoreType();
        datastoreType.setName(datastoreName);
        datastoreType.setSchemaName(schemaName);
        datastoreType.getTable().addAll(tables);

        return datastoreType;
    }

    /**
     * Creates a serialized POJO copy of a datastore.
     * 
     * @param datastore
     *            the datastore to copy
     * @param columns
     *            the columns to include, or null if all tables/columns should
     *            be included.
     * @param maxRowsToQuery
     *            the maximum number of records to query and include in the
     *            datastore copy. Keep this number reasonably low, or else the
     *            copy might cause out-of-memory issues (Both while reading and
     *            writing).
     * @return
     */
    public AbstractDatastoreType createPojoDatastore(final Datastore datastore, final Set<Column> columns,
            final int maxRowsToQuery) {
        final PojoDatastoreType datastoreType = new PojoDatastoreType();
        datastoreType.setName(datastore.getName());
        datastoreType.setDescription(datastore.getDescription());

        try (final DatastoreConnection con = datastore.openConnection()) {
            final DataContext dataContext = con.getDataContext();

            final Schema schema;
            final Table[] tables;
            if (columns == null || columns.isEmpty()) {
                schema = dataContext.getDefaultSchema();
                tables = schema.getTables();
            } else {
                tables = MetaModelHelper.getTables(columns);
                // TODO: There's a possibility that tables span multiple
                // schemas, but we cannot currently support that in a
                // PojoDatastore, so we just pick the first and cross our
                // fingers.
                schema = tables[0].getSchema();
            }

            datastoreType.setSchemaName(schema.getName());

            for (final Table table : tables) {
                final Column[] usedColumns;
                if (columns == null || columns.isEmpty()) {
                    usedColumns = table.getColumns();
                } else {
                    usedColumns = MetaModelHelper.getTableColumns(table, columns);
                }

                final PojoTableType tableType = createPojoTable(dataContext, table, usedColumns, maxRowsToQuery);
                datastoreType.getTable().add(tableType);
            }
        }

        return datastoreType;
    }
}
