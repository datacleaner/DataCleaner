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
package org.datacleaner.monitor.scheduling.widgets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.datacleaner.monitor.scheduling.SchedulingServiceAsync;
import org.datacleaner.monitor.scheduling.model.ScheduleDefinition;
import org.datacleaner.monitor.shared.ClientConfig;
import org.datacleaner.monitor.shared.model.JobIdentifier;
import org.datacleaner.monitor.util.DCAsyncCallback;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;

/**
 * Presents an overview of all scheduled activity in DC monitor.
 */
public class SchedulingOverviewPanel extends Composite {
    static final String JOB_GROUPING_CATEGORY = "Group";

    static final int PAGE_SIZE = 10;

    private static final boolean USE_TAB_PANEL = false;

    private static final String CATEGORY = "Category";
    private static final String OTHERS = "(Other)";
    private final ClientConfig _clientConfig;
    private final SchedulingServiceAsync _service;

    public SchedulingOverviewPanel(final ClientConfig clientConfig, final SchedulingServiceAsync service) {
        _clientConfig = clientConfig;
        _service = service;
    }

    public void initialize(final Runnable listener) {
        if (USE_TAB_PANEL) {
            _service.getSchedules(_clientConfig.getTenant(), false, new DCAsyncCallback<List<ScheduleDefinition>>() {
                @Override
                public void onSuccess(final List<ScheduleDefinition> result) {

                    final Map<String, List<ScheduleDefinition>> categoryAndGroupMapForJobs =
                            createCategoryAndGroupMapForJobs(result);

                    final int categoryCount = categoryAndGroupMapForJobs.size();

                    if (categoryCount == 0) {
                        final HorizontalPanel panel = new HorizontalPanel();
                        panel.add(new Label("There are no jobs available."));
                        initWidget(panel);
                    } else if (categoryCount == 1) {
                        getSynchronousPagedPanel(result);
                    } else {
                        final DecoratedTabPanel tabPanel = new DecoratedTabPanel();
                        tabPanel.setWidth("100%");
                        Collection<String> jobCategories = categoryAndGroupMapForJobs.keySet();
                        jobCategories = sortJobCategories(jobCategories);

                        for (final String jobCategory : jobCategories) {
                            final List<ScheduleDefinition> list = categoryAndGroupMapForJobs.get(jobCategory);
                            final ComplexPanel panel = getSynchronousPagedPanel(list);
                            tabPanel.add(panel, jobCategory);
                        }
                        tabPanel.selectTab(0);
                        tabPanel.ensureDebugId("cwTabPanel");
                        initWidget(tabPanel);
                    }
                    listener.run();
                }

                private Collection<String> sortJobCategories(final Collection<String> jobCategories) {
                    final List<String> result = new ArrayList<>(jobCategories);

                    // move OTHERS to the end
                    final boolean removed = result.remove(OTHERS);
                    if (removed) {
                        result.add(OTHERS);
                    }

                    return result;
                }

                private Map<String, List<ScheduleDefinition>> createCategoryAndGroupMapForJobs(
                        final List<ScheduleDefinition> result) {
                    final Map<String, List<ScheduleDefinition>> categoryAndGroupMap = new TreeMap<>();

                    for (final ScheduleDefinition scheduleDefinition : result) {
                        final Map<String, String> jobMetadataProperties = scheduleDefinition.getJobMetadataProperties();

                        GWT.log("Job '" + scheduleDefinition.getJob().getName() + "' metadata: "
                                + jobMetadataProperties);

                        final String categoryName;
                        if (jobMetadataProperties == null) {
                            categoryName = OTHERS;
                        } else {
                            final String metadataValue = jobMetadataProperties.get(CATEGORY);
                            if (metadataValue != null && !"".equals(metadataValue.trim())) {
                                categoryName = metadataValue;
                            } else {
                                categoryName = OTHERS;
                            }
                        }

                        List<ScheduleDefinition> listOfJobWithSameCategory;
                        listOfJobWithSameCategory = categoryAndGroupMap.get(categoryName);

                        if (listOfJobWithSameCategory == null) {
                            listOfJobWithSameCategory = new ArrayList<>();

                            categoryAndGroupMap.put(categoryName, listOfJobWithSameCategory);
                        }
                        listOfJobWithSameCategory.add(scheduleDefinition);

                    }
                    return categoryAndGroupMap;

                }

            });
        } else {
            _service.getJobs(_clientConfig.getTenant(), new DCAsyncCallback<List<JobIdentifier>>() {

                @Override
                public void onSuccess(final List<JobIdentifier> jobs) {
                    if (jobs.size() == 0) {
                        final HorizontalPanel panel = new HorizontalPanel();
                        panel.add(new Label("There are no jobs available."));
                        initWidget(panel);
                    } else {
                        getAsyncPagedPanel(jobs);
                    }
                    listener.run();
                }
            });
        }
    }

