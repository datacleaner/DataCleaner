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
package org.datacleaner.monitor.server.controllers.referencedata.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class StringPatternModel {
    public enum PatternType {
        STRING("string"), REGEX("regex"), REGEX_MATCH_ENTIRE_STRING("regex-match-entire-string");

        private final String _name;

        PatternType(final String name) {

            _name = name;
        }

        public String getName() {
            return _name;
        }
    }

    private final String _name;
    private final String _pattern;
    private final PatternType _patternType;

    @JsonCreator
    public StringPatternModel(@JsonProperty("name") final String name, @JsonProperty("pattern") final String pattern,
            @JsonProperty("patternType") final PatternType patternType) {
        _name = name;
        _pattern = pattern;
        _patternType = patternType;
    }

    public String getPattern() {
        return _pattern;
    }

    public PatternType getPatternType() {
        return _patternType;
    }

    public String getName() {
        return _name;
    }
}
