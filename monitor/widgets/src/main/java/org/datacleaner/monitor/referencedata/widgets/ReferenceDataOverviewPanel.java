package org.datacleaner.monitor.referencedata.widgets;

import org.datacleaner.monitor.referencedata.ReferenceDataServiceAsync;
import org.datacleaner.monitor.shared.ClientConfig;
import org.datacleaner.monitor.util.DCAsyncCallback;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

public class ReferenceDataOverviewPanel  extends Composite {

    private final ReferenceDataServiceAsync _service; 
    private final ClientConfig _clientConfig;
    
    public ReferenceDataOverviewPanel(ClientConfig clientConfig, ReferenceDataServiceAsync service) {
        _clientConfig = clientConfig; 
        _service = service; 
    }
    
    public void initialize(final Runnable listener){
        _service.getDictionaries(_clientConfig.getTenant(), new DCAsyncCallback() {

            @Override
            public void onSuccess(Object result) {
                final HorizontalPanel panel = new HorizontalPanel();
                panel.add(new Label("There are no dictionaries available."));
                initWidget(panel);
            }
        });
        listener.run();
    }
}
