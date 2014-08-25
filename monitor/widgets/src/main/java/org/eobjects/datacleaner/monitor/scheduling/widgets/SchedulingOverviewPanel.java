/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.monitor.scheduling.widgets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eobjects.datacleaner.monitor.scheduling.SchedulingServiceAsync;
import org.eobjects.datacleaner.monitor.scheduling.model.ScheduleDefinition;
import org.eobjects.datacleaner.monitor.shared.ClientConfig;
import org.eobjects.datacleaner.monitor.util.DCAsyncCallback;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;

/**
 * Presents an overview of all scheduled activity in the DCmonitor.
 */
public class SchedulingOverviewPanel extends Composite {

    private static final String CATEGORY = "Category";
	private static final String OTHERS = "Others";
	private final ClientConfig _clientConfig;
    private final SchedulingServiceAsync _service;
    private final Map<String, ScheduleGroupPanel> _scheduleGroupPanels;
    
    
    
    
    public SchedulingOverviewPanel(ClientConfig clientConfig, SchedulingServiceAsync service) {
        _clientConfig = clientConfig;
        _service = service;
        _scheduleGroupPanels = new HashMap<String, ScheduleGroupPanel>();
        
        
        
    }

    public void initialize(final Runnable listener) {
        _service.getSchedules(_clientConfig.getTenant(), new DCAsyncCallback<List<ScheduleDefinition>>() {
            @Override
            public void onSuccess(List<ScheduleDefinition> result) {
            	
            	Map<String, List<ScheduleDefinition>> categoryAndGroupMapForJobs = createCategoryAndGroupMapForJobs(result);
            	Window.alert(categoryAndGroupMapForJobs.toString());
            	String jobGroupingCategory = "Group";
            	
            	if(categoryAndGroupMapForJobs.size()==1){
            		FlowPanel panel = new FlowPanel();
            		panel.add(createHeaderPanel());
            		panel.addStyleName("SchedulingOverviewPanel");
            		for (ScheduleDefinition scheduleDefinition : result) {
            			addSchedule(scheduleDefinition, jobGroupingCategory,panel);
            		}
            		initWidget(panel);
            	}else{
            		DecoratedTabPanel tabPanel = new DecoratedTabPanel();
            		Set<String> jobCategories = categoryAndGroupMapForJobs.keySet();
            		for (String jobCategory : jobCategories) {
            			FlowPanel panel = new FlowPanel();
                		panel.add(createHeaderPanel());
                		panel.addStyleName("SchedulingOverviewPanel");
                		tabPanel.add(panel,jobCategory);
                		for (ScheduleDefinition scheduleDefinition : categoryAndGroupMapForJobs.get(jobCategory)) {
                			addSchedule(scheduleDefinition, jobGroupingCategory,panel);
                		}
            		}
            		tabPanel.selectTab(0);
            	    tabPanel.ensureDebugId("cwTabPanel");
            		initWidget(tabPanel);
            	}
                listener.run();
            }

			private Map<String,List<ScheduleDefinition>> createCategoryAndGroupMapForJobs(
					List<ScheduleDefinition> result) {
				Map<String,List<ScheduleDefinition>> categoryAndGroupMap = new TreeMap<String, List<ScheduleDefinition>>();
				for (ScheduleDefinition scheduleDefinition : result) {
					Map<String, String> jobMetadataProperties = scheduleDefinition.getJobMetadataProperties();
					String categoryName;
					if(jobMetadataProperties == null){
						categoryName = OTHERS;
					}else{
						categoryName = jobMetadataProperties.get(CATEGORY);
						Window.alert(categoryName);
						Window.alert("JOB mETADATA "+jobMetadataProperties);
					}
					
					List<ScheduleDefinition> listOfJobWithSameCategory;
					if(categoryName == null || categoryName.isEmpty() ){
						categoryName = OTHERS;
					}
						listOfJobWithSameCategory = categoryAndGroupMap.get(categoryName);
					
						if(listOfJobWithSameCategory == null){
							listOfJobWithSameCategory = new ArrayList<ScheduleDefinition>();
							Window.alert("cat name" + categoryName);
							Window.alert("list name" + listOfJobWithSameCategory);
							
							categoryAndGroupMap.put(categoryName, listOfJobWithSameCategory);
						}
						listOfJobWithSameCategory.add(scheduleDefinition);
					
				}
				return categoryAndGroupMap;
				
			}

        });
    }

    public void addSchedule(ScheduleDefinition schedule, String jobGroupingCategory, FlowPanel panel) {
    	String groupName = null;
    	
    	if(jobGroupingCategory == null || jobGroupingCategory.trim().length() == 0){
    		groupName = schedule.getGroupName();
    	} else {
    		Map<String, String> jobMetadataProperties = schedule.getJobMetadataProperties();
    		
    		if(jobMetadataProperties != null){
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
        if (_scheduleGroupPanels.containsKey(groupName)) {
            scheduleGroupPanel = _scheduleGroupPanels.get(groupName);
        } else {
            scheduleGroupPanel = new ScheduleGroupPanel(groupName, _clientConfig, _service);
            panel.add(scheduleGroupPanel);
            _scheduleGroupPanels.put(groupName, scheduleGroupPanel);
        }

        scheduleGroupPanel.addSchedule(schedule);
    }

    private Panel createHeaderPanel() {
        final FlowPanel panel = new FlowPanel();
        panel.addStyleName("ColumnHeaders");

        panel.add(createLabel("Job name", "JobColumn"));
        panel.add(createLabel("Actions", "ActionsColumn"));

        return panel;
    }

    private Label createLabel(String text, String styleName) {
        final Label label = new Label();
        label.setText(text);
        label.setStyleName(styleName);
        return label;
    }
}
