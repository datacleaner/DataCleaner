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

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.Transformer;
import org.apache.metamodel.schema.Table;
import org.datacleaner.components.convert.ConvertToNumberTransformer;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.metadata.HasMetadataProperties;
import org.datacleaner.util.IconUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import edu.uci.ics.jung.graph.DirectedGraph;

/**
 * Transformer that makes 2D points for each vertex in the graph.
 */
public class JobGraphLayoutTransformer implements Transformer<Object, Point2D> {

    private static final Logger logger = LoggerFactory.getLogger(JobGraphLayoutTransformer.class);
    private static final int X_STEP = 160;
    private static final int X_OFFSET = 40;
    private static final int Y_STEP = 80;
    private static final int Y_OFFSET = 40;
    private final AnalysisJobBuilder _analysisJobBuilder;
    private final DirectedGraph<Object, JobGraphLink> _graph;
    private final Comparator<Object> longestTrailComparator = (o1, o2) -> {
        final int prerequisiteCount1 = getAccumulatedPrerequisiteCount(o1);
        final int prerequisiteCount2 = getAccumulatedPrerequisiteCount(o2);
        return prerequisiteCount2 - prerequisiteCount1;
    };
    private final Map<Object, Point> _points = new IdentityHashMap<>();
    private final Map<Integer, Integer> _yCount = new HashMap<>();
    private volatile boolean _transformed;

    public JobGraphLayoutTransformer(final AnalysisJobBuilder analysisJobBuilder,
            final DirectedGraph<Object, JobGraphLink> graph) {
        _analysisJobBuilder = analysisJobBuilder;
        _graph = graph;
        createPoints();
        _transformed = false;
    }

    private void createPoints() {
        final List<Object> vertices = getEndpointVertices();
        if (vertices.isEmpty()) {
            return;
        }

        // sort so that the longest trails will be plotted first
        Collections.sort(vertices, longestTrailComparator);

        final int maxPrerequisiteCount = getAccumulatedPrerequisiteCount(vertices.get(0));
        logger.trace("Maximum prerequisite count: {}", maxPrerequisiteCount);

        for (final Object vertex : vertices) {
            final Point point = createPoint(vertex, maxPrerequisiteCount, true);
            if (point != null) {
                _points.put(vertex, point);
            }
        }
        for (final Object vertex : vertices) {
            if (!_points.containsKey(vertex)) {
                final Point point = createPoint(vertex, maxPrerequisiteCount, false);
                _points.put(vertex, point);
            }

            createPrerequisitePoints(vertex, maxPrerequisiteCount);
        }
    }

    private void createPrerequisitePoints(final Object vertex, final int vertexX) {
        final List<Object> prerequisites = getPrerequisites(vertex);

        // sort so that the longest trails will be plotted first
        Collections.sort(prerequisites, longestTrailComparator);

        for (final Object prerequisiteVertex : prerequisites) {
            if (!_points.containsKey(prerequisiteVertex)) {
                final int x = Math.max(0, vertexX - 1);
                final Point point = createPoint(prerequisiteVertex, x, false);
                _points.put(prerequisiteVertex, point);

                createPrerequisitePoints(prerequisiteVertex, x);
            }
        }
    }

    private List<Object> getEndpointVertices() {
        final List<Object> result = new ArrayList<>();
        for (final Object vertex : _graph.getVertices()) {
            final Collection<JobGraphLink> outEdges = _graph.getOutEdges(vertex);
            if (outEdges == null || outEdges.isEmpty()) {
                result.add(vertex);
            }
        }
        return result;
    }

