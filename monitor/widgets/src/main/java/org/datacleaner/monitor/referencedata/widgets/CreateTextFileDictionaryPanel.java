package org.datacleaner.monitor.referencedata.widgets;

import org.datacleaner.monitor.shared.widgets.DCButtons;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;

public class CreateTextFileDictionaryPanel extends FlowPanel {

  public CreateTextFileDictionaryPanel() {
    // TODO Auto-generated constructor stub
      
      final Button formulaMetricButton = DCButtons.defaultButton("glyphicon-scale", "Add text file dictionary");
      add(formulaMetricButton); 
}
}
 