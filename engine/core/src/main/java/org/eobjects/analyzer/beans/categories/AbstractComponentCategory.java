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
package org.eobjects.analyzer.beans.categories;

import org.eobjects.analyzer.beans.api.ComponentCategory;
import org.eobjects.analyzer.util.ReflectionUtils;

/**
 * Abstract implementation of {@link ComponentCategory}. This implementation
 * assumes that all instances of a category class are equal, which is also the
 * recommended approach.
 */
public abstract class AbstractComponentCategory implements ComponentCategory {

	private static final long serialVersionUID = 1L;

	@Override
	public String getName() {
		String simpleName = getClass().getSimpleName();
		if (simpleName.endsWith("Category")) {
			simpleName = simpleName.substring(0, simpleName.length() - "Category".length());
		}
		return ReflectionUtils.explodeCamelCase(simpleName, false);
	}

	@Override
	public final boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		return getClass().equals(obj.getClass());
	}

	@Override
	public final int hashCode() {
		return getClass().hashCode();
	}
	
	@Override
	public final String toString() {
		return getName();
	}
}
