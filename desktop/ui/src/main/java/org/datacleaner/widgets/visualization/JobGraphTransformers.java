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
package org.datacleaner.widgets.visualization;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.TruePredicate;
import org.apache.metamodel.schema.Table;
import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.InputColumn;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.job.ComponentRequirement;
import org.datacleaner.job.FilterOutcome;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.GraphUtils;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.LabelUtils;
import org.datacleaner.util.ReflectionUtils;
import org.datacleaner.util.WidgetUtils;
import org.elasticsearch.common.base.Strings;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.renderers.EdgeLabelRenderer;

/**
 * Collection of {@link Transformer} (and {@link Predicate} and so on) instances
 * to use in the {@link JobGraph}.
 */
public class JobGraphTransformers {

    public static final String USER_PREFERENCES_PROPERTY_SHOW_CANVAS_HINTS = "JobGraph.ShowCanvasHints";
    public static final String USER_PREFERENCES_PROPERTY_FONT_SIZE_FACTOR = "JobGraph.FontSizeFactor";
    public static final String USER_PREFERENCES_PROPERTY_EDGE_STYLE = "JobGraph.LineStyle";

    public static final String EDGE_STYLE_NAME_STRAIGHT = "straight";
    public static final String EDGE_STYLE_NAME_CURVED = "curved";
    public static final String EDGE_STYLE_NAME_ORTOGHONAL = "orthogonal";

    private static final ImageManager imageManager = ImageManager.get();

    private final UserPreferences _userPreferences;
    private final Set<Object> _highlighedVertexes;

    private final Font _normalFont;
    private final Font _boldFont;

    public JobGraphTransformers(UserPreferences userPreferences, Set<Object> highlighedVertexes) {
        _userPreferences = userPreferences;
        _highlighedVertexes = highlighedVertexes;

        final float fontFactor;
        String fontFactorString = userPreferences.getAdditionalProperties().get(
                USER_PREFERENCES_PROPERTY_FONT_SIZE_FACTOR);
        if (Strings.isNullOrEmpty(fontFactorString)) {
            fontFactor = 1.0f;
        } else {
            fontFactor = 1.0f * Float.parseFloat(fontFactorString);
        }

        _normalFont = font(WidgetUtils.FONT_SMALL, fontFactor);
        _boldFont = _normalFont.deriveFont(Font.BOLD);
    }

    private Font font(Font font, float fontFactor) {
        if (fontFactor == 1.0) {
            return font;
        }
        return font.deriveFont(font.getSize() * fontFactor);
    }

    public static final Predicate<Context<Graph<Object, JobGraphLink>, JobGraphLink>> EDGE_ARROW_PREDICATE = TruePredicate
            .getInstance();;

    public static final Transformer<JobGraphLink, String> EDGE_LABEL_TRANSFORMER = new Transformer<JobGraphLink, String>() {
        @Override
        public String transform(JobGraphLink link) {
            final ComponentRequirement req = link.getRequirement();
            if (req == null) {
                return null;
            }
            return req.getSimpleName();
        }
    };

    public static final Transformer<Context<Graph<Object, JobGraphLink>, JobGraphLink>, Shape> EDGE_ARROW_TRANSFORMER = new Transformer<Context<Graph<Object, JobGraphLink>, JobGraphLink>, Shape>() {
        @Override
        public Shape transform(Context<Graph<Object, JobGraphLink>, JobGraphLink> input) {
            return GraphUtils.ARROW_SHAPE;
        }
    };

    public Transformer<Context<Graph<Object, JobGraphLink>, JobGraphLink>, Shape> getEdgeShapeTransformer() {
        final String edgeStyle = _userPreferences.getAdditionalProperties().get(USER_PREFERENCES_PROPERTY_EDGE_STYLE);
        if (edgeStyle == null) {
            return new EdgeShape.QuadCurve<>();
        }
        switch (edgeStyle) {
        case EDGE_STYLE_NAME_STRAIGHT:
            return new EdgeShape.Line<>();
        case EDGE_STYLE_NAME_CURVED:
            return new EdgeShape.QuadCurve<>();
        case EDGE_STYLE_NAME_ORTOGHONAL:
            return new EdgeShape.Orthogonal<>();
        }
        return new EdgeShape.QuadCurve<>();
    }

