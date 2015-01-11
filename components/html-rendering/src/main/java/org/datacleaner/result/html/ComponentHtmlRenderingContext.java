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

import org.datacleaner.job.ComponentJob;

/**
 * {@link HtmlRenderingContext} which is aware of the component it is rendering
 * for. Wraps an underlying implementation for escaping and element id creation.
 */
public class ComponentHtmlRenderingContext implements HtmlRenderingContext {

    private final HtmlRenderingContext _delegate;
    private final ComponentJob _componentJob;

    public ComponentHtmlRenderingContext(HtmlRenderingContext delegate, ComponentJob componentJob) {
        _delegate = delegate;
        _componentJob = componentJob;
    }

    @Override
    public ComponentJob getComponentJob() {
        return _componentJob;
    }

    @Override
    public String escapeHtml(String str) {
        return _delegate.escapeHtml(str);
    }

    @Override
    public String escapeJson(String str) {
        return _delegate.escapeJson(str);
    }

    @Override
    public String createElementId() {
        return _delegate.createElementId();
    }
}
