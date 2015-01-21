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
package org.datacleaner.monitor.shared.widgets;

import org.datacleaner.monitor.shared.model.JobIdentifier;
import org.datacleaner.monitor.shared.model.MetricIdentifier;
import org.datacleaner.monitor.shared.model.TenantIdentifier;

import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;

/**
 * A textbox with suggestions which the user uses to express string metric
 * parameters.
 */
public class StringParameterizedMetricTextBox extends SuggestBox {

    public StringParameterizedMetricTextBox(TenantIdentifier tenant, JobIdentifier job, MetricIdentifier metric,
            String text, CheckBox checkBoxToActivate) {
        this(text, checkBoxToActivate, new StringParameterizedMetricSuggestOracle(tenant, job, metric));
    }

    public StringParameterizedMetricTextBox(String text, final CheckBox checkBoxToActivate, SuggestOracle suggestOracle) {
        super(suggestOracle);
        addStyleName("StringParameterizedMetricTextBox");
        setText(text);

        getValueBox().addFocusHandler(new FocusHandler() {
            @Override
            public void onFocus(FocusEvent event) {
                showSuggestionList();
            }
        });

        if (checkBoxToActivate != null) {
            getValueBox().addKeyUpHandler(new KeyUpHandler() {
                @Override
                public void onKeyUp(KeyUpEvent event) {
                    final String text = getText();
                    if (text != null && !"".equals(text)) {
                        // activate checkbox whenever something is written.
                        checkBoxToActivate.setValue(true);
                    }
                }
            });
        }
    }

    public void setMetric(TenantIdentifier tenant, JobIdentifier job, MetricIdentifier metric) {
        StringParameterizedMetricSuggestOracle oracle = (StringParameterizedMetricSuggestOracle) getSuggestOracle();
        oracle.setMetric(tenant, job, metric);
    }
}
