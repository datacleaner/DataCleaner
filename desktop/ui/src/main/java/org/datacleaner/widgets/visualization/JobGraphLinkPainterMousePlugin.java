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

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.jung.visualization.control.AbstractGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.EditingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.GraphMousePlugin;

/**
 * {@link GraphMousePlugin} inpsired by {@link EditingGraphMousePlugin} to
 * support the creation of new links between parts of a visualized DataCleaner
 * job.
 */
public class JobGraphLinkPainterMousePlugin extends AbstractGraphMousePlugin implements MouseListener,
        MouseMotionListener {

    private static final Logger logger = LoggerFactory.getLogger(JobGraphLinkPainterMousePlugin.class);

    private final JobGraphLinkPainter _linkPainter;
    private final JobGraphContext _graphContext;

    public JobGraphLinkPainterMousePlugin(JobGraphLinkPainter linkPainter, JobGraphContext graphContext) {
        super(MouseEvent.BUTTON1_MASK + MouseEvent.SHIFT_MASK);
        _graphContext = graphContext;
        _linkPainter = linkPainter;
        cursor = Cursor.getDefaultCursor();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        _linkPainter.moveCursor(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        _linkPainter.moveCursor(e);
    }

    @Override
    public void mouseClicked(MouseEvent me) {
        logger.debug("mouseClicked({})", me);
        final boolean ended = _linkPainter.endLink(me);
        if (ended) {
            me.consume();
        }
    }

    @Override
    public void mousePressed(MouseEvent me) {
        final boolean mods = checkModifiers(me);
        logger.debug("mousePressed({}) (mods={})", me, mods);
        if (mods) {
            final Object vertex = _graphContext.getVertex(me);
            if (vertex != null) {
                me.consume();
                _linkPainter.startLink(new JobGraphLinkPainter.VertexContext(vertex,
                        _graphContext.getAnalysisJobBuilder(vertex), null));
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent me) {
        final boolean mods = checkModifiers(me);
        logger.debug("mouseReleased({}) (mods={})", me, mods);
        if (mods) {
            final Point2D p = me.getPoint();
            final Object vertex = _graphContext.getVertex(p);
            final boolean ended = _linkPainter.endLink(vertex, me);
            if (ended) {
                me.consume();
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}
