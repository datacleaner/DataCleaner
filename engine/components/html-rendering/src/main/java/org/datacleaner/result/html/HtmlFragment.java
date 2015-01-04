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

import java.util.List;

/**
 * Represents a fragment of HTML to be inserted into a HTML page.
 */
public interface HtmlFragment {

    /**
     * Initializes the {@link HtmlFragment}. This happens immediately before
     * invoking {@link #getHeadElements()} and {@link #getBodyElements()}
     * 
     * @param context
     */
    public void initialize(HtmlRenderingContext context);

    /**
     * Gets elements to be inserted into the <head> section of the HTML.
     * 
     * @param context
     * @return
     */
    public List<HeadElement> getHeadElements();

    /**
     * Gets elements to be inserted into the <body> section of the HTML.
     * 
     * @return
     */
    public List<BodyElement> getBodyElements();
}
