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
package dk.eobjects.datacleaner.catalog;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import dk.eobjects.metamodel.util.FileHelper;

public class NamedRegex implements Serializable {

	private static final long serialVersionUID = 6546221842231307672L;
	private String _name;
	private String _expression;

	public NamedRegex() {
	}

	public NamedRegex(String name, String expression) {
		this();
		setName(name);
		setExpression(expression);
	}

	public String getName() {
		return _name;
	}

	public String getExpression() {
		return _expression;
	}

	public NamedRegex setName(String name) {
		_name = name;
		return this;
	}

	public NamedRegex setExpression(String expression) {
		_expression = expression;
		return this;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("name", _name).append("expression", _expression)
				.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof NamedRegex) {
			NamedRegex that = (NamedRegex) obj;
			return new EqualsBuilder().append(this._name, that._name).append(
					this._expression, that._expression).isEquals();
		}
		return false;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(_name).append(_expression)
				.toHashCode();
	}

	public static List<NamedRegex> loadFromFile(File file) {
		List<NamedRegex> result = new ArrayList<NamedRegex>();
		BufferedReader reader = FileHelper.getBufferedReader(file);
		try {
			for (String line = reader.readLine(); line != null; line = reader
					.readLine()) {
				line = line.trim();
				if (!line.startsWith("#")) {
					int seperatorIndex = line.indexOf('=');
					String name = line.substring(0, seperatorIndex);
					String expression = line.substring(seperatorIndex + 1, line
							.length());
					result.add(new NamedRegex(name, expression));
				}
			}
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
		return result;
	}

	public static void saveToFile(List<NamedRegex> regexes, File file) {
		StringBuilder sb = new StringBuilder();
		sb.append("#Regex registrations for DataCleaner\n");
		if (regexes != null) {
			for (NamedRegex namedRegex : regexes) {
				sb.append(namedRegex.getName() + "="
						+ namedRegex.getExpression() + "\n");
			}
		}
		FileHelper.writeStringAsFile(file, sb.toString());
	}
}