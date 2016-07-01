package org.datacleaner.monitor.referencedata;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

public class ReferencedataEntryPoint implements EntryPoint {

    @Override
    public void onModuleLoad() {
        System.out.println("I am in module load Reference Data Entry Point");
        final CreateTextFileDictionaryPanel panel = new CreateTextFileDictionaryPanel();
        final RootPanel rootPanel = RootPanel.get("RootPanelTarget");
        rootPanel.add(panel);

    }

}
