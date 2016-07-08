package org.datacleaner.monitor.referencedata;

import org.datacleaner.monitor.referencedata.widgets.ReferenceDataOverviewPanel;
import org.datacleaner.monitor.shared.ClientConfig;
import org.datacleaner.monitor.shared.DictionaryClientConfig;
import org.datacleaner.monitor.shared.widgets.LoadingIndicator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.RootPanel;


public class ReferencedataEntryPoint implements com.google.gwt.core.client.EntryPoint {

   
    private final Logger logger = LoggerFactory.getLogger(ReferencedataEntryPoint.class); 
    @Override
    public void onModuleLoad() {
        final ClientConfig clientConfig = new DictionaryClientConfig();
        final ReferenceDataServiceAsync service = GWT.create(ReferenceDataService.class);
        
        final RootPanel rootPanel = RootPanel.get("RootPanelTarget");
        rootPanel.add(new LoadingIndicator());

        final ReferenceDataOverviewPanel overviewPanel = new ReferenceDataOverviewPanel(clientConfig, service);
        overviewPanel.initialize(new Runnable() {
            @Override
            public void run() {
                rootPanel.clear();
                rootPanel.add(overviewPanel);
            }
        });
       

    }

}
