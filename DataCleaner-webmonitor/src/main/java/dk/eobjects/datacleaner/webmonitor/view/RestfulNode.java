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
package dk.eobjects.datacleaner.webmonitor.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import dk.eobjects.datacleaner.profiler.IProfileDescriptor;
import dk.eobjects.datacleaner.validator.IValidationRuleDescriptor;

public class RestfulNode {

	private String _name;
	private String _text;
	private List<RestfulNode> _innerNodes = new ArrayList<RestfulNode>();
	private Map<String, String> _attributes = new TreeMap<String, String>();

	public static RestfulNode newModulesNode() {
		return new RestfulNode("modules");
	}

	public static RestfulNode newProfileDescriptorNode(
			IProfileDescriptor profileDescriptor) {
		RestfulNode result = new RestfulNode("profileDescriptor");
		result.addAttribute("profileClass", profileDescriptor.getProfileClass()
				.getName());
		result.addAttribute("displayName", profileDescriptor.getDisplayName());
		String[] propertyNames = profileDescriptor.getPropertyNames();
		for (int i = 0; i < propertyNames.length; i++) {
			String propertyName = propertyNames[i];
			RestfulNode propertyNode = new RestfulNode("propertyName");
			propertyNode.setText(propertyName);
			result.addInnerNode(propertyNode);
		}
		return result;
	}

	public static RestfulNode newValidationRuleDescriptorNode(
			IValidationRuleDescriptor validationRuleDescriptor) {
		RestfulNode result = new RestfulNode("validationRuleDescriptor");
		result.addAttribute("validationRuleClass", validationRuleDescriptor
				.getValidationRuleClass().getName());
		result.addAttribute("displayName", validationRuleDescriptor
				.getDisplayName());
		String[] propertyNames = validationRuleDescriptor.getPropertyNames();
		for (int i = 0; i < propertyNames.length; i++) {
			String propertyName = propertyNames[i];
			RestfulNode propertyNode = new RestfulNode("propertyName");
			propertyNode.setText(propertyName);
			result.addInnerNode(propertyNode);
		}
		return result;
	}

	private void setText(String text) {
		_text = text;
	}

	private RestfulNode(String name) {
		_name = name;
	}

	public void addInnerNode(RestfulNode node) {
		_innerNodes.add(node);
	}

	public void addAttribute(String key, String value) {
		_attributes.put(key, value);
	}

	private String toString(String offset) {
		StringBuilder sb = new StringBuilder();
		sb.append(offset + "<" + _name);
		for (String attributeKey : _attributes.keySet()) {
			sb.append(" " + attributeKey + "=\""
					+ _attributes.get(attributeKey) + "\"");
		}
		if (_innerNodes.size() > 0) {
			sb.append(">");
			for (RestfulNode innerNode : _innerNodes) {
				sb.append("\n" + innerNode.toString(offset + " "));
			}
			sb.append("\n" + offset + "</" + _name + ">");
		} else {
			if (_text == null) {
				sb.append("/>");
			} else {
				sb.append(">" + _text + "</" + _name + ">");
			}
		}

		return sb.toString();
	}

	@Override
	public String toString() {
		return toString("");
	}
}