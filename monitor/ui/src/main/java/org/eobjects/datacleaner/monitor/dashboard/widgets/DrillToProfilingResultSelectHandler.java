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
package org.eobjects.datacleaner.monitor.dashboard.widgets;

import java.util.Date;

import org.eobjects.datacleaner.monitor.shared.widgets.CancelPopupButton;
import org.eobjects.datacleaner.monitor.shared.widgets.DCPopupPanel;
import org.eobjects.datacleaner.monitor.util.Urls;

import com.google.gwt.core.client.JsArray;
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
    private final DCPopupPanel _popup;

    public DrillToProfilingResultSelectHandler(CoreChart chart, AbstractDataTable data) {
        _chart = chart;
        _data = data;
        _popup = new DCPopupPanel("Inspect profiling result?");
        _popup.addStyleName("DrillToProfilingResultPopupPanel");
    }

    @Override
    public void onSelect(SelectEvent event) {
        final JsArray<Selection> selections = _chart.getSelections();

        if (selections == null || selections.length() != 1) {
            // this handler only reacts to single cell selections
            return;
        }

        final Selection selection = selections.get(0);
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
        final String url = Urls.createRelativeUrl("repository" + resultFilePath);

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
}
