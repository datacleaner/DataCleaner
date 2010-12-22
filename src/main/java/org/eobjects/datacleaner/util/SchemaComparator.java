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
package org.eobjects.datacleaner.util;

import java.util.Comparator;

import dk.eobjects.metamodel.schema.Schema;

/**
 * Comparator of schemas that will put information schemas in the top
 * 
 * @author Kasper Sørensen
 * 
 */
public class SchemaComparator implements Comparator<Schema> {

	@Override
	public int compare(Schema o1, Schema o2) {
		if (isInformationSchema(o1)) {
			return -1;
		}
		if (isInformationSchema(o2)) {
			return 1;
		}
		return o1.compareTo(o2);
	}

	public static boolean isInformationSchema(Schema schema) {
		String name = schema.getName();
		if (name == null) {
			return false;
		}
		return "information_schema".equals(name.toLowerCase());
	}

}
