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
package org.datacleaner.result.html;

import org.datacleaner.util.SystemProperties;

/**
 * Object responsible for locating the javascript URLs of Flot charts (used for most of the charts/graphs in HTML
 * rendered analyzer results).
 */
public class FlotChartLocator {

    public static final String SYSTEM_PROPERTY_FLOT_HOME = "org.datacleaner.flot.home";

    private static final String DEFAULT_FLOT_HOME = "http://cdnjs.cloudflare.com/ajax/libs/flot/0.8.3";

    /**
     * Gets the URL for the base flot library, typically named as "jquery.flot.min.js"
     */
    public static String getFlotBaseUrl() {
        return getFlotHome() + "/jquery.flot.min.js";
    }

    /**
     * Gets the URL for the flot plugin for pie charts, typically named as "jquery.flot.pie.min.js"
     */
    public static String getFlotPieUrl() {
        return getFlotHome() + "/jquery.flot.pie.js";
    }

    /**
     * Gets the URL for the flot plugin for selecting parts of the plot, typically named as
     * "jquery.flot.selection.min.js"
     */
    public static String getFlotSelectionUrl() {
        return getFlotHome() + "/jquery.flot.selection.js";
    }

    /**
     * Gets the URL for the flot plugin for additional point symbols, typically named as "jquery.flot.symbol.min.js"
     */
    public static String getFlotSymbolUrl() {
        return getFlotHome() + "/jquery.flot.symbol.js";
    }

    /**
     * Gets the URL for the flot plugin for "fill between" effect, typically named as "jquery.flot.fillbetween.min.js"
     */
    public static String getFlotFillBetweenUrl() {
        return getFlotHome() + "/jquery.flot.fillbetween.js";
    }

    /**
     * Gets the URL for the flot plugin for navigating (zoom/pan), typically named as "jquery.flot.navigate.min.js"
     */
    public static String getFlotNavigateUrl() {
        return getFlotHome() + "/jquery.flot.navigate.js";
    }

    /**
     * Gets the URL for the flot plugin for automatically resizing charts, typically named as
     * "jquery.flot.resize.min.js"
     */
    public static String getFlotResizeUrl() {
        return getFlotHome() + "/jquery.flot.resize.js";
    }

    /**
     * Gets the URL for the flot plugin for stacked charts, typically named as "jquery.flot.stack.min.js"
     */

    public static String getFlotStackUrl() {
        return getFlotHome() + "/jquery.flot.stack.js";
    }

    /**
     * Gets the URL for the flot plugin for threshold effect, typically named as "jquery.flot.threshold.min.js"
     */
    public static String getFlotThresholdUrl() {
        return getFlotHome() + "/jquery.flot.threshold.min.js";
    }

    /**
     * Gets the URL for the flot plugin for plotting categories instead of numbers on an axis, typically named as
     * "jquery.flot.categories.js"
     */
    public static String getFlotCategoriesUrl() {
        return getFlotHome() + "/jquery.flot.categories.min.js";
    }

    /**
     * Gets the home folder of all flot javascript files
     */
    protected static String getFlotHome() {
        return SystemProperties.getString(SYSTEM_PROPERTY_FLOT_HOME, DEFAULT_FLOT_HOME);
    }

    /**
     * Sets the home folder of all flot javascript files
     */
    public static void setFlotHome(String flotHome) {
        if (flotHome == null || flotHome.trim().isEmpty()) {
            System.clearProperty(SYSTEM_PROPERTY_FLOT_HOME);
        } else {
            final String propValue = flotHome.endsWith("/") ? flotHome.substring(0, flotHome.length() - 1) : flotHome;
            System.setProperty(SYSTEM_PROPERTY_FLOT_HOME, propValue);
        }
    }

    // private constructor - only static methods
    private FlotChartLocator() {
    }
}
