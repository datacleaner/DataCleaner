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

/**
 * The obligatory head element which includes required stylesheets, scripts etc.
 */
public final class BaseHeadElement implements HeadElement {

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
    public BaseHeadElement(String resourcesDirectory) {
        _resourcesDirectory = resourcesDirectory;
    }

    @Override
    public String toHtml(HtmlRenderingContext context) {
        // Here it would be logical to use the HTTP base tag, but unfortunately JQuery UI tabs doesn't work with that
        return "<link rel=\"shortcut icon\" href=\"" + _resourcesDirectory + "/analysis-result-icon.png\" />\n"
                + "<link rel=\"stylesheet\" type=\"text/css\" href=\"https://cdnjs.cloudflare.com/ajax/libs/jqueryui/1.12.0/themes/base/jquery-ui.css\" />\n"
                + "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + _resourcesDirectory + "/analysis-result.css?load=script\" />\n"
                + "<script type=\"text/javascript\">//<![CDATA[\nvar analysisResult = {};\n"
                + "requirejs = {\n"
                + "    baseUrl: 'https://cdnjs.cloudflare.com/ajax/libs/',\n"
                + "    shim: {\n"
                + "        'jquery-ui': {\n"
                + "            deps: ['jquery']\n"
                + "        },\n"
                + "        'jquery.flot': {\n"
                + "            deps: ['jquery'],\n"
                + "            exports: '$.plot'\n"
                + "        },\n"
                + "        'jquery.flot.selection': {\n"
                + "            deps: ['jquery.flot']\n"
                + "        }\n"
                + "    },\n"
                + "    paths: {\n"
                + "        'jquery': 'jquery/2.2.4/jquery.min',\n"
                + "        'jquery-ui': 'jqueryui/1.12.0/jquery-ui.min',\n"
                + "        'jquery.flot': 'flot/0.8.3/jquery.flot.min',\n"
                + "        'jquery.flot.selection': 'flot/0.8.3/jquery.flot.selection.min'\n"
                + "    }\n"
                + "};\n"
                + "//]]>\n</script>"
                + "<script data-main=\"" + _resourcesDirectory + "/analysis-result-v2.js\" type=\"text/javascript\""
                + "     src=\"https://cdnjs.cloudflare.com/ajax/libs/require.js/2.2.0/require.js\"></script>";
    }
}
