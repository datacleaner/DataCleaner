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
        return "<script type=\"text/javascript\" src=\"" + _resourcesDirectory + "/analysis-result.js\"></script>\n"
                + "<link rel=\"shortcut icon\" href=\"" + _resourcesDirectory + "/analysis-result-icon.png\" />\n"
                + "<script type=\"text/javascript\">//<![CDATA[\n" + "  var analysisResult = {};\n" + "//]]>\n</script>";
    }

}
