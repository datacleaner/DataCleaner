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
package org.datacleaner.util;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Shape;
import java.awt.event.MouseWheelEvent;
import java.util.Arrays;

import org.apache.commons.collections15.Transformer;
import org.apache.metamodel.util.HasName;

import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.renderers.DefaultEdgeLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.DefaultVertexLabelRenderer;
import edu.uci.ics.jung.visualization.util.ArrowFactory;

/**
 * Contains features related to layout and styling of JUNG based graphs.
 */
public class GraphUtils {

    public static final Shape ARROW_SHAPE = ArrowFactory.getWedgeArrow(6, 6);
    private static final BasicStroke stroke = new BasicStroke(1.2f);

    private GraphUtils() {
        // prevent instantiation
    }

    public static <V, E> void applyStyles(final VisualizationViewer<V, E> visualizationViewer) {
        final RenderContext<V, E> renderContext = visualizationViewer.getRenderContext();

        renderContext.setEdgeLabelRenderer(new DefaultEdgeLabelRenderer(WidgetUtils.BG_COLOR_BLUE_MEDIUM, false));
        renderContext.setEdgeStrokeTransformer(input -> stroke);
        renderContext.setEdgeDrawPaintTransformer(input -> WidgetUtils.BG_COLOR_LESS_BRIGHT);
        renderContext.setVertexLabelRenderer(new DefaultVertexLabelRenderer(WidgetUtils.BG_COLOR_BLUE_MEDIUM));
        renderContext.setEdgeFontTransformer(GraphUtils.createFontTransformer());
        renderContext.setVertexFontTransformer(GraphUtils.createFontTransformer());

        final DefaultModalGraphMouse<Object, Integer> graphMouse = new DefaultModalGraphMouse<Object, Integer>() {
            @Override
            public void mouseWheelMoved(final MouseWheelEvent e) {
                // avoid zooming
                return;
            }
        };
        graphMouse.setMode(ModalGraphMouse.Mode.PICKING);
        graphMouse.setZoomAtMouse(false);

        visualizationViewer.setGraphMouse(graphMouse);
    }

    private static <E> Transformer<E, Font> createFontTransformer() {
        return input -> {
            final Font defaultFont = WidgetUtils.FONT_SMALL;
            if (input == null) {
                return defaultFont;
            }

            final String str;
            if (input instanceof HasName) {
                str = ((HasName) input).getName();
            } else if (input instanceof Object[]) {
                str = Arrays.toString((Object[]) input);
            } else {
                str = input.toString();
            }

            if (defaultFont.canDisplayUpTo(str) == -1) {
                return defaultFont;
            }
            final Font findCompatibleFont = WidgetUtils.findCompatibleFont(str, WidgetUtils.FONT_SMALL);
            return findCompatibleFont.deriveFont(WidgetUtils.FONT_SIZE_SMALL);
        };
    }
}
