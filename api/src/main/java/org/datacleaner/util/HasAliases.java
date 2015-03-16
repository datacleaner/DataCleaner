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
package org.datacleaner.util;

import org.apache.metamodel.util.HasName;

/**
 * Represents any object (typically a {@link HasName} instance) which has
 * aliases also. Aliases are typically used either to provide fault tolerance or
 * to support renaming components or properties and retaining backwards
 * compatibility.
 */
public interface HasAliases {

	/**
	 * Gets alias names for this object
	 * 
	 * @return
	 */
	public String[] getAliases();
}
