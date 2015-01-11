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
package org.datacleaner.result.renderer;

import org.datacleaner.result.html.BodyElement;
import org.datacleaner.result.html.HtmlRenderingContext;

/**
 * A {@link BodyElement} which prints a header. Useful for subdivisioning
 * (sectioning) the result rendering.
 */
public class SectionHeaderBodyElement implements BodyElement {

    private final String _header;

    public SectionHeaderBodyElement(String header) {
        _header = header;
    }

    @Override
    public String toHtml(HtmlRenderingContext context) {
        return "<h3>" + context.escapeHtml(_header) + "</h3>";
    }

}
