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
package org.eobjects.datacleaner.monitor.timeline.widgets;

import java.util.Date;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.visualization.client.AbstractDataTable;
import com.google.gwt.visualization.client.Selection;
import com.google.gwt.visualization.client.events.SelectHandler;
import com.google.gwt.visualization.client.visualizations.corechart.CoreChart;

/**
 * Handler invoked when selecting a point in a timeline chart
 */
public class DrillToProfilingResultSelectHandler extends SelectHandler {

    public static final String PROPERTY_NAME_RESULT_FILE = "path";

    private final CoreChart _chart;
    private final AbstractDataTable _data;
    private final PopupPanel _popup;

    public DrillToProfilingResultSelectHandler(CoreChart chart, AbstractDataTable data) {
        _chart = chart;
        _data = data;
        _popup = new PopupPanel(true, true);
        _popup.addStyleName("DrillToProfilingResultPopupPanel");
        _popup.setGlassEnabled(true);
    }

    @Override
    public void onSelect(SelectEvent event) {
        JsArray<Selection> selections = _chart.getSelections();

        if (selections == null || selections.length() != 1) {
            // this handler only reacts to single cell selections
            return;
        }

        Selection selection = selections.get(0);
        if (!selection.isCell()) {
            // this handler only reacts to single cell selections
            return;
        }

        final int column = selection.getColumn();
        final int row = selection.getRow();
        final String metricLabel = _data.getColumnLabel(column);
        final Date date = _data.getValueDate(row, 0);
        final String resultFilePath = _data.getProperty(row, 0, PROPERTY_NAME_RESULT_FILE);
        final String formattedDate = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT).format(date);

        final Button showResultButton = new Button("Show results");
        showResultButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Frame frame = new Frame("../repository" + resultFilePath);
                frame.setPixelSize(800, 500);
                _popup.setWidget(frame);
                _popup.center();
            }
        });

        final Button cancelButton = new Button("Cancel");
        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                _popup.hide();
            }
        });

        final FlowPanel buttonPanel = new FlowPanel();
        buttonPanel.addStyleName("ButtonPanel");
        buttonPanel.add(showResultButton);
        buttonPanel.add(cancelButton);

        final SafeHtml labelHtml = new SafeHtmlBuilder()
                .appendHtmlConstant("Do you wish to inspect the profiling result for ").appendEscaped(metricLabel)
                .appendEscapedLines("\ncollected at ").appendEscaped(formattedDate).appendHtmlConstant("?")
                .toSafeHtml();

        final VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.add(new HeadingLabel("Inspect profiling result?"));
        verticalPanel.add(new HTML(labelHtml));
        verticalPanel.add(buttonPanel);

        _popup.setWidget(verticalPanel);
        _popup.center();
        _popup.show();
    }
}
