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

import org.datacleaner.job.AnalysisJob;

import com.google.common.base.Strings;

/**
 * Represents commonly referenced system properties which AnalyzerBeans makes
 * use of.
 */
public class SystemProperties {

    /**
     * Determines if the select clause of queries in AnalyzerBeans should be
     * optimized. If set to "true", AnalyzerBeans may disregard columns set in a
     * {@link AnalysisJob} that are not consumed by any component in the job.
     */
    public static final String QUERY_SELECTCLAUSE_OPTIMIZE = "analyzerbeans.query.selectclause.optimize";

    /**
     * Gets a system property string, or a replacement value if the property is
     * null or blank.
     * 
     * @param key
     * @param valueIfNull
     * @return
     */
    public static String getString(String key, String valueIfNull) {
        String value = System.getProperty(key);
        if (Strings.isNullOrEmpty(value)) {
            return valueIfNull;
        }
        return value;
    }

    /**
     * Gets a system property long, or a replacement value if the property is
     * null or blank or not parseable
     * 
     * @param key
     * @param valueIfNullOrNotParseable
     * @return
     */
    public static long getLong(String key, long valueIfNullOrNotParseable) {
        String value = System.getProperty(key);
        if (Strings.isNullOrEmpty(value)) {
            return valueIfNullOrNotParseable;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return valueIfNullOrNotParseable;
        }
    }

    /**
     * Gets a system property int, or a replacement value if the property is
     * null or blank or not parseable
     * 
     * @param key
     * @param valueIfNullOrNotParseable
     * @return
     */
    public static long getInt(String key, int valueIfNullOrNotParseable) {
        String value = System.getProperty(key);
        if (Strings.isNullOrEmpty(value)) {
            return valueIfNullOrNotParseable;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return valueIfNullOrNotParseable;
        }
    }

    /**
     * Gets a system property boolean, or a replacement value if the property is
     * null or blank or not parseable as a boolean.
     * 
     * @param key
     * @param valueIfNull
     * @return
     */
    public static boolean getBoolean(String key, boolean valueIfNull) {
        String value = System.getProperty(key);
        if (Strings.isNullOrEmpty(value)) {
            return valueIfNull;
        }

        value = value.trim().toLowerCase();

        if ("true".equals(value)) {
            return true;
        } else if ("false".equals(value)) {
            return false;
        }

        return valueIfNull;
    }
}
