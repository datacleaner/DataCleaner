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
        _headElements = new ArrayList<HeadElement>();
        _bodyElements = new ArrayList<BodyElement>();
    }

    @Override
    public void initialize(HtmlRenderingContext context) {
        // do nothing
    }

    public SimpleHtmlFragment addBodyElement(String html) {
        return addBodyElement(new SimpleBodyElement(html));
    }

    public SimpleHtmlFragment addBodyElement(BodyElement bodyElement) {
        _bodyElements.add(bodyElement);
        return this;
    }

    public SimpleHtmlFragment addHeadElement(String html) {
        return addHeadElement(new SimpleHeadElement(html));
    }

    public SimpleHtmlFragment addHeadElement(HeadElement headElement) {
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