    public void addSchedule(final ScheduleDefinition schedule, final String jobGroupingCategory, final FlowPanel panel,
            final Map<String, ScheduleGroupPanel> scheduleGroupPanels) {
        addScheduleInGroup(schedule, jobGroupingCategory, panel, scheduleGroupPanels);
    }

    ScheduleGroupPanel addScheduleInGroup(final ScheduleDefinition schedule, final String jobGroupingCategory,
            final FlowPanel panel, final Map<String, ScheduleGroupPanel> scheduleGroupPanels) {
        String groupName = null;

        if (jobGroupingCategory == null || jobGroupingCategory.trim().length() == 0) {
            groupName = schedule.getGroupName();
        } else {
            final Map<String, String> jobMetadataProperties = schedule.getJobMetadataProperties();

            if (jobMetadataProperties != null) {
                groupName = jobMetadataProperties.get(jobGroupingCategory);
            }
            if (groupName == null || groupName.trim().length() == 0) {
                groupName = schedule.getGroupName();
            }
        }

        if (groupName == null || groupName.trim().length() == 0) {
            groupName = "(other)";
        }

        final ScheduleGroupPanel scheduleGroupPanel;
        if (scheduleGroupPanels.containsKey(groupName)) {
            scheduleGroupPanel = scheduleGroupPanels.get(groupName);
        } else {
            scheduleGroupPanel = new ScheduleGroupPanel(groupName, _clientConfig, _service);
            panel.add(scheduleGroupPanel);
            scheduleGroupPanels.put(groupName, scheduleGroupPanel);
        }

        scheduleGroupPanel.addSchedule(schedule);

        return scheduleGroupPanel;
    }

    private Panel createHeaderPanel() {
        final FlowPanel panel = new FlowPanel();
        panel.addStyleName("ColumnHeaders");
        panel.addStyleName("row");

        panel.add(createLabel("Job name", "col-sm-9", "col-xs-8"));
        panel.add(createLabel("Actions", "col-sm-3", "col-xs-4"));

        return panel;
    }

    private Label createLabel(final String text, final String... styleNames) {
        final Label label = new Label();
        label.setText(text);
        for (final String styleName : styleNames) {
            label.addStyleName(styleName);
        }
        return label;
    }

    private ComplexPanel getSynchronousPagedPanel(final List<ScheduleDefinition> result) {
        final ScheduleDataPanel dataFlowPanel = getScheduleDataPanel();

        final ListDataProvider<ScheduleDefinition> dataProvider = new ListDataProvider<>();
        final List<ScheduleDefinition> data = dataProvider.getList();

        for (final ScheduleDefinition definition : result) {
            data.add(definition);
        }
        dataProvider.addDataDisplay(dataFlowPanel);

        return getPagedPanel(dataFlowPanel);
    }

    private ComplexPanel getPagedPanel(final ScheduleDataPanel dataFlowPanel) {
        final ScheduleDataPager pager = new ScheduleDataPager();
        pager.setDisplay(dataFlowPanel);
        pager.setPageSize(PAGE_SIZE);

        final VerticalPanel pagedPanel = new VerticalPanel();
        pagedPanel.setWidth("100%");
        pagedPanel.add(dataFlowPanel);
        pagedPanel.add(pager);
        initWidget(pagedPanel);

        return pagedPanel;
    }

    private ComplexPanel getAsyncPagedPanel(final List<JobIdentifier> result) {
        final ScheduleDataPanel dataFlowPanel = getScheduleDataPanel();

        final ScheduleDataProvider dataProvider = new ScheduleDataProvider(_clientConfig, _service, result);
        dataProvider.updateRowCount(result.size(), true);
        dataProvider.addDataDisplay(dataFlowPanel);

        return getPagedPanel(dataFlowPanel);
    }

    private ScheduleDataPanel getScheduleDataPanel() {
        final ScheduleDataPanel dataFlowPanel = new ScheduleDataPanel(this);
        dataFlowPanel.add(createHeaderPanel());
        dataFlowPanel.addStyleName("SchedulingOverviewPanel");
        return dataFlowPanel;
    }
}
