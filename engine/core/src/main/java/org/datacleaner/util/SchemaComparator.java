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

import java.io.Serializable;
import java.util.Comparator;

import org.apache.metamodel.MetaModelHelper;
import org.apache.metamodel.schema.Schema;

/**
 * Comparator of schemas that will put information schemas in the top
 */
public class SchemaComparator implements Comparator<Schema>, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     *
     * @param schema
     * @return
     *
     * @deprecated use {@link MetaModelHelper#isInformationSchema(Schema)}
     *             instead
     */
    @Deprecated
    public static boolean isInformationSchema(final Schema schema) {
        return MetaModelHelper.isInformationSchema(schema);
    }

    @Override
    public int compare(final Schema o1, final Schema o2) {
        if (o1 == null) {
            return -1;
        }
        if (o2 == null) {
            return 1;
        }
        if (MetaModelHelper.isInformationSchema(o1)) {
            return -1;
        }
        if (MetaModelHelper.isInformationSchema(o2)) {
            return 1;
        }
        return o1.compareTo(o2);
    }

}
