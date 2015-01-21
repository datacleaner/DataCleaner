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

import java.util.Collection;

/**
 * Represents a collection of values where lookup using containsValue(...) is
 * the preferred way of access. Typically the implementation will use some
 * caching mechanism for the contained values because getting all values would
 * mean loading a lot of objects into memory.
 * 
 * 
 * 
 * @param <E>
 */
public interface ReferenceValues<E> {

	public Collection<E> getValues();

	public boolean containsValue(E value);
}
