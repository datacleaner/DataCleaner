/**
 * AnalyzerBeans
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
package org.eobjects.analyzer.util.convert;

import org.eobjects.analyzer.beans.api.Converter;

public class NullConverter implements Converter<Object> {

	@Override
	public Object fromString(Class<?> type, String serializedForm) {
		if (!"<null>".equals(serializedForm)) {
			throw new IllegalArgumentException("Null expected, but found: " + serializedForm);
		}
		return null;
	}

	@Override
	public String toString(Object instance) {
		if (null != instance) {
			throw new IllegalArgumentException("Null expected, but found: " + instance);
		}
		return "<null>";
	}

	@Override
	public boolean isConvertable(Class<?> type) {
		return null == type;
	}

	public boolean isNull(String serializedForm) {
		return "<null>".equals(serializedForm);
	}
}