    public static final Transformer<Context<Graph<Object, JobGraphLink>, JobGraphLink>, Number> EDGE_LABEL_CLOSENESS_TRANSFORMER = new Transformer<Context<Graph<Object, JobGraphLink>, JobGraphLink>, Number>() {
        @Override
        public Number transform(Context<Graph<Object, JobGraphLink>, JobGraphLink> input) {
            return 0.4d;
        }
    };

    public final EdgeLabelRenderer getEdgeLabelRenderer() {
        return new EdgeLabelRenderer() {
            @Override
            public void setRotateEdgeLabels(boolean state) {
            }

            @Override
            public boolean isRotateEdgeLabels() {
                return true;
            }

            @Override
            public <T> Component getEdgeLabelRendererComponent(JComponent vv, Object value, Font font,
                    boolean isSelected, T edge) {
                final Icon icon = imageManager.getImageIcon(IconUtils.FILTER_OUTCOME_PATH, IconUtils.ICON_SIZE_SMALL);
                final JLabel label = new JLabel(value + "", icon, JLabel.LEFT);
                label.setFont(_normalFont);
                return label;
            }
        };
    }

    public static final Transformer<Object, Icon> VERTEX_ICON_TRANSFORMER = new Transformer<Object, Icon>() {
        @Override
        public Icon transform(Object obj) {
            if (obj == JobGraph.MORE_COLUMNS_VERTEX || obj instanceof InputColumn) {
                return imageManager.getImageIcon(IconUtils.MODEL_COLUMN, IconUtils.ICON_SIZE_MEDIUM);
            }
            if (obj instanceof ComponentBuilder) {
                final ComponentBuilder componentBuilder = (ComponentBuilder) obj;
                final ComponentDescriptor<?> descriptor = componentBuilder.getDescriptor();
                final boolean configured;
                if (componentBuilder.getInput().length == 0) {
                    configured = true;
                } else {
                    configured = componentBuilder.isConfigured(false);
                }
                return IconUtils.getDescriptorIcon(descriptor, configured, IconUtils.ICON_SIZE_LARGE);
            }
            if (obj instanceof FilterOutcome) {
                return imageManager.getImageIcon(IconUtils.FILTER_OUTCOME_PATH, IconUtils.ICON_SIZE_MEDIUM);
            }
            if (obj instanceof Table) {
                return imageManager.getImageIcon(IconUtils.MODEL_TABLE, IconUtils.ICON_SIZE_LARGE);
            }
            if (obj instanceof Class) {
                Class<?> cls = (Class<?>) obj;
                if (ReflectionUtils.is(cls, AnalyzerResult.class)) {
                    return imageManager.getImageIcon(IconUtils.MODEL_RESULT, IconUtils.ICON_SIZE_LARGE);
                }
            }
            return imageManager.getImageIcon(IconUtils.STATUS_ERROR);
        }
    };

    public static final Transformer<Object, String> VERTEX_LABEL_TRANSFORMER = new Transformer<Object, String>() {
        @Override
        public String transform(Object obj) {
            if (obj instanceof InputColumn) {
                return ((InputColumn<?>) obj).getName();
            }
            if (obj instanceof ComponentBuilder) {
                return LabelUtils.getLabel((ComponentBuilder) obj);
            }
            if (obj instanceof FilterOutcome) {
                return ((FilterOutcome) obj).getCategory().name();
            }
            if (obj instanceof Table) {
                return ((Table) obj).getName();
            }
            if (obj instanceof Class) {
                Class<?> cls = (Class<?>) obj;
                if (ReflectionUtils.is(cls, AnalyzerResult.class)) {
                    return "Analyzer result";
                }
                return cls.getSimpleName();
            }
            return obj.toString();
        }
    };

    public static final Transformer<Object, Shape> VERTEX_SHAPE_TRANSFORMER = new Transformer<Object, Shape>() {
        @Override
        public Shape transform(Object input) {
            int size = IconUtils.ICON_SIZE_LARGE;
            int offset = -size / 2;
            return new Rectangle(new Point(offset, offset), new Dimension(size, size));
        }
    };

    public Transformer<Object, Font> getVertexFontTransformer() {
        return new Transformer<Object, Font>() {
            @Override
            public Font transform(Object vertex) {
                if (_highlighedVertexes.contains(vertex)) {
                    return _boldFont;
                }
                return _normalFont;
            }
        };
    }
}
