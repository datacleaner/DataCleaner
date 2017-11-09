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
package org.datacleaner.components.fillpattern;

import org.apache.metamodel.util.HasName;
import org.datacleaner.util.LabelUtils;

public enum InspectionType implements HasName {

    NULL_BLANK_OR_FILLED("Null, blank or filled"),

    NULL_OR_FILLED("Null or filled"),

    DISTINCT_VALUES("Distinct values");

    private String _name;

    private InspectionType(String name) {
        _name = name;
    }

    public String getName() {
        return _name;
    }

    public Object inspect(Object value) {
        if (value == null) {
            return LabelUtils.NULL_LABEL;
        }
        final boolean isBlank = value instanceof String && ((String) value).trim().isEmpty();
        switch (this) {
        case NULL_OR_FILLED:
            return FillPatternAnalyzer.FILLED_LABEL;
        case NULL_BLANK_OR_FILLED:
            if (isBlank) {
                return LabelUtils.BLANK_LABEL;
            }
            return FillPatternAnalyzer.FILLED_LABEL;
        case DISTINCT_VALUES:
            if (isBlank) {
                return LabelUtils.BLANK_LABEL;
            }
            return value;
        default:
            throw new UnsupportedOperationException("Unsupported inspection type: " + this);
        }
    }
}
