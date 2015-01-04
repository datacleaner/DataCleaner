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
package org.datacleaner.beans.api;

import java.io.Serializable;

import org.apache.metamodel.util.HasName;

/**
 * Represents a category that a component can be applied to. Categories aid the
 * description of the components by building relationships through related
 * categories.
 */
public interface ComponentCategory extends Serializable, HasName {

	/**
	 * Gets the name of the category. The name is often used as the primary
	 * identifier of a category.
	 * 
	 * @return a string name
	 */
	@Override
	public String getName();
}
