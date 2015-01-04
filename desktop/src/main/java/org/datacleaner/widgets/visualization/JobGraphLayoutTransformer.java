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
import org.elasticsearch.common.collect.IdentityHashSet;
import org.datacleaner.beans.convert.ConvertToNumberTransformer;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.metadata.HasMetadataProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.jung.graph.DirectedGraph;

/**
 * Transformer that makes 2D points for each vertex in the graph.
 */
public class JobGraphLayoutTransformer implements Transformer<Object, Point2D> {

    private static final Logger logger = LoggerFactory.getLogger(JobGraphLayoutTransformer.class);

    private final Comparator<Object> longestTrailComparator = new Comparator<Object>() {
        @Override
        public int compare(Object o1, Object o2) {
            final int prerequisiteCount1 = getAccumulatedPrerequisiteCount(o1);
            final int prerequisiteCount2 = getAccumulatedPrerequisiteCount(o2);
            return prerequisiteCount2 - prerequisiteCount1;
        }
    };

    private static final int X_STEP = 160;
    private static final int X_OFFSET = 40;
    private static final int Y_STEP = 80;
    private static final int Y_OFFSET = 40;

    private final AnalysisJobBuilder _analysisJobBuilder;
    private final DirectedGraph<Object, JobGraphLink> _graph;
    private final Map<Object, Point> _points = new IdentityHashMap<Object, Point>();
    private final Map<Integer, Integer> _yCount = new HashMap<Integer, Integer>();
    private volatile boolean _transformed;

    public JobGraphLayoutTransformer(AnalysisJobBuilder analysisJobBuilder, DirectedGraph<Object, JobGraphLink> graph) {
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

        final int x = maxPrerequisiteCount;
        for (Object vertex : vertices) {
            final Point point = createPoint(vertex, x, true);
            if (point != null) {
                _points.put(vertex, point);
            }
        }
        for (Object vertex : vertices) {
            final Point point = createPoint(vertex, x, false);
            _points.put(vertex, point);

            createPrerequisitePoints(vertex, x);
        }
    }

    private void createPrerequisitePoints(final Object vertex, final int vertexX) {
        final List<Object> prerequisites = getPrerequisites(vertex);

        // sort so that the longest trails will be plotted first
        Collections.sort(prerequisites, longestTrailComparator);

        for (Object prerequisiteVertex : prerequisites) {
            if (!_points.containsKey(prerequisiteVertex)) {
                final int x = Math.max(0, vertexX - 1);
                final Point point = createPoint(prerequisiteVertex, x, false);
                _points.put(prerequisiteVertex, point);

                createPrerequisitePoints(prerequisiteVertex, x);
            }
        }
    }

    private List<Object> getEndpointVertices() {
        List<Object> result = new ArrayList<Object>();
        for (Object vertex : _graph.getVertices()) {
            Collection<JobGraphLink> outEdges = _graph.getOutEdges(vertex);
            if (outEdges == null || outEdges.isEmpty()) {
                result.add(vertex);
            }
        }
        return result;
    }

    private Point createPoint(final Object vertex, int xIndex, boolean onlyIfCoordinatesDefined) {
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

        return point;
    }

    private Point createPoint(final int x, final int y) {
        if (x < 0 || y < 0) {
            throw new IllegalArgumentException("Negative coordinates are not allowed: x=" + x + ",y=" + y);
        }
        return new Point(x * X_STEP + X_OFFSET, y * Y_STEP + Y_OFFSET);
    }

    @Override
    public Point2D transform(Object vertex) {
        Point point = _points.get(vertex);
        if (point == null) {
            logger.warn("Vertex {} has no assigned coordinate!", vertex);
            return new Point(0, 0);
        }
        _transformed = true;
        return point;
    }

    public boolean isTransformed() {
        return _transformed;
    }

    private List<Object> getPrerequisites(Object vertex) {
        Collection<JobGraphLink> edges = _graph.getInEdges(vertex);
        if (edges == null || edges.isEmpty()) {
            return Collections.emptyList();
        }
        List<Object> result = new ArrayList<Object>();
        for (JobGraphLink edge : edges) {
            result.add(edge.getFrom());
        }
        return result;
    }

    private int getAccumulatedPrerequisiteCount(Object obj) {
        final Set<JobGraphLink> visitedEdges = new IdentityHashSet<>();
        return getAccumulatedPrerequisiteCount(obj, visitedEdges);
    }

    private int getAccumulatedPrerequisiteCount(Object obj, Set<JobGraphLink> visitedEdges) {
        Collection<JobGraphLink> edges = _graph.getInEdges(obj);
        if (edges == null || edges.isEmpty()) {
            return 0;
        }
        int max = 0;
        for (JobGraphLink edge : edges) {
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
}
