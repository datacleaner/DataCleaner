package org.eobjects.datacleaner.monitor.timeline.widgets;

import com.google.gwt.user.client.ui.SuggestBox;

public class StringParameterizedMetricTextBox extends SuggestBox {

    public StringParameterizedMetricTextBox(String text) {
        super();
        addStyleName("StringParameterizedMetricTextBox");
        setText(text);
    }

}
