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
package org.eobjects.analyzer.result.html;

import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.job.ComponentJob;
import org.eobjects.analyzer.result.AnalyzerResult;

/**
 * Represents the context of generating/rendering HTML results. This interface
 * provides access to utilities commonly used while generating HTML elements,
 * scripts etc.
 */
public interface HtmlRenderingContext {

    /**
     * Escapes a string to make it ready for safely inserting into HTML
     * 
     * @param str
     * @return
     */
    public String escapeHtml(String str);

    /**
     * Escapes a string to make it ready for safely inserting into JSON
     * 
     * @param str
     * @return
     */
    public String escapeJson(String str);

    /**
     * Generates a new unique element ID for this rendering context.
     * 
     * @return
     */
    public String createElementId();

    /**
     * Gets the component job (if determinable), typically an
     * {@link AnalyzerJob}, which generated the currently rendered
     * {@link AnalyzerResult}. This method may return null if eg. a job is not
     * determinable by the orchestrating code.
     */
    public ComponentJob getComponentJob();
}
