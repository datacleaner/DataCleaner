/**
 * AnalyzerBeans
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

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringEscapeUtils;
import org.eobjects.analyzer.job.ComponentJob;

/**
 * Default implementation of {@link HtmlRenderingContext}
 */
public class DefaultHtmlRenderingContext implements HtmlRenderingContext {

    private final AtomicInteger _elementCounter;

    public DefaultHtmlRenderingContext() {
        _elementCounter = new AtomicInteger(0);
    }

    @Override
    public String escapeHtml(String str) {
        return StringEscapeUtils.escapeHtml(str);
    }

    @Override
    public String escapeJson(String str) {
        return StringEscapeUtils.escapeJavaScript(str);
    }

    @Override
    public String createElementId() {
        return "reselem_" + _elementCounter.incrementAndGet();
    }

    @Override
    public ComponentJob getComponentJob() {
        // not implemented, use an overriding implementation to provide this.
        return null;
    }

}
