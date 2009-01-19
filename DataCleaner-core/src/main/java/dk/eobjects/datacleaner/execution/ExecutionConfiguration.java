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
package dk.eobjects.datacleaner.execution;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import dk.eobjects.datacleaner.util.DomHelper;

/**
 * Represents the configuration that defines how to execute a composite job in
 * an IRunner instance. This configuration contains all performance and memory
 * oriented options as well as any options that relate to how the IRunner
 * instance should perform execution.
 */
public class ExecutionConfiguration implements Serializable {

	private static final long serialVersionUID = -9020571795601572387L;

	public static final String NODE_NAME = "executionConfiguration";

	private Long _querySplitterSize;
	private int _maxQueriesPerConnection;
	private int _maxConnections;
	private boolean _drillToDetailEnabled;
	private boolean _groupByOptimizationEnabled;

	public ExecutionConfiguration() {
		this(true, true);
	}

	public ExecutionConfiguration(boolean drillToDetailsEnabled,
			boolean groupByOptimizationEnabled) {
		this(null, drillToDetailsEnabled, groupByOptimizationEnabled, 1, 1);
	}

	public ExecutionConfiguration(Long querySplitterSize) {
		this(querySplitterSize, true, false, 1, 1);
	}

	public ExecutionConfiguration(Long querySplitterSize,
			boolean drillToDetailsEnabled, boolean groupByOptimizationEnabled,
			int maxConnections, int maxQueriesPerConnection) {
		setQuerySplitterSize(querySplitterSize);
		_drillToDetailEnabled = drillToDetailsEnabled;
		_groupByOptimizationEnabled = groupByOptimizationEnabled;
		setMaxConnections(maxConnections);
		setMaxQueriesPerConnection(maxQueriesPerConnection);
	}

	public boolean isQuerySplitterEnabled() {
		return (_querySplitterSize != null);
	}

	public long getQuerySplitterSize() {
		return _querySplitterSize;
	}

	public void setQuerySplitterSize(Long size) {
		if (size != null && size < 1) {
			throw new IllegalArgumentException(
					"querySplitterSize cannot be less than 1");
		}
		_querySplitterSize = size;
	}

	public boolean isDrillToDetailEnabled() {
		return _drillToDetailEnabled;
	}

	public void setDrillToDetailEnabled(boolean drillToDetailEnabled) {
		_drillToDetailEnabled = drillToDetailEnabled;
	}

	public boolean isGroupByOptimizationEnabled() {
		return _groupByOptimizationEnabled;
	}

	public void setGroupByOptimizationEnabled(boolean groupByOptimizationEnabled) {
		_groupByOptimizationEnabled = groupByOptimizationEnabled;
	}

	public boolean isMultipleConnectionsEnabled() {
		if (_maxConnections == 1) {
			return false;
		}
		return true;
	}

	public int getMaxConnections() {
		return _maxConnections;
	}

	public void setMaxConnections(int maxConnections) {
		if (maxConnections <= 0) {
			throw new IllegalArgumentException(
					"maxConnections cannot be negative or 0");
		}
		_maxConnections = maxConnections;
	}

	public boolean isMultipleQueriesPerConnectionEnabled() {
		if (_maxQueriesPerConnection == 1) {
			return false;
		}
		return true;
	}

	public int getMaxQueriesPerConnection() {
		return _maxQueriesPerConnection;
	}

	public void setMaxQueriesPerConnection(int maxQueriesPerConnection) {
		if (maxQueriesPerConnection <= 0) {
			throw new IllegalArgumentException(
					"maxQueriesPerConnection cannot be negative or 0");
		}
		_maxQueriesPerConnection = maxQueriesPerConnection;
	}

	public Element serialize(Document document) {
		Element executionConfigurationElement = document
				.createElement(NODE_NAME);
		if (_querySplitterSize != null) {
			DomHelper.addPropertyNode(document, executionConfigurationElement,
					"querySplitterSize", Long.toString(_querySplitterSize));
		}
		DomHelper.addPropertyNode(document, executionConfigurationElement,
				"maxConnections", Integer.toString(_maxConnections));
		DomHelper.addPropertyNode(document, executionConfigurationElement,
				"maxQueriesPerConnection", Integer
						.toString(_maxQueriesPerConnection));
		DomHelper
				.addPropertyNode(document, executionConfigurationElement,
						"drillToDetailEnabled", Boolean
								.toString(_drillToDetailEnabled));
		DomHelper.addPropertyNode(document, executionConfigurationElement,
				"groupByOptimizationEnabled", Boolean
						.toString(_groupByOptimizationEnabled));
		return executionConfigurationElement;
	}

	public static ExecutionConfiguration deserialize(Node node)
			throws SQLException {
		if (!NODE_NAME.equals(node.getNodeName())) {
			throw new IllegalArgumentException(
					"Node name must be 'executionConfiguration', found '"
							+ node.getNodeName() + "'");
		}
		Map<String, String> properties = DomHelper
				.getPropertiesFromChildNodes(node);

		ExecutionConfiguration ec = new ExecutionConfiguration();

		if (properties.containsKey("querySplitterSize")) {
			ec.setQuerySplitterSize(Long.parseLong(properties
					.get("querySplitterSize")));
		}
		if (properties.containsKey("maxConnections")) {
			ec.setMaxConnections(Integer.parseInt(properties
					.get("maxConnections")));
		}
		if (properties.containsKey("maxQueriesPerConnection")) {
			ec.setMaxQueriesPerConnection(Integer.parseInt(properties
					.get("maxQueriesPerConnection")));
		}
		ec.setDrillToDetailEnabled(Boolean.parseBoolean(properties
				.get("drillToDetailEnabled")));
		ec.setGroupByOptimizationEnabled(Boolean.parseBoolean(properties
				.get("groupByOptimizationEnabled")));
		return ec;
	}
}