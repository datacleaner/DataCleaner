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
package org.eobjects.datacleaner.util;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Stroke;
import java.util.Arrays;

import org.apache.commons.collections15.Transformer;
import org.apache.metamodel.util.HasName;

import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.renderers.DefaultEdgeLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.DefaultVertexLabelRenderer;

/**
 * Contains features related to layout and styling of JUNG based graphs.
 */
public class GraphUtils {

    private static final BasicStroke stroke = new BasicStroke(1.2f);

    private GraphUtils() {
        // prevent instantiation
    }

    public static <V, E> void applyStyles(VisualizationViewer<V, E> visualizationViewer) {
        final RenderContext<V, E> renderContext = visualizationViewer.getRenderContext();

        renderContext.setEdgeLabelRenderer(new DefaultEdgeLabelRenderer(WidgetUtils.BG_COLOR_BLUE_MEDIUM, false));
        renderContext.setEdgeStrokeTransformer(new Transformer<E, Stroke>() {
            @Override
            public Stroke transform(E input) {
                return stroke;
            }
        });
        renderContext.setEdgeDrawPaintTransformer(new Transformer<E, Paint>() {
            @Override
            public Paint transform(E input) {
                return WidgetUtils.BG_COLOR_MEDIUM;
            }
        });
        renderContext.setVertexLabelRenderer(new DefaultVertexLabelRenderer(WidgetUtils.BG_COLOR_BLUE_MEDIUM));
        renderContext.setEdgeFontTransformer(GraphUtils.<E> createFontTransformer());
        renderContext.setVertexFontTransformer(GraphUtils.<V> createFontTransformer());

        final DefaultModalGraphMouse<Object, Integer> graphMouse = new DefaultModalGraphMouse<Object, Integer>();
        graphMouse.setMode(ModalGraphMouse.Mode.PICKING);
        visualizationViewer.setGraphMouse(graphMouse);
    }

    private static <E> Transformer<E, Font> createFontTransformer() {
        return new Transformer<E, Font>() {
            @Override
            public Font transform(E input) {
                final Font defaultFont = WidgetUtils.FONT_SMALL;
                if (input == null) {
                    return defaultFont;
                }

                final String str;
                if (input instanceof HasName) {
                    str = ((HasName) input).getName();
                } else if (input instanceof Object[]) {
                    str = Arrays.toString((Object[])input);
                } else {
                    str = input.toString();
                }

                if (defaultFont.canDisplayUpTo(str) == -1) {
                    return defaultFont;
                }
                final Font findCompatibleFont = WidgetUtils.findCompatibleFont(str, WidgetUtils.FONT_SMALL);
                return findCompatibleFont.deriveFont(WidgetUtils.FONT_SIZE_SMALL);
            }
        };
    }
}
