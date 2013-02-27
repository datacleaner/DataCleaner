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
package org.eobjects.datacleaner.monitor.dashboard.widgets;

import org.eobjects.datacleaner.monitor.dashboard.DashboardServiceAsync;
import org.eobjects.datacleaner.monitor.dashboard.model.DefaultVAxisOption;
import org.eobjects.datacleaner.monitor.dashboard.model.TimelineData;
import org.eobjects.datacleaner.monitor.dashboard.model.TimelineDefinition;
import org.eobjects.datacleaner.monitor.dashboard.model.TimelineIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.shared.widgets.ButtonPanel;
import org.eobjects.datacleaner.monitor.shared.widgets.HeadingLabel;
import org.eobjects.datacleaner.monitor.shared.widgets.LoadingIndicator;
import org.eobjects.datacleaner.monitor.util.DCAsyncCallback;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * Panel that displays a timeline.
 */
public class TimelinePanel extends FlowPanel {

    

    private final DashboardServiceAsync _service;
    private final LoadingIndicator _loadingIndicator;
    private final TenantIdentifier _tenant;
    private final HeadingLabel _header;
    private final DashboardGroupPanel _timelineGroupPanel;
    private final Button _saveButton;
    private final Button _deleteButton;
    private final boolean _isDashboardEditor;

    private TimelineIdentifier _timelineIdentifier;
    private TimelineDefinition _timelineDefinition;
    private TimelineData _timelineData;

