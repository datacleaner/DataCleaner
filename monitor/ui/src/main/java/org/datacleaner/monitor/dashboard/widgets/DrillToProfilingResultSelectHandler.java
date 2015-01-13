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

import java.util.Date;

import org.datacleaner.monitor.dashboard.model.TimelineData;
import org.datacleaner.monitor.dashboard.model.TimelineDefinition;
import org.datacleaner.monitor.shared.model.MetricIdentifier;
import org.datacleaner.monitor.shared.widgets.CancelPopupButton;
import org.datacleaner.monitor.shared.widgets.DCPopupPanel;
import org.datacleaner.monitor.util.Urls;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HTML;
import com.googlecode.gflot.client.event.PlotItem;

/**
 * Handler invoked when selecting a point in a timeline chart
 */
public class DrillToProfilingResultSelectHandler {

    public static final String PROPERTY_NAME_RESULT_FILE = "path";

    private final DCPopupPanel _popup;
    private final TimelineDefinition _timelineDefinition;
    private final TimelineData _timelineData;

    private PlotItem _item;

    public DrillToProfilingResultSelectHandler(PlotItem item, TimelineDefinition timelineDefinition,
            TimelineData timelineData) {
        _item = item;
        _timelineDefinition = timelineDefinition;
        _timelineData = timelineData;
        _popup = new DCPopupPanel("Inspect profiling result?");
        _popup.addStyleName("DrillToProfilingResultPopupPanel");
    }

    public void onSelect() {

        final String metricLabel = _item.getSeries().getLabel();

        final Integer index = _item.getDataIndex();
        GWT.log("Item index: " + index);
        
        final Date date = _timelineData.getRows().get(index).getDate();
        final String formattedDate = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT).format(date);
        final String resultFilePath = _timelineData.getRows().get(index).getResultFilePath();

        final String analyzerDescriptorName = getAnalyzerDescriptorName();
        final String bookmark = createResultUrlBookmark(analyzerDescriptorName);

        final String url = Urls.createRelativeUrl("repository" + resultFilePath) + bookmark;
        GWT.log("Drill to result URL: " + url);

        final Button showResultButton = new Button("Show results");
        showResultButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Frame frame = new Frame(url);
                frame.setPixelSize(800, 500);
                _popup.setWidget(frame);
                _popup.removeButton(showResultButton);
                _popup.center();
            }
        });

        final Button showResultFullPageButton = new Button("Show results (new window)");
        showResultFullPageButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Window.open(url, "_blank", null);
            }
        });

        final SafeHtml labelHtml = new SafeHtmlBuilder()
                .appendHtmlConstant("Do you wish to inspect the profiling result for ").appendEscaped(metricLabel)
                .appendEscapedLines("\ncollected at ").appendEscaped(formattedDate).appendHtmlConstant("?")
                .toSafeHtml();

        _popup.setWidget(new HTML(labelHtml));
        _popup.removeButtons();
        _popup.addButton(showResultButton);
        _popup.addButton(showResultFullPageButton);
        _popup.addButton(new CancelPopupButton(_popup));
        _popup.center();
        _popup.show();
    }

    /**
     * Create a URL bookmark-part (ie. that part with a hash-sign) to make sure
     * that the result URL will point to the correct analyzer tab.
     * 
     * @param analyzerDescriptorName
     * @return
     */
    private String createResultUrlBookmark(String analyzerDescriptorName) {
        return "#analysisResultDescriptorGroup_" + toCamelCase(analyzerDescriptorName);
    }

    public static String toCamelCase(String analyzerDescriptorName) {
        if (analyzerDescriptorName == null) {
            return "";
        }
        analyzerDescriptorName = analyzerDescriptorName.trim();
        StringBuilder sb = new StringBuilder(analyzerDescriptorName);

        boolean capitalizeNext = true;
        for (int i = 0; i < sb.length(); i++) {
            char c = sb.charAt(i);
            if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                sb.deleteCharAt(i);
                capitalizeNext = true;
                i--;
            } else if (capitalizeNext) {
                if (Character.isLowerCase(c)) {
                    sb.setCharAt(i, Character.toUpperCase(c));
                }
                capitalizeNext = false;
            }
        }

        return sb.toString();
    }

    private String getAnalyzerDescriptorName() {
        final MetricIdentifier metric = _timelineDefinition.getMetrics().get(_item.getSeriesIndex());
        GWT.log("Clicked metric is: " + metric);
        if (metric.isFormulaBased()) {
            return metric.getChildren().get(0).getAnalyzerDescriptorName();
        } else {
            return metric.getAnalyzerDescriptorName();
        }
    }
}
