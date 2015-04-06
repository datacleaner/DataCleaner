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
package org.datacleaner.reference;

import java.io.Serializable;

import org.apache.metamodel.util.HasName;

/**
 * Abstraction over all reference data types in AnalyzerBeans
 * 
 * 
 */
public interface ReferenceData extends Serializable, HasName {

	/**
	 * Gets the name of the reference data item.
	 * 
	 * @return a String containing the name of this reference data item.
	 */
	@Override
	public String getName();

	/**
	 * Gets an optional description of the reference data item.
	 * 
	 * @return a String description, or null if no description is available.
	 */
	public String getDescription();

	/**
	 * Sets the description of the reference data item.
	 * 
	 * @param description
	 *            the new description of the reference data item.
	 */
	public void setDescription(String description);
}
