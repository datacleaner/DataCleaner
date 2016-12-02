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

import java.net.URI;

/**
 * The obligatory head element which includes required stylesheets, scripts etc.
 */
public final class BaseHeadElement implements HeadElement {

    public static final String CDNJS_URL = "https://cdnjs.cloudflare.com/ajax/libs/";
    private final String _resourcesDirectory;

    /**
     * Constructs a {@link BaseHeadElement} with the default (hosted) resources
     * directory.
     */
    public BaseHeadElement() {
        this("http://eobjects.org/resources/datacleaner-html-rendering");
    }

    /**
     * Constructs a {@link BaseHeadElement}.
     *
     * @param resourcesDirectory
     */
    public BaseHeadElement(final String resourcesDirectory) {
        _resourcesDirectory = resourcesDirectory;
    }

    @Override
    public String toHtml(final HtmlRenderingContext context) {
        String externalLibs = _resourcesDirectory + '/';

        // If it is from an external server, a CDN is preferred.
        if (URI.create(_resourcesDirectory).isAbsolute()) {
            externalLibs = CDNJS_URL;
        }

        // Here it would be logical to use the HTTP base tag, but unfortunately JQuery UI tabs doesn't work with that
        return "<link rel=\"shortcut icon\" href=\"" + _resourcesDirectory + "/analysis-result-icon.png\" />\n"
                + "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + externalLibs
                + "jqueryui/1.12.0/themes/base/jquery-ui.css\" />\n"
                + "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + _resourcesDirectory
                + "/analysis-result.css?load=script\" />\n"
                + "<script type=\"text/javascript\">//<![CDATA[\nvar analysisResult = {};\n" + "requirejs = {\n"
                + "    baseUrl: '" + externalLibs + "',\n" + "    shim: {\n" + "        'jquery-ui': {\n"
                + "            deps: ['jquery']\n" + "        },\n" + "        'jquery.flot': {\n"
                + "            deps: ['jquery'],\n" + "            exports: '$.plot'\n" + "        },\n"
                + "        'jquery.flot.selection': {\n" + "            deps: ['jquery.flot']\n" + "        }\n"
                + "    },\n" + "    paths: {\n" + "        'jquery': 'jquery/2.2.4/jquery.min',\n"
                + "        'jquery-ui': 'jqueryui/1.12.0/jquery-ui.min',\n"
                + "        'excanvas': 'flot/0.8.3/excanvas.min',\n"
                + "        'jquery.colorhelpers': 'flot/0.8.3/jquery.colorhelpers.min',\n"
                + "        'jquery.flot.canvas': 'flot/0.8.3/jquery.flot.canvas.min',\n"
                + "        'jquery.flot.categories': 'flot/0.8.3/jquery.flot.categories.min',\n"
                + "        'jquery.flot.crosshair': 'flot/0.8.3/jquery.flot.crosshair.min',\n"
                + "        'jquery.flot.errorbars': 'flot/0.8.3/jquery.flot.errorbars.min',\n"
                + "        'jquery.flot.fillbetween': 'flot/0.8.3/jquery.flot.fillbetween.min',\n"
                + "        'jquery.flot.image': 'flot/0.8.3/jquery.flot.image.min',\n"
                + "        'jquery.flot': 'flot/0.8.3/jquery.flot.min',\n"
                + "        'jquery.flot.navigate': 'flot/0.8.3/jquery.flot.navigate.min',\n"
                + "        'jquery.flot.pie': 'flot/0.8.3/jquery.flot.pie.min',\n"
                + "        'jquery.flot.resize': 'flot/0.8.3/jquery.flot.resize.min',\n"
                + "        'jquery.flot.selection': 'flot/0.8.3/jquery.flot.selection.min',\n"
                + "        'jquery.flot.stack': 'flot/0.8.3/jquery.flot.stack.min',\n"
                + "        'jquery.flot.symbol': 'flot/0.8.3/jquery.flot.symbol.min',\n"
                + "        'jquery.flot.threshold': 'flot/0.8.3/jquery.flot.threshold.min',\n"
                + "        'jquery.flot.time': 'flot/0.8.3/jquery.flot.time.min'\n" + "    }\n" + "};\n"
                + "//]]>\n</script>" + "<script type=\"text/javascript\"" + "     src=\"" + externalLibs
                + "require.js/2.2.0/require.js\"></script>" + "<script type=\"text/javascript\"" + "     src=\""
                + _resourcesDirectory + "/analysis-result-v2.js\"></script>";
    }
}
