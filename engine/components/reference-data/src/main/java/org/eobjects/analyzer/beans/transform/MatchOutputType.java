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
package org.eobjects.analyzer.beans.transform;

import org.apache.metamodel.util.HasName;

/**
 * Defines a type of matching output, which can be either true/false or the
 * matched input (if correct) or null (if incorrect).
 * 
 * 
 */
public enum MatchOutputType implements HasName {

	TRUE_FALSE("True or false", Boolean.class), INPUT_OR_NULL("Corrected value or null", String.class);

	private final String _name;
	private final Class<?> _outputClass;

	private MatchOutputType(String name, Class<?> outputClass) {
		_name = name;
		_outputClass = outputClass;
	}

	@Override
	public String getName() {
		return _name;
	}

	public Class<?> getOutputClass() {
		return _outputClass;
	}
}
