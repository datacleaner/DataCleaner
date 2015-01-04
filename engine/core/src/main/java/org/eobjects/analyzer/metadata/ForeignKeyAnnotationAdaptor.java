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
package org.eobjects.analyzer.metadata;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link MetadataAnnotationAdaptor} for {@link ForeignKey}.
 */
public class ForeignKeyAnnotationAdaptor implements MetadataAnnotationAdaptor<ForeignKey> {

    @Override
    public String getAnnotationName() {
        return "ForeignKey";
    }

    @Override
    public ForeignKey convertFromAnnotation(MetadataAnnotation annotation) {
        final Map<String, String> parameters = annotation.getParameters();
        final String datastoreName = parameters.get("ForeignDatastoreName");
        final String schemaName = parameters.get("ForeignSchemaName");
        final String tableName = parameters.get("ForeignTableName");
        final String columnName = parameters.get("ForeignColumnName");
        return new ForeignKey(datastoreName, schemaName, tableName, columnName);
    }

    @Override
    public MetadataAnnotation convertToAnnotation(ForeignKey object) {
        final Map<String, String> map = new HashMap<String, String>();
        map.put("ForeignDatastoreName", object.getForeignDatastoreName());
        map.put("ForeignSchemaName", object.getForeignSchemaName());
        map.put("ForeignTableName", object.getForeignTableName());
        map.put("ForeignColumnName", object.getForeignColumnName());
        return new MetadataAnnotationImpl(getAnnotationName(), map);
    }

}
