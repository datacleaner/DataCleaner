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
package org.datacleaner.monitor.scheduling.widgets;

import java.util.Date;

import org.datacleaner.monitor.scheduling.model.ExecutionIdentifier;
import org.datacleaner.monitor.scheduling.model.ExecutionStatus;
import org.datacleaner.monitor.scheduling.model.TriggerType;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * {@link Cell} to present an {@link ExecutionIdentifier}
 */
final class ExecutionIdentifierCell extends AbstractCell<ExecutionIdentifier> {

    private final DateTimeFormat _format = DateTimeFormat.getFormat(PredefinedFormat.DATE_SHORT);

    @Override
    public void render(com.google.gwt.cell.client.Cell.Context context, ExecutionIdentifier executionIdentifier,
            SafeHtmlBuilder sb) {
        sb.appendHtmlConstant("<div class=\"ExecutionIdentifier\">");

        final Date beginDate = executionIdentifier.getJobBeginDate();
        final String dateString;
        if (beginDate == null) {
            dateString = "(n/a)";
        } else {
            dateString = _format.format(beginDate);
        }

        // date
        sb.appendHtmlConstant("<span class=\"beginDate\">");
        sb.appendEscaped(dateString);
        sb.appendHtmlConstant("</span>");

        // trigger type
        sb.appendHtmlConstant("<span class=\"triggerTypes\">");
        final TriggerType triggerType = executionIdentifier.getTriggerType();
        if (triggerType == null) {
            sb.appendEscaped("(n/a)");
        } else {
            sb.appendEscaped(triggerType.toString());
        }
        sb.appendHtmlConstant("</span>");

        // execution status
        sb.appendHtmlConstant("<span class=\"executionStatus\">");
        final ExecutionStatus executionStatus = executionIdentifier.getExecutionStatus();
        if (executionStatus == null) {
            sb.appendEscaped("(n/a)");
        } else {
            sb.appendEscaped(executionStatus.toString());
        }
        sb.appendHtmlConstant("</span>");

        sb.appendHtmlConstant("</div>");
    }
}
