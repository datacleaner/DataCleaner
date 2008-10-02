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
package dk.eobjects.datacleaner.profiler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import dk.eobjects.datacleaner.execution.IRunnableConfiguration;
import dk.eobjects.datacleaner.util.DomHelper;
import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.schema.Column;

/**
 * A profile configuration is a configured profile which is serializable and
 * thereby possible to store for execution again and again. A profile
 * configuration does not store the profile instance, but just the class and the
 * configuration to use for making instances.
 * 
 * @see dk.eobjects.datacleaner.execution.ProfileRunner
 */
public class ProfileConfiguration implements IRunnableConfiguration {

	public static final String NODE_NAME = "configuration";

	private static final long serialVersionUID = -3296880963830262834L;

	private IProfileDescriptor _profileDescriptor;
	private Map<String, String> _profileProperties = new HashMap<String, String>();
	private Column[] _columns;

	public ProfileConfiguration() {
	}

	public ProfileConfiguration(IProfileDescriptor profileDescriptor) {
		this();
		setProfileDescriptor(profileDescriptor);
	}

	public IProfileDescriptor getProfileDescriptor() {
		return _profileDescriptor;
	}

	public void setProfileDescriptor(IProfileDescriptor profileDescriptor) {
		_profileDescriptor = profileDescriptor;
	}

	public Map<String, String> getProfileProperties() {
		return _profileProperties;
	}

	public void setProfileProperties(Map<String, String> profileProperties) {
		_profileProperties = profileProperties;
	}

	public void addProfileProperty(String property, String value) {
		_profileProperties.put(property, value);
	}

	public void setColumns(Column... columns) {
		_columns = columns;
	}

	public void setColumns(List<Column> columns) {
		_columns = columns.toArray(new Column[columns.size()]);
	}

	public Column[] getColumns() {
		if (_columns == null) {
			return new Column[0];
		}
		return _columns;
	}

	@Override
	public String toString() {
		return "ProfileConfiguration[profileDescriptor=" + _profileDescriptor
				+ ",columns=" + ArrayUtils.toString(_columns) + ",properties="
				+ _profileProperties.toString() + "]";
	}

	public static ProfileConfiguration deserialize(Node node, DataContext dc)
			throws IllegalArgumentException {
		if (!NODE_NAME.equals(node.getNodeName())) {
			throw new IllegalArgumentException(
					"Node name must be 'configuration', found '"
							+ node.getNodeName() + "'");
		}
		String profileClassName = DomHelper.getAttributeValue(node,
				"profileClass");
		IProfileDescriptor profileDescriptor = ProfilerManager
				.getProfileDescriptorByProfileClassName(profileClassName);
		if (profileDescriptor == null) {
			throw new IllegalArgumentException(
					"Could not resolve profileClass '" + profileClassName + "'");
		}
		ProfileConfiguration configuration = new ProfileConfiguration(
				profileDescriptor);
		Map<String, String> properties = DomHelper
				.getPropertiesFromChildNodes(node);
		configuration.setProfileProperties(properties);

		List<Column> columns = DomHelper.getColumnsFromChildNodes(node, dc);
		configuration.setColumns(columns);

		return configuration;
	}

	public Element serialize(Document document) {
		Element configurationElement = document.createElement(NODE_NAME);
		String profileClass = getProfileDescriptor().getProfileClass()
				.getName();
		configurationElement.setAttribute("profileClass", profileClass);

		Map<String, String> properties = getProfileProperties();
		DomHelper.addPropertyNodes(document, configurationElement, properties);

		Column[] columns = getColumns();
		DomHelper.addColumnNodes(document, configurationElement, columns);

		return configurationElement;
	}
}