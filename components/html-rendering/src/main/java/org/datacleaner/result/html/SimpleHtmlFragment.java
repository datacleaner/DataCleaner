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

import java.util.ArrayList;
import java.util.List;

/**
 * Simple {@link HtmlFragment} implementation which function just as a simple
 * collection of {@link HeadElement}s and {@link BodyElement}s.
 */
public class SimpleHtmlFragment implements HtmlFragment {

    private final List<HeadElement> _headElements;
    private final List<BodyElement> _bodyElements;

    public SimpleHtmlFragment() {
        _headElements = new ArrayList<>();
        _bodyElements = new ArrayList<>();
    }

    @Override
    public void initialize(final HtmlRenderingContext context) {
        // do nothing
    }

    public SimpleHtmlFragment addBodyElement(final String html) {
        return addBodyElement(new SimpleBodyElement(html));
    }

    public SimpleHtmlFragment addBodyElement(final BodyElement bodyElement) {
        _bodyElements.add(bodyElement);
        return this;
    }

    public SimpleHtmlFragment addHeadElement(final String html) {
        return addHeadElement(new SimpleHeadElement(html));
    }

    public SimpleHtmlFragment addHeadElement(final HeadElement headElement) {
        _headElements.add(headElement);
        return this;
    }

    @Override
    public List<HeadElement> getHeadElements() {
        return _headElements;
    }

    @Override
    public List<BodyElement> getBodyElements() {
        return _bodyElements;
    }

    @Override
    public String toString() {
        return "SimpleHtmlFragment[headElements=" + _headElements.size() + ",bodyElements=" + _bodyElements.size()
                + "]";
    }
}
