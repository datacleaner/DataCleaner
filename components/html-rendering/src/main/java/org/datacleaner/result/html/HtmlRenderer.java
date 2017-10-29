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

import org.datacleaner.api.Renderable;
import org.datacleaner.api.Renderer;
import org.datacleaner.api.RendererPrecedence;

/**
 * An extension of the renderer interface, useful for most simple HTML renderers.
 */
public abstract class HtmlRenderer<R extends Renderable> implements Renderer<R, HtmlFragment> {

    @Override
    public RendererPrecedence getPrecedence(R renderable) {
        return RendererPrecedence.MEDIUM;
    }

    @Override
    public HtmlFragment render(R result) {
        final SimpleHtmlFragment fragment = new SimpleHtmlFragment();
        return new HtmlFragment() {

            @Override
            public void initialize(HtmlRenderingContext context) {
                handleFragment(fragment, result, context);
            }

            @Override
            public List<HeadElement> getHeadElements() {
                return fragment.getHeadElements();
            }

            @Override
            public List<BodyElement> getBodyElements() {
                return fragment.getBodyElements();
            }
        };
    }

    protected abstract void handleFragment(SimpleHtmlFragment frag, R result, HtmlRenderingContext context);
}
