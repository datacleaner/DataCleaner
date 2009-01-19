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
package dk.eobjects.datacleaner.validator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import dk.eobjects.datacleaner.execution.IJobConfiguration;
import dk.eobjects.datacleaner.util.DomHelper;
import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.schema.Column;

/**
 * A validation rule configuration is a configured validation rule which is
 * serializable and thereby possible to store for execution again and again. A
 * validation rule configuration does not store the validation rule instance,
 * but just the class and the configuration to use for making instances.
 * 
 * @see dk.eobjects.datacleaner.validator.ValidatorExecutorCallback
 */
public class ValidatorJobConfiguration implements IJobConfiguration {

	public static final String NODE_NAME = "configuration";
	private static final long serialVersionUID = -3666518358685662574L;
	private IValidationRuleDescriptor _validationRuleDescriptor;
	private Map<String, String> _validationRuleProperties = new HashMap<String, String>();
	private Column[] _columns;

	public ValidatorJobConfiguration() {
	}

	public ValidatorJobConfiguration(IValidationRuleDescriptor descriptor) {
		this();
		setValidationRuleDescriptor(descriptor);
	}

	public void addValidationRuleProperty(String property, String value) {
		_validationRuleProperties.put(property, value);
	}

	public IValidationRuleDescriptor getValidationRuleDescriptor() {
		return _validationRuleDescriptor;
	}

	public void setValidationRuleDescriptor(
			IValidationRuleDescriptor validationRuleDescriptor) {
		_validationRuleDescriptor = validationRuleDescriptor;
	}

	public Map<String, String> getValidationRuleProperties() {
		return _validationRuleProperties;
	}

	public void setValidationRuleProperties(
			Map<String, String> validationRuleProperties) {
		_validationRuleProperties = validationRuleProperties;
	}

	public Column[] getColumns() {
		if (_columns == null) {
			return new Column[0];
		}
		return _columns;
	}

	public void setColumns(Column... columns) {
		_columns = columns;
	}

	@Override
	public String toString() {
		return "ValidationRuleConfiguration[validationRuleDescriptor="
				+ _validationRuleDescriptor + ",columns="
				+ ArrayUtils.toString(_columns) + ",properties="
				+ _validationRuleProperties.toString() + "]";
	}

	public void setColumns(List<Column> columns) {
		setColumns(columns.toArray(new Column[columns.size()]));
	}

	public static ValidatorJobConfiguration deserialize(Node node,
			DataContext dc) throws IllegalArgumentException {
		if (!NODE_NAME.equals(node.getNodeName())) {
			throw new IllegalArgumentException(
					"Node name must be 'configuration', found '"
							+ node.getNodeName() + "'");
		}
		String validationRuleClassName = DomHelper.getAttributeValue(node,
				"validationRuleClass");
		IValidationRuleDescriptor validationRuleDescriptor = ValidatorManager
				.getValidationRuleDescriptorByValidationRuleClassName(validationRuleClassName);
		if (validationRuleDescriptor == null) {
			throw new IllegalArgumentException(
					"Could not resolve validationRuleClass '"
							+ validationRuleClassName + "'");
		}
		ValidatorJobConfiguration configuration = new ValidatorJobConfiguration(
				validationRuleDescriptor);
		Map<String, String> properties = DomHelper
				.getPropertiesFromChildNodes(node);
		configuration.setValidationRuleProperties(properties);

		List<Column> columns = DomHelper.getColumnsFromChildNodes(node, dc);
		configuration.setColumns(columns);

		return configuration;
	}

	public Element serialize(Document document) {
		Element configurationElement = document.createElement(NODE_NAME);
		String profileClass = getValidationRuleDescriptor()
				.getValidationRuleClass().getName();
		configurationElement.setAttribute("validationRuleClass", profileClass);

		Map<String, String> properties = getValidationRuleProperties();
		DomHelper.addPropertyNodes(document, configurationElement, properties);

		Column[] columns = getColumns();
		DomHelper.addColumnNodes(document, configurationElement, columns);

		return configurationElement;
	}
}