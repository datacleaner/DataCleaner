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
package dk.eobjects.datacleaner.data;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import dk.eobjects.datacleaner.util.DomHelper;
import dk.eobjects.datacleaner.util.WeakObservable;
import dk.eobjects.metamodel.CsvDataContextStrategy;
import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.ExcelDataContextStrategy;
import dk.eobjects.metamodel.IDataContextStrategy;
import dk.eobjects.metamodel.JdbcDataContextFactory;
import dk.eobjects.metamodel.OpenOfficeDataContextStrategy;
import dk.eobjects.metamodel.XmlDataContextStrategy;
import dk.eobjects.metamodel.schema.TableType;

/**
 * Represents the users selection of a data context. While the data context
 * itself can change and be replaced over time, the data context selection will
 * be the same in order to provide a hook for object who needs to access the
 * currently selected data context.
 * 
 * Also, the DataContextSelection provides some methods to serializing,
 * deserializing and duplicating DataContexts.
 * 
 * @see DataContext
 */
public class DataContextSelection extends WeakObservable {

	public static final String NODE_NAME = "dataContext";
	public static final String EXTENSION_COMMA_SEPARATED = "csv";
	public static final String EXTENSION_TAB_SEPARATED = "tsv";
	public static final String EXTENSION_XLS = "xls";
	public static final String EXTENSION_ODB = "odb";
	public static final String EXTENSION_XML = "xml";
	public static final String EXTENSION_DAT = "dat";
	public static final String EXTENSION_TEXT = "txt";

	private DataContext _dataContext;
	private Connection _connectionObject;
	private Map<String, String> _connectionMetadata = new HashMap<String, String>();

	public DataContextSelection() {
		super();
	}

	/**
	 * Convenience constructor mostly used for tests. When using this
	 * constructor, serialization, deserialization and duplication is not
	 * supported, since no metadata are recorded.
	 */
	public DataContextSelection(DataContext dataContext) {
		this();
		setDataContext(dataContext, null);
	}

	/**
	 * Sets the state of the current DataContext selection
	 * 
	 * @param dataContext
	 *            the DataContext to use.
	 * @param connectionObject
	 *            an optional connection object that should be automatically
	 *            closed if a new DataContext is set. Usefull for JDBC
	 *            connection objects.
	 * @return
	 */
	private DataContextSelection setDataContext(DataContext dataContext,
			Connection connectionObject) {
		if (_connectionObject != null) {
			try {
				_log.debug("closing previous connectionObject: "
						+ _connectionObject);
				_connectionObject.close();
			} catch (Exception e) {
				_log.warn("Could not close former connectionObject: " + e);
			}
		} else {
			_log.debug("no previous connection object to close");
		}
		_dataContext = dataContext;
		_connectionObject = connectionObject;
		setChanged();
		notifyObservers();
		return this;
	}

	public DataContext getDataContext() {
		return _dataContext;
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		if (_connectionObject != null) {
			try {
				_connectionObject.close();
			} catch (Exception e) {
				_log.warn("Could not close former connectionObject: " + e);
			}
		}
	}

	public boolean isJdbcSource() {
		if (_connectionMetadata.containsKey("connectionString")) {
			return true;
		}
		if (_connectionObject instanceof Connection) {
			return true;
		}
		// TODO: Ideally we would check whether or not the datacontext's
		// strategy was a JdbcDataContextStrategy instance, but we can't access
		// that object
		return false;
	}

	public void selectDatabase(String connectionString, String catalog,
			String username, String password, TableType[] tableTypes)
			throws SQLException {
		if (_log.isDebugEnabled()) {
			_log.debug("selectDatabase(" + connectionString + ',' + catalog
					+ ',' + username + ',' + password + ','
					+ ArrayUtils.toString(tableTypes) + ")");
		}

		if (tableTypes == null || tableTypes.length == 0) {
			tableTypes = new TableType[] { TableType.TABLE };
		}
		Connection connection = DriverManager.getConnection(connectionString,
				username, password);
		try {
			connection.setReadOnly(true);
		} catch (SQLException e) {
			_log
					.warn("Could not set the readOnly flag on the JDBC connection: "
							+ e.getMessage());
		}
		DataContext dc = JdbcDataContextFactory.getDataContext(connection,
				catalog, tableTypes);
		_connectionMetadata.clear();
		_connectionMetadata.put("connectionString", connectionString);
		_connectionMetadata.put("catalog", catalog);
		_connectionMetadata.put("username", username);
		_connectionMetadata.put("password", password);
		_connectionMetadata.put("tables", (ArrayUtils.indexOf(tableTypes,
				TableType.TABLE) != -1 ? "true" : "false"));
		_connectionMetadata.put("views", (ArrayUtils.indexOf(tableTypes,
				TableType.VIEW) != -1 ? "true" : "false"));
		setDataContext(dc, connection);
	}

	public void selectFile(File file, char separatorChar, char quoteChar) {
		if (_log.isDebugEnabled() && file != null) {
			_log.debug("selectFile(" + file.getAbsolutePath() + ','
					+ separatorChar + ',' + quoteChar + ")");
		}
		DataContext dc = new DataContext(new CsvDataContextStrategy(file,
				separatorChar, quoteChar));
		_connectionMetadata.clear();
		_connectionMetadata.put("filename", file.getAbsolutePath());
		_connectionMetadata.put("separator", "" + separatorChar);
		_connectionMetadata.put("quoteChar", "" + quoteChar);
		setDataContext(dc, null);
	}

	public void selectFile(File file) {
		if (_log.isDebugEnabled() && file != null) {
			_log.debug("selectFile(" + file.getAbsolutePath() + ")");
		}

		DataContext dc;
		try {
			IDataContextStrategy strategy = null;
			String extention = getExtention(file);
			if (extention.equals(EXTENSION_ODB)) {
				strategy = new OpenOfficeDataContextStrategy(file);
			} else if (extention.equals(EXTENSION_XML)) {
				strategy = new XmlDataContextStrategy(file, true);
			} else if (extention.equals(EXTENSION_XLS)) {
				strategy = new ExcelDataContextStrategy(file);
			} else {
				// Try XML parsing, perhaps there's a DOM that can be used
				Document document = DocumentBuilderFactory.newInstance()
						.newDocumentBuilder().parse(file);
				strategy = new XmlDataContextStrategy(file.getName(), document,
						true);
			}
			dc = new DataContext(strategy);
		} catch (Exception e1) {
			_log
					.debug(
							"Could not open file by using new DataContext(file)",
							e1);
			if (e1 instanceof RuntimeException) {
				throw (RuntimeException) e1;
			}
			throw new IllegalArgumentException(e1);
		}
		_connectionMetadata.clear();
		_connectionMetadata.put("filename", file.getAbsolutePath());
		setDataContext(dc, null);
	}

	public void selectNothing() {
		_log.debug("selectNothing()");
		_connectionMetadata.clear();
		setDataContext(null, null);
	}

	public Element serialize(Document document) {
		Element dataContextElement = document.createElement(NODE_NAME);
		DomHelper.addPropertyNodes(document, dataContextElement,
				_connectionMetadata);
		return dataContextElement;
	}

	public static DataContextSelection deserialize(Node node)
			throws SQLException {
		if (!NODE_NAME.equals(node.getNodeName())) {
			throw new IllegalArgumentException(
					"Node name must be 'dataContext', found '"
							+ node.getNodeName() + "'");
		}
		Map<String, String> properties = DomHelper
				.getPropertiesFromChildNodes(node);

		DataContextSelection dcs = new DataContextSelection();
		if (properties.containsKey("filename")) {
			File file = new File(properties.get("filename"));
			String seperatorString = properties.get("separator");
			String quoteCharString = properties.get("quoteChar");
			if (seperatorString != null && seperatorString.length() == 1
					&& quoteCharString != null && quoteCharString.length() == 1) {
				dcs.selectFile(file, seperatorString.charAt(0), quoteCharString
						.charAt(0));
			} else {
				dcs.selectFile(file);
			}
		} else if (properties.containsKey("connectionString")) {
			String connectionString = properties.get("connectionString");
			String catalog = properties.get("catalog");
			String username = properties.get("username");
			String password = properties.get("password");
			List<TableType> tableTypes = new ArrayList<TableType>();
			if ("true".equals(properties.get("tables"))) {
				tableTypes.add(TableType.TABLE);
			}
			if ("true".equals(properties.get("views"))) {
				tableTypes.add(TableType.VIEW);
			}
			dcs.selectDatabase(connectionString, catalog, username, password,
					tableTypes.toArray(new TableType[tableTypes.size()]));
		} else {
			throw new IllegalArgumentException(
					"Could not determine type of DataContext in DataContextSelection deserialization. Properties: "
							+ properties.toString());
		}
		return dcs;
	}

	public boolean isDuplicatable() {
		return isJdbcSource();
	}

	/**
	 * Creates a duplicate of this DataContextSelection with an individual
	 * connection object if it is an SQL based datastore.
	 * 
	 * @throws SQLException
	 *             if making a new connection based on the existing connections
	 *             metadata throws an exception.
	 * @throws IllegalStateException
	 *             if the DataContextSelection is not duplicatable
	 * 
	 * @see #isDuplicatable()
	 */
	public DataContextSelection duplicate() throws SQLException,
			IllegalStateException {
		if (isDuplicatable()) {
			DataContextSelection result = new DataContextSelection();
			List<TableType> tableTypes = new ArrayList<TableType>();
			if ("true".equals(_connectionMetadata.get("tables"))) {
				tableTypes.add(TableType.TABLE);
			}
			if ("true".equals(_connectionMetadata.get("views"))) {
				tableTypes.add(TableType.VIEW);
			}
			result.selectDatabase(_connectionMetadata.get("connectionString"),
					_connectionMetadata.get("catalog"), _connectionMetadata
							.get("username"), _connectionMetadata
							.get("password"), tableTypes
							.toArray(new TableType[tableTypes.size()]));
			return result;
		} else {
			throw new IllegalStateException(
					"DataContextSelection is not duplicatable");
		}
	}

	public static String getExtention(File file) {
		if (file != null) {
			String temp = file.getName();
			int i = temp.lastIndexOf('.');
			if (i != -1) {
				return (temp.substring(i + 1, temp.length()));
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("connectionString",
						_connectionMetadata.get("connectionString")).append(
						"filename", _connectionMetadata.get("filename"))
				.toString();
	}
}