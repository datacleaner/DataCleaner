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
package org.datacleaner.widgets.visualization;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;
import org.datacleaner.components.convert.ConvertToNumberTransformer;
import org.datacleaner.job.builder.AnalysisJobBuilder;

public class JobGraphMetadata {

    public static final String METADATA_PROPERTY_COORDINATES_X = "CoordinatesX";
    public static final String METADATA_PROPERTY_COORDINATES_Y = "CoordinatesY";

    public static Point getPointForTable(AnalysisJobBuilder analysisJobBuilder, Table table) {
        final String postFix = getTablePostFix(table);

        final Map<String, String> metadataProperties = analysisJobBuilder.getMetadataProperties();
        final String xString = metadataProperties.get(METADATA_PROPERTY_COORDINATES_X + postFix);
        final String yString = metadataProperties.get(METADATA_PROPERTY_COORDINATES_Y + postFix);
        final Number x = ConvertToNumberTransformer.transformValue(xString);
        final Number y = ConvertToNumberTransformer.transformValue(yString);
        if (x != null && y != null) {
            return new Point(x.intValue(), y.intValue());
        }
        return null;
    }

    public static void setPointForTable(AnalysisJobBuilder analysisJobBuilder, Table table, Number x, Number y) {
        final String postFix = getTablePostFix(table);

        final Map<String, String> metadataProperties = analysisJobBuilder.getMetadataProperties();
        metadataProperties.put(JobGraphMetadata.METADATA_PROPERTY_COORDINATES_X + postFix, "" + x.intValue());
        metadataProperties.put(JobGraphMetadata.METADATA_PROPERTY_COORDINATES_Y + postFix, "" + y.intValue());
    }

    private static String getTablePostFix(Table table) {
        final String postFix;
        final Schema schema = table.getSchema();
        if (schema == null) {
            postFix = "." + table.getName();
        } else {
            postFix = "." + schema.getName() + "." + table.getName();
        }
        return postFix;
    }

    public static Map<String, String> createMetadataProperties(int x, int y) {
        final Map<String, String> metadataProperties = new HashMap<>();
        metadataProperties.put(JobGraphMetadata.METADATA_PROPERTY_COORDINATES_X, "" + x);
        metadataProperties.put(JobGraphMetadata.METADATA_PROPERTY_COORDINATES_Y, "" + y);
        return metadataProperties;
    }

    public static Map<String, String> createMetadataProperties(Point2D p) {
        if (p == null) {
            return null;
        }
        final int x = (int) p.getX();
        final int y = (int) p.getY();
        return createMetadataProperties(x, y);
    }
}
