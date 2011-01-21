/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.collections15.Transformer;
import org.eobjects.analyzer.data.InputColumn;

import edu.uci.ics.jung.graph.DirectedGraph;

public class VisualizeJobLayoutTransformer implements Transformer<Object, Point2D> {

	private static final int X_STEP = 180;
	private static final int X_OFFSET = 60;
	private static final int Y_STEP = 60;
	private static final int Y_MAX = 6;
	private final DirectedGraph<Object, VisualizeJobLink> _graph;
	private final Map<Object, Point> _points = new IdentityHashMap<Object, Point>();
	private final Map<Integer, Integer> _yCount = new HashMap<Integer, Integer>();
	private final int _nonPhysicalSourceColumnsExtraXSteps;
	private int addedXSteps = 0;

	public VisualizeJobLayoutTransformer(DirectedGraph<Object, VisualizeJobLink> graph) {
		_graph = graph;
		final Collection<Object> vertices = _graph.getVertices();
		int sourceColumns = 0;
		for (Object obj : vertices) {
			if (isPhysicalSourceColumn(obj)) {
				sourceColumns++;
			}
		}
		_nonPhysicalSourceColumnsExtraXSteps = (sourceColumns / Y_MAX);

		for (Object obj : vertices) {
			// eager load all points to be able to deliver preferred size
			transform(obj);
		}
	}

	public Dimension getPreferredSize() {
		int w = 0;
		if (!_yCount.isEmpty()) {
			Integer x = new TreeSet<Integer>(_yCount.keySet()).last();
			w = X_OFFSET + (x + 1) * X_STEP;
		}
		int h = (Y_MAX + 1) * Y_STEP;
		return new Dimension(w, h);
	}

	private boolean isPhysicalSourceColumn(Object obj) {
		if (obj instanceof InputColumn) {
			if (((InputColumn<?>) obj).isPhysicalColumn()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Point2D transform(Object obj) {
		Point point = _points.get(obj);
		if (point == null) {
			int inboundEdgeCount = getInboundEdgeCount(obj);
			int x = inboundEdgeCount + addedXSteps;
			if (!isPhysicalSourceColumn(obj)) {
				x += _nonPhysicalSourceColumnsExtraXSteps;
			}

			Integer y = _yCount.get(x);
			if (y == null) {
				y = 0;
			}
			if (y >= Y_MAX) {
				addedXSteps++;
			}

			y++;
			_yCount.put(x, y);
			point = new Point(x * X_STEP + X_OFFSET, y * Y_STEP);

			_points.put(obj, point);
		}
		return point;
	}

	private int getInboundEdgeCount(Object obj) {
		Collection<VisualizeJobLink> edges = _graph.getInEdges(obj);
		if (edges == null || edges.isEmpty()) {
			return 0;
		}
		int max = 0;
		for (VisualizeJobLink edge : edges) {
			assert edge.getTo() == obj;
			Object from = edge.getFrom();
			int count = getInboundEdgeCount(from) + 1;
			max = Math.max(max, count);
		}
		return max;
	}
}
