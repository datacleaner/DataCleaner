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
package org.datacleaner.monitor.dashboard.widgets;

import org.datacleaner.monitor.dashboard.DashboardServiceAsync;
import org.datacleaner.monitor.dashboard.model.DefaultVAxisOption;
import org.datacleaner.monitor.dashboard.model.TimelineData;
import org.datacleaner.monitor.dashboard.model.TimelineDefinition;
import org.datacleaner.monitor.dashboard.model.TimelineIdentifier;
import org.datacleaner.monitor.shared.model.TenantIdentifier;
import org.datacleaner.monitor.shared.widgets.ButtonPanel;
import org.datacleaner.monitor.shared.widgets.DCButtons;
import org.datacleaner.monitor.shared.widgets.HeadingLabel;
import org.datacleaner.monitor.shared.widgets.LoadingIndicator;
import org.datacleaner.monitor.util.DCAsyncCallback;

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

    public TimelinePanel(final TenantIdentifier tenant, final DashboardServiceAsync service,
            final TimelineIdentifier timelineIdentifier, final DashboardGroupPanel timelineGroupPanel,
            final boolean isDashboardEditor) {
        super();
        _tenant = tenant;
        _service = service;
        _timelineIdentifier = timelineIdentifier;
        _timelineGroupPanel = timelineGroupPanel;
        _isDashboardEditor = isDashboardEditor;
        _header = new HeadingLabel("");

        _loadingIndicator = new LoadingIndicator();
        _loadingIndicator.setHeight((DefaultVAxisOption.DEFAULT_HEIGHT + 4) + "px");

        _saveButton = DCButtons.defaultButton("glyphicon-save", null);
        _saveButton.setVisible(isDashboardEditor);
        _saveButton.setTitle("Save timeline");
        _saveButton.addClickHandler(new SaveTimelineClickHandler(_service, _tenant, this));

        if (_timelineIdentifier != null) {
            // initially does not make sense to save an (unchanged) and
            // identifyable timeline.
            setTimelineDefinitionUnchanged();
        }

        _deleteButton = DCButtons.dangerButton("glyphicon-minus", null);
        _deleteButton.setVisible(isDashboardEditor);
        _deleteButton.setTitle("Delete timeline");
        _deleteButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                final boolean confirmation = Window.confirm("Are you sure you wish to delete this timeline?");
                if (confirmation) {
                    if (_timelineIdentifier != null) {
                        _service.removeTimeline(_tenant, _timelineIdentifier, new DCAsyncCallback<Boolean>() {
                            @Override
                            public void onSuccess(final Boolean result) {
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

    public void setTimelineIdentifier(final TimelineIdentifier timelineIdentifier) {
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
                setTimelineDefinitionChanged();
            }
            setLoading();
            _service.getTimelineData(_tenant, timelineDefinition, new DCAsyncCallback<TimelineData>() {
                @Override
                public void onSuccess(final TimelineData data) {
                    setTimelineData(data);
                }
            });
        }
    }

    public TimelineDefinition getTimelineDefinition() {
        return _timelineDefinition;
    }

    public void setTimelineDefinition(final TimelineDefinition timelineDefinition) {
        setTimelineDefinition(timelineDefinition, true);
    }

    private void renderChart() {
        remove(_loadingIndicator);
        final TimelineDesigner timeLineDesigner =
                new TimelineDesigner(_timelineDefinition, _timelineData, this, _isDashboardEditor);
        add(timeLineDesigner.createPlot());
        add(timeLineDesigner.getLegendPanel());
    }

    public TimelineData getTimelineData() {
        return _timelineData;
    }

    public void setTimelineData(final TimelineData timelineData) {
        if (timelineData.equals(_timelineData)) {
            return;
        }
        _timelineData = timelineData;

        renderChart();
    }

    private FlowPanel createButtonPanel() {
        final Button customizeButton = DCButtons.defaultButton("glyphicon-cog", null);
        customizeButton.setVisible(_isDashboardEditor);
        customizeButton.setTitle("Customize timeline");
        customizeButton.addClickHandler(new CustomizeTimelineHandler(_service, this));

        final Button copyButton = DCButtons.defaultButton("glyphicon-duplicate", null);
        copyButton.setVisible(_isDashboardEditor);
        copyButton.setTitle("Copy timeline");
        copyButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                final TimelinePanel copyPanel =
                        new TimelinePanel(_tenant, _service, null, _timelineGroupPanel, _isDashboardEditor);
                copyPanel.setTimelineDefinition(_timelineDefinition);
                _timelineGroupPanel.add(copyPanel);
            }
        });

        final ButtonPanel buttonPanel = new ButtonPanel(false);
        buttonPanel.add(customizeButton);
        buttonPanel.add(copyButton);
        buttonPanel.add(_saveButton);
        buttonPanel.add(_deleteButton);

        final FlowPanel timelineButtonPanel = new FlowPanel();
        timelineButtonPanel.addStyleName("TimelineButtonPanel");
        timelineButtonPanel.add(_header);
        timelineButtonPanel.add(buttonPanel);

        return timelineButtonPanel;
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

    public void refreshTimelineDefiniton(final boolean isSaveTimelineActive) {
        setLoading();
        renderChart();
        if (isSaveTimelineActive) {
            _saveButton.setEnabled(true);
        }
    }

    public void setTimelineDefinitionChanged() {
        _saveButton.setEnabled(true);
    }

    public void setTimelineDefinitionUnchanged() {
        _saveButton.setEnabled(false);
    }

}