    public TimelinePanel(TenantIdentifier tenant, DashboardServiceAsync service, TimelineIdentifier timelineIdentifier,
            DashboardGroupPanel timelineGroupPanel, boolean isDashboardEditor) {
        super();
        _tenant = tenant;
        _service = service;
        _timelineIdentifier = timelineIdentifier;
        _timelineGroupPanel = timelineGroupPanel;
        _isDashboardEditor = isDashboardEditor;
        _header = new HeadingLabel("");

        _loadingIndicator = new LoadingIndicator();
        _loadingIndicator.setHeight((DefaultVAxisOption.DEFAULT_HEIGHT + 4) + "px");

        _saveButton = new Button("");
        _saveButton.setVisible(isDashboardEditor);
        _saveButton.addStyleDependentName("ImageButton");
        _saveButton.setTitle("Save timeline");
        _saveButton.addStyleName("SaveButton");
        _saveButton.addClickHandler(new SaveTimelineClickHandler(_service, _tenant, this));
        _saveButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                // disable button once saved
                _saveButton.setEnabled(false);
            }
        });

        if (_timelineIdentifier != null) {
            // initially does not make sense to save an (unchanged) and
            // identifyable timeline.
            _saveButton.setEnabled(false);
        }

        _deleteButton = new Button();
        _deleteButton.setVisible(isDashboardEditor);
        _deleteButton.addStyleDependentName("ImageButton");
        _deleteButton.setTitle("Delete timeline");
        _deleteButton.addStyleName("DeleteButton");
        _deleteButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final boolean confirmation = Window.confirm("Are you sure you wish to delete this timeline?");
                if (confirmation) {
                    if (_timelineIdentifier != null) {
                        _service.removeTimeline(_tenant, _timelineIdentifier, new DCAsyncCallback<Boolean>() {
                            @Override
                            public void onSuccess(Boolean result) {
                                // do nothing
                            }
                        });
                    }
                    _timelineGroupPanel.removeTimelinePanel(TimelinePanel.this);
                }
            }
        });

        addStyleName("TimelinePanel");
        add(createButtonPanel());
        updateHeader();
        setLoading();

        if (_timelineIdentifier != null) {
            _service.getTimelineDefinition(_tenant, _timelineIdentifier, new DCAsyncCallback<TimelineDefinition>() {
                @Override
                public void onSuccess(final TimelineDefinition definition) {
                    setTimelineDefinition(definition);
                }
            });
        }
    }

    private void setLoading() {
        if (getWidgetCount() == 2) {
            if (getWidget(1) == _loadingIndicator) {
                // the loading indicator is already showing correctly
                return;
            }
        }

        // clean up everything except the button panel
        while (getWidgetCount() > 1) {
            remove(1);
        }
        add(_loadingIndicator);
    }

    public TimelineIdentifier getTimelineIdentifier() {
        return _timelineIdentifier;
    }

    public void setTimelineIdentifier(TimelineIdentifier timelineIdentifier) {
        if (timelineIdentifier.equals(_timelineIdentifier)) {
            return;
        }

        _timelineIdentifier = timelineIdentifier;

        updateHeader();

        if (_timelineData != null) {
            setLoading();
            renderChart();
        }
    }

    private void updateHeader() {
        if (_timelineIdentifier == null) {
            _header.setText("<new timeline>");
        } else {
            _header.setText(_timelineIdentifier.getName());
        }
    }

    public TenantIdentifier getTenantIdentifier() {
        return _tenant;
    }

    public void setTimelineDefinition(final TimelineDefinition timelineDefinition, final boolean fireEvents) {
        if (timelineDefinition.equals(_timelineDefinition) && _timelineData != null) {
            return;
        }
        _timelineDefinition = timelineDefinition;
        if (fireEvents) {
            if (timelineDefinition.isChanged()) {
                _saveButton.setEnabled(true);
            }
            setLoading();
            _service.getTimelineData(_tenant, timelineDefinition, new DCAsyncCallback<TimelineData>() {
                @Override
                public void onSuccess(TimelineData data) {
                    setTimelineData(data);
                }
            });
        }
    }

    public void setTimelineDefinition(final TimelineDefinition timelineDefinition) {
        setTimelineDefinition(timelineDefinition, true);
    }

    public TimelineDefinition getTimelineDefinition() {
        return _timelineDefinition;
    }

    public void setTimelineData(final TimelineData timelineData) {
        if (timelineData.equals(_timelineData)) {
            return;
        }
        _timelineData = timelineData;

        renderChart();
    }

    private void renderChart() {
        remove(_loadingIndicator);
        TimelineDesigner timeLineDesigner = new TimelineDesigner(_timelineDefinition, _timelineData, this,
                _isDashboardEditor);
        add(timeLineDesigner.createPlot());
        add(timeLineDesigner.getLegendPanel());
    }

    public TimelineData getTimelineData() {
        return _timelineData;
    }

    private ButtonPanel createButtonPanel() {
        final Button customizeButton = new Button("");
        customizeButton.setVisible(_isDashboardEditor);
        customizeButton.addStyleDependentName("ImageButton");
        customizeButton.setTitle("Customize timeline");
        customizeButton.addStyleName("CustomizeButton");
        customizeButton.addClickHandler(new CustomizeTimelineHandler(_service, this));

        final Button copyButton = new Button("");
        copyButton.setVisible(_isDashboardEditor);
        copyButton.addStyleDependentName("ImageButton");
        copyButton.setTitle("Copy timeline");
        copyButton.addStyleName("CopyButton");
        copyButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                TimelinePanel copyPanel = new TimelinePanel(_tenant, _service, null, _timelineGroupPanel,
                        _isDashboardEditor);
                copyPanel.setTimelineDefinition(_timelineDefinition);
                _timelineGroupPanel.add(copyPanel);
            }
        });

        final ButtonPanel buttonPanel = new ButtonPanel();

        buttonPanel.add(_header);
        buttonPanel.add(customizeButton);
        buttonPanel.add(copyButton);
        buttonPanel.add(_saveButton);
        buttonPanel.add(_deleteButton);

        return buttonPanel;
    }

    public DashboardGroupPanel getTimelineGroupPanel() {
        return _timelineGroupPanel;
    }

    @Override
    protected void onAttach() {
        super.onAttach();

        // (re) attaching charts needs re-rendering
        if (_timelineDefinition != null && _timelineData != null) {
            setLoading();
            renderChart();
        }
    }

    public void refreshTimelineDefiniton(boolean isSaveTimelineActive) {
        setLoading();
        renderChart();
        if (isSaveTimelineActive) {
            _saveButton.setEnabled(true);
        }
    }

}
