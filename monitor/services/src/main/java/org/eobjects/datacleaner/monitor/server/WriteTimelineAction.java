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
package org.eobjects.datacleaner.monitor.server;

import java.io.OutputStream;

import org.eobjects.datacleaner.monitor.dashboard.model.TimelineDefinition;
import org.eobjects.datacleaner.monitor.server.jaxb.JaxbTimelineWriter;
import org.apache.metamodel.util.Action;

public class WriteTimelineAction implements Action<OutputStream> {

    private final TimelineDefinition _timeline;

    public WriteTimelineAction(TimelineDefinition timeline) {
        _timeline = timeline;
    }

    @Override
    public void run(OutputStream out) throws Exception {
        final TimelineWriter writer = new JaxbTimelineWriter();
        writer.write(_timeline, out);
    }
}
