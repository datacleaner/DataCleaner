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
package org.eobjects.datacleaner.widgets.visualization;

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
import java.util.TreeSet;

import org.apache.commons.collections15.Transformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.jung.graph.DirectedGraph;

public class VisualizeJobLayoutTransformer implements Transformer<Object, Point2D> {

    private static final Logger logger = LoggerFactory.getLogger(VisualizeJobLayoutTransformer.class);

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
    private final DirectedGraph<Object, VisualizeJobLink> _graph;
    private final Map<Object, Point> _points = new IdentityHashMap<Object, Point>();
    private final Map<Integer, Integer> _yCount = new HashMap<Integer, Integer>();
    private volatile boolean _transformed;

    public VisualizeJobLayoutTransformer(DirectedGraph<Object, VisualizeJobLink> graph) {
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
        logger.debug("Maximum prerequisite count: {}", maxPrerequisiteCount);

        final int x = maxPrerequisiteCount;
        for (Object vertex : vertices) {
            final Point point = createPoint(vertex, x);
            _points.put(vertex, point);

            createPrerequisitePoints(vertex, x);
        }
    }

    private void createPrerequisitePoints(final Object vertex, final int vertexX) {
        List<Object> prerequisites = getPrerequisites(vertex);

        // sort so that the longest trails will be plotted first
        Collections.sort(prerequisites, longestTrailComparator);

        for (Object prerequisiteVertex : prerequisites) {
            if (!_points.containsKey(prerequisiteVertex)) {
                final int x = vertexX - 1;
                final Point point = createPoint(prerequisiteVertex, x);
                _points.put(prerequisiteVertex, point);

                createPrerequisitePoints(prerequisiteVertex, x);
            }
        }
    }

    private List<Object> getEndpointVertices() {
        List<Object> result = new ArrayList<Object>();
        for (Object vertex : _graph.getVertices()) {
            Collection<VisualizeJobLink> outEdges = _graph.getOutEdges(vertex);
            if (outEdges == null || outEdges.isEmpty()) {
                result.add(vertex);
            }
        }
        return result;
    }

    private Point createPoint(final Object vertex, final int x) {
        Integer y = _yCount.get(x);
        if (y == null) {
            y = 0;
        } else {
            y++;
        }
        _yCount.put(x, y);

        if (logger.isDebugEnabled()) {
            logger.debug("Assigning coordinate ({},{}) to vertex {}", new Object[] { x, y, vertex });
        }

        return createPoint(x, y.intValue());
    }

    private Point createPoint(final int x, final int y) {
        if (x < 0 || y < 0) {
            throw new IllegalArgumentException("Negative coordinates are not allowed: x=" + x + ",y=" + y);
        }
        return new Point(x * X_STEP + X_OFFSET, y * Y_STEP + Y_OFFSET);
    }

    public Dimension getPreferredSize() {
        final int x;
        final int y;
        if (_yCount.isEmpty()) {
            x = 1;
            y = 1;
        } else {
            x = new TreeSet<Integer>(_yCount.keySet()).last();
            y = new TreeSet<Integer>(_yCount.values()).last();
        }
        final int w = X_OFFSET + (x + 1) * X_STEP;
        final int h = Y_OFFSET + (y + 1) * Y_STEP;
        return new Dimension(w, h);
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
        Collection<VisualizeJobLink> edges = _graph.getInEdges(vertex);
        if (edges == null || edges.isEmpty()) {
            return Collections.emptyList();
        }
        List<Object> result = new ArrayList<Object>();
        for (VisualizeJobLink edge : edges) {
            result.add(edge.getFrom());
        }
        return result;
    }

    private int getAccumulatedPrerequisiteCount(Object obj) {
        Collection<VisualizeJobLink> edges = _graph.getInEdges(obj);
        if (edges == null || edges.isEmpty()) {
            return 0;
        }
        int max = 0;
        for (VisualizeJobLink edge : edges) {
            assert edge.getTo() == obj;
            final Object from = edge.getFrom();
            if (obj == from) {
                // strange case where an edge is both going from and to the same
                // vertex.
                return max;
            }
            final int count = getAccumulatedPrerequisiteCount(from) + 1;
            max = Math.max(max, count);
        }
        return max;
    }
}
