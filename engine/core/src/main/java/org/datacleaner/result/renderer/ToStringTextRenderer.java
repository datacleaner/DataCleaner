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

import java.lang.reflect.Method;

import org.datacleaner.beans.api.Renderer;
import org.datacleaner.beans.api.RendererBean;
import org.datacleaner.beans.api.RendererPrecedence;
import org.datacleaner.result.AnalyzerResult;

/**
 * A very simple renderer that "renders" the toString() method of results, if a specialized toString() method is available.
 * 
 * Mostly used for testing (or result types that implement a meaningful
 * toString() method.
 */
@RendererBean(TextRenderingFormat.class)
public class ToStringTextRenderer implements Renderer<AnalyzerResult, String> {

    @Override
    public RendererPrecedence getPrecedence(AnalyzerResult renderable) {
        try {
            // only apply to classes that has a specialized toString() method.
            final Method toStringMethod = renderable.getClass().getDeclaredMethod("toString");
            if (toStringMethod != null) {
                return RendererPrecedence.LOW;
            }
        } catch (Exception e) {
            // ignore
        }
        return RendererPrecedence.NOT_CAPABLE;
    }

    @Override
    public String render(AnalyzerResult result) {
        return result.toString();
    }

}
