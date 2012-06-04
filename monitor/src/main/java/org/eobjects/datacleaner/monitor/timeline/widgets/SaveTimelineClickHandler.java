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
package org.eobjects.datacleaner.monitor.timeline.widgets;

import org.eobjects.datacleaner.monitor.timeline.TimelineServiceAsync;
import org.eobjects.datacleaner.monitor.timeline.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.timeline.model.TimelineDefinition;
import org.eobjects.datacleaner.monitor.timeline.model.TimelineIdentifier;
import org.eobjects.datacleaner.monitor.util.DCAsyncCallback;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;

/**
 * Click handler invoked when the user clicks the "Save timeline" button.
 */
public class SaveTimelineClickHandler implements ClickHandler {

    private final TimelineServiceAsync _service;
    private final TenantIdentifier _tenantIdentifier;
    private final TimelinePanel _timelinePanel;

    public SaveTimelineClickHandler(TimelineServiceAsync service, TenantIdentifier tenantIdentifier,
            TimelinePanel timelinePanel) {
        _service = service;
        _tenantIdentifier = tenantIdentifier;
        _timelinePanel = timelinePanel;
    }

    @Override
    public void onClick(ClickEvent event) {
        final boolean create;
        TimelineIdentifier timelineIdentifier = _timelinePanel.getTimelineIdentifier();
        if (timelineIdentifier == null) {
            // TODO: This is a bit too naive :P
            String name = Window.prompt("Name of timeline?", "");
            timelineIdentifier = new TimelineIdentifier(name, "/" + _tenantIdentifier.getId() + "/timelines/" + name
                    + ".analysis.timeline.xml");
            create = true;
        } else {
            create = false;
        }
        TimelineDefinition timelineDefinition = _timelinePanel.getTimelineDefinition();

        if (create) {
            _service.createTimelineDefinition(_tenantIdentifier, timelineIdentifier, timelineDefinition,
                    new DCAsyncCallback<TimelineIdentifier>() {
                        @Override
                        public void onSuccess(TimelineIdentifier result) {
                            _timelinePanel.setTimelineIdentifier(result);
                            Window.alert("Saved timeline '" + result.getName() + "'");
                        }
                    });
        } else {
            _service.updateTimelineDefinition(_tenantIdentifier, timelineIdentifier, timelineDefinition,
                    new DCAsyncCallback<TimelineIdentifier>() {
                        @Override
                        public void onSuccess(TimelineIdentifier result) {
                            Window.alert("Saved timeline '" + result.getName() + "'");
                        }
                    });
        }
    }

}
