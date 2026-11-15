/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Free Software Foundation, Inc.
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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.datacleaner.metamodel.schema.Table;
import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.InputColumn;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.job.ComponentRequirement;
import org.datacleaner.job.CompoundComponentRequirement;
import org.datacleaner.job.FilterOutcome;
import org.datacleaner.job.HasFilterOutcomes;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.GraphUtils;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.LabelUtils;
import org.datacleaner.util.ReflectionUtils;
import org.datacleaner.util.WidgetUtils;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.renderers.EdgeLabelRenderer;

/**
 * Collection of {@link Function} (and {@link Predicate} and so on) instances to use in the {@link JobGraph}.
 */
public class JobGraphTransformers {

    public static final String USER_PREFERENCES_PROPERTY_SHOW_CANVAS_HINTS = "JobGraph.ShowCanvasHints";
    public static final String USER_PREFERENCES_PROPERTY_FONT_SIZE_FACTOR = "JobGraph.FontSizeFactor";
    public static final String USER_PREFERENCES_PROPERTY_EDGE_STYLE = "JobGraph.LineStyle";

    public static final String EDGE_STYLE_NAME_STRAIGHT = "straight";
    public static final String EDGE_STYLE_NAME_CURVED = "curved";
    public static final String EDGE_STYLE_NAME_ORTOGHONAL = "orthogonal";

