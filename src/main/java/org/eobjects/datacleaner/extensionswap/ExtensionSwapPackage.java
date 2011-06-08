/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.extensionswap;

import org.eobjects.datacleaner.user.ExtensionPackage;

/**
 * Represents an {@link ExtensionPackage} in the online extension swap.
 * 
 * @author Kasper Sørensen
 */
public final class ExtensionSwapPackage {

	private final String _id;
	private final String _name;

	public ExtensionSwapPackage(String id, String name) {
		_id = id;
		_name = name;
	}

	public String getId() {
		return _id;
	}

	public String getName() {
		return _name;
	}
}