    private Point createPoint(final Object vertex, int xIndex, final boolean onlyIfCoordinatesDefined) {
        Point point = null;
        final Map<String, String> metadataProperties;
        if (vertex instanceof HasMetadataProperties) {
            metadataProperties = ((HasMetadataProperties) vertex).getMetadataProperties();
            final String xString = metadataProperties.get(JobGraphMetadata.METADATA_PROPERTY_COORDINATES_X);
            final String yString = metadataProperties.get(JobGraphMetadata.METADATA_PROPERTY_COORDINATES_Y);
            final Number x = ConvertToNumberTransformer.transformValue(xString);
            final Number y = ConvertToNumberTransformer.transformValue(yString);
            if (x != null && y != null) {
                point = new Point(x.intValue(), y.intValue());
            }
        } else {
            metadataProperties = null;
        }

        if (point == null && vertex instanceof Table) {
            point = JobGraphMetadata.getPointForTable(_analysisJobBuilder, (Table) vertex);
        }

        if (onlyIfCoordinatesDefined && point == null) {
            // this means we are not interested in generating a point
            return null;
        }

        if (point != null) {
            // find out what the "xIndex" should be - which spot in the grid
            // would we want to occupy with this component.
            final int x = point.x;
            xIndex = x / X_STEP + x % X_STEP / X_OFFSET - 1;
            xIndex = Math.max(xIndex, 0);
        }

        Integer y = _yCount.get(xIndex);
        if (y == null) {
            y = 0;
        } else {
            y++;
        }
        _yCount.put(xIndex, y);

        if (logger.isTraceEnabled()) {
            logger.trace("Assigning coordinate ({},{}) to vertex {}", new Object[] { xIndex, y, vertex });
        }

        if (point == null) {
            point = createPoint(xIndex, y.intValue());
        }

        if (metadataProperties != null) {
            metadataProperties.put(JobGraphMetadata.METADATA_PROPERTY_COORDINATES_X, "" + point.x);
            metadataProperties.put(JobGraphMetadata.METADATA_PROPERTY_COORDINATES_Y, "" + point.y);
        }

        if (vertex instanceof Table) {
            JobGraphMetadata.setPointForTable(_analysisJobBuilder, (Table) vertex, point.x, point.y);
        }

        if (point.x < 0 || point.y < 0) {
            // apply max(offset, value) to avoid location outside of canvas
            final int xValue = Math.max(X_OFFSET, point.x);
            final int yValue = Math.max(Y_OFFSET, point.y);
            point = new Point(xValue, yValue);
        }

        return point;
    }

    private Point createPoint(final int x, final int y) {
        if (x < 0 || y < 0) {
            throw new IllegalArgumentException("Negative coordinates are not allowed: x=" + x + ",y=" + y);
        }
        return new Point(x * X_STEP + X_OFFSET, y * Y_STEP + Y_OFFSET);
    }

    @Override
    public Point2D transform(final Object vertex) {
        Point point = _points.get(vertex);
        if (point == null) {
            logger.warn("Vertex {} has no assigned coordinate!", vertex);
            point = new Point(0, 0);
        }
        _transformed = true;
        return point;
    }

    public boolean isTransformed() {
        return _transformed;
    }

    private List<Object> getPrerequisites(final Object vertex) {
        final Collection<JobGraphLink> edges = _graph.getInEdges(vertex);
        if (edges == null || edges.isEmpty()) {
            return Collections.emptyList();
        }
        final List<Object> result = new ArrayList<>();
        for (final JobGraphLink edge : edges) {
            result.add(edge.getFrom());
        }
        return result;
    }

    private int getAccumulatedPrerequisiteCount(final Object obj) {
        final Set<JobGraphLink> visitedEdges = Sets.newSetFromMap(new IdentityHashMap<>());
        return getAccumulatedPrerequisiteCount(obj, visitedEdges);
    }

    private int getAccumulatedPrerequisiteCount(final Object obj, final Set<JobGraphLink> visitedEdges) {
        final Collection<JobGraphLink> edges = _graph.getInEdges(obj);
        if (edges == null || edges.isEmpty()) {
            return 0;
        }
        int max = 0;
        for (final JobGraphLink edge : edges) {
            final boolean added = visitedEdges.add(edge);
            if (added) {
                assert edge.getTo() == obj;
                final Object from = edge.getFrom();
                if (obj == from) {
                    // strange case where an edge is both going from and to the
                    // same
                    // vertex.
                    return max;
                }
                final int count = getAccumulatedPrerequisiteCount(from, visitedEdges) + 1;
                max = Math.max(max, count);
            }
        }
        return max;
    }

    public Dimension getPreferredSize() {
        // sensible minimum size of the canvas
        int maxX = 600;
        int maxY = 400;

        final Collection<Point> points = _points.values();
        for (final Point point : points) {
            maxX = Math.max(maxX, point.x + IconUtils.ICON_SIZE_LARGE);
            maxY = Math.max(maxY, point.y + IconUtils.ICON_SIZE_LARGE);
        }

        return new Dimension(maxX, maxY);
    }
}