    public static final Predicate<Context<Graph<Object, JobGraphLink>, JobGraphLink>> EDGE_ARROW_PREDICATE =
            (c -> true);
    public static final Function<JobGraphLink, String> EDGE_LABEL_TRANSFORMER = JobGraphLink::getLinkLabel;
    public static final Function<Context<Graph<Object, JobGraphLink>, JobGraphLink>, Shape> EDGE_ARROW_TRANSFORMER =
            input -> GraphUtils.ARROW_SHAPE;
    public static final Function<Context<Graph<Object, JobGraphLink>, JobGraphLink>, Number> EDGE_LABEL_CLOSENESS_TRANSFORMER =
            input -> 0.4d;
    public static final Function<Object, String> VERTEX_LABEL_TRANSFORMER = obj -> {
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
            final Class<?> cls = (Class<?>) obj;
            if (ReflectionUtils.is(cls, AnalyzerResult.class)) {
                return "Analyzer result";
            }
            return cls.getSimpleName();
        }
        return obj.toString();
    };

    /**
     * Vertex shape is fixed size, so it is precomputed to avoid per-vertex allocations on every paint.
     */
    public static final Function<Object, Shape> VERTEX_SHAPE_TRANSFORMER;

    static {
        final int vertexShapeSize = IconUtils.ICON_SIZE_LARGE;
        final int vertexShapeOffset = -vertexShapeSize / 2;
        VERTEX_SHAPE_TRANSFORMER = input -> new Rectangle(
                new Point(vertexShapeOffset, vertexShapeOffset), new Dimension(vertexShapeSize, vertexShapeSize));
    }

    private static final ImageManager imageManager = ImageManager.get();
    private final UserPreferences _userPreferences;
    private final Set<Object> _highlighedVertexes;
    private final DirectedGraph<Object, JobGraphLink> _graph;
    private final Font _normalFont;
    private final Font _boldFont;

    /**
     * Cache of vertex icons. Building icons (in particular the "unconfigured" error overlay) allocates a
     * BufferedImage per icon, which is far too expensive to do per vertex on every paint.
     */
    private final Map<String, Icon> _vertexIconCache = new ConcurrentHashMap<>();

    /**
     * Cached "wedge" edge shape, used for compound requirement links. Created lazily because it is only needed
     * for some graphs.
     */
    private Function<? super JobGraphLink, Shape> _compoundEdgeShapeTransformer;

    public JobGraphTransformers(DirectedGraph<Object, JobGraphLink> graph, final UserPreferences userPreferences,
            final Set<Object> highlighedVertexes) {
        _graph = graph;
        _userPreferences = userPreferences;
        _highlighedVertexes = highlighedVertexes;

        final float fontFactor;
        final String fontFactorString =
                userPreferences.getAdditionalProperties().get(USER_PREFERENCES_PROPERTY_FONT_SIZE_FACTOR);
        if (Strings.isNullOrEmpty(fontFactorString)) {
            fontFactor = 1.0f;
        } else {
            fontFactor = 1.0f * Float.parseFloat(fontFactorString);
        }

        _normalFont = font(WidgetUtils.FONT_SMALL, fontFactor);
        _boldFont = _normalFont.deriveFont(Font.BOLD);
    }

    private Font font(final Font font, final float fontFactor) {
        if (fontFactor == 1.0) {
            return font;
        }
        return font.deriveFont(font.getSize() * fontFactor);
    }

    public Function<? super JobGraphLink, Shape> getEdgeShapeTransformer() {
        final String edgeStyle = _userPreferences.getAdditionalProperties().get(USER_PREFERENCES_PROPERTY_EDGE_STYLE);
        final Function<? super JobGraphLink, Shape> baseTransformer = getBaseEdgeShapeTransformer(edgeStyle);

        return input -> {
            final Shape result = baseTransformer.apply(input);
            if (isCompoundRequirementLink(input)) {
                // make a double link (actually a wedge, but close
                // enough) to show that there are more than one filter
                // outcome coming from this source
                if (_compoundEdgeShapeTransformer == null) {
                    _compoundEdgeShapeTransformer = EdgeShape.wedge(_graph, 10);
                }
                return _compoundEdgeShapeTransformer.apply(input);
            }
            return result;
        };

    }

    protected boolean isCompoundRequirementLink(final JobGraphLink link) {
        final ComponentRequirement req = link.getRequirement();
        final Object from = link.getFrom();
        if (req instanceof CompoundComponentRequirement && from instanceof HasFilterOutcomes) {
            if (((CompoundComponentRequirement) req).hasMultipleRequirementsFrom((HasFilterOutcomes) from)) {
                return true;
            }
        }
        return false;
    }

    private Function<? super JobGraphLink, Shape> getBaseEdgeShapeTransformer(final String edgeStyle) {
        if (edgeStyle == null) {
            return EdgeShape.quadCurve(_graph);
        }
        switch (edgeStyle) {
        case EDGE_STYLE_NAME_STRAIGHT:
            return EdgeShape.line(_graph);
        case EDGE_STYLE_NAME_CURVED:
            return EdgeShape.quadCurve(_graph);
        case EDGE_STYLE_NAME_ORTOGHONAL:
            return EdgeShape.orthogonal(_graph);
        default:
            return EdgeShape.quadCurve(_graph);
        }
    }

    public final EdgeLabelRenderer getEdgeLabelRenderer() {
        return new EdgeLabelRenderer() {
            @Override
            public boolean isRotateEdgeLabels() {
                return true;
            }

            @Override
            public void setRotateEdgeLabels(final boolean state) {
            }

            @Override
            public <T> Component getEdgeLabelRendererComponent(final JComponent vv, final Object value, final Font font,
                    final boolean isSelected, final T edge) {
                final String labelText;
                final String iconPath;

                if (edge instanceof JobGraphLink) {
                    final JobGraphLink link = (JobGraphLink) edge;
                    if (link.getOutputDataStream() != null) {
                        labelText = link.getLinkLabel();
                        iconPath = IconUtils.OUTPUT_DATA_STREAM_PATH;
                    } else if (isCompoundRequirementLink(link)) {
                        final HasFilterOutcomes from = (HasFilterOutcomes) link.getFrom();
                        final CompoundComponentRequirement req = (CompoundComponentRequirement) link.getRequirement();
                        final Set<FilterOutcome> outcomesFrom = req.getOutcomesFrom(from);
                        labelText = new CompoundComponentRequirement(outcomesFrom).getSimpleName();
                        iconPath = IconUtils.FILTER_OUTCOME_PATH;
                    } else {
                        labelText = link.getLinkLabel();
                        iconPath = IconUtils.FILTER_OUTCOME_PATH;
                    }
                } else {
                    labelText = value + "";
                    iconPath = IconUtils.FILTER_OUTCOME_PATH;
                }

                final Icon icon = imageManager.getImageIcon(iconPath, IconUtils.ICON_SIZE_SMALL);
                final JLabel label = new JLabel(labelText, icon, JLabel.LEFT);
                label.setFont(_normalFont);
                return label;
            }
        };
    }

    public Function<Object, Font> getVertexFontTransformer() {
        return vertex -> {
            if (_highlighedVertexes.contains(vertex)) {
                return _boldFont;
            }
            return _normalFont;
        };
    }

    /**
     * Provides the vertex icon transformer. Icons are cached per descriptor and configuration state, because
     * building them (notably the "unconfigured" error overlay) allocates a BufferedImage per icon - much too
     * expensive to do for every vertex on every paint.
     */
    public Function<Object, Icon> getVertexIconTransformer() {
        return obj -> {
            if (obj == JobGraph.MORE_COLUMNS_VERTEX || obj instanceof InputColumn) {
                return imageManager.getIcon(IconUtils.MODEL_COLUMN, IconUtils.ICON_SIZE_MEDIUM);
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
                final String cacheKey =
                        descriptor.getComponentClass().getName() + "#" + configured + "#" + IconUtils.ICON_SIZE_LARGE;
                return _vertexIconCache.computeIfAbsent(cacheKey,
                        key -> IconUtils.getDescriptorIcon(descriptor, configured, IconUtils.ICON_SIZE_LARGE));
            }
            if (obj instanceof FilterOutcome) {
                return imageManager.getImageIcon(IconUtils.FILTER_OUTCOME_PATH, IconUtils.ICON_SIZE_MEDIUM);
            }
            if (obj instanceof Table) {
                return imageManager.getIcon(IconUtils.MODEL_TABLE, IconUtils.ICON_SIZE_LARGE);
            }
            if (obj instanceof Class) {
                final Class<?> cls = (Class<?>) obj;
                if (ReflectionUtils.is(cls, AnalyzerResult.class)) {
                    return imageManager.getImageIcon(IconUtils.MODEL_RESULT, IconUtils.ICON_SIZE_LARGE);
                }
            }
            return imageManager.getImageIcon(IconUtils.STATUS_ERROR, IconUtils.ICON_SIZE_LARGE);
        };
    }
}
