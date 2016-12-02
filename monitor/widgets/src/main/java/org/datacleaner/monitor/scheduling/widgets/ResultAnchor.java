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

import org.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.datacleaner.monitor.shared.model.TenantIdentifier;
import org.datacleaner.monitor.util.Urls;

import com.google.gwt.user.client.ui.Anchor;

/**
 * An anchor to a result report.
 */
public class ResultAnchor extends Anchor {

    private final TenantIdentifier _tenant;

    public ResultAnchor(final TenantIdentifier tenant) {
        super();
        addStyleName("ResultAnchor");
        _tenant = tenant;
    }

    public void setResult(final ExecutionLog executionLog) {
        setResult(executionLog, null);
    }

    public void setResult(final ExecutionLog executionLog, final String text) {
        final String resultId = executionLog.getResultId();
        if (resultId == null || !executionLog.isResultPersisted()) {
            setEnabled(false);
            setText("");
        } else {
            final String resultFilename = resultId + ".analysis.result.dat";
            final String url = Urls.createRelativeUrl("repository/" + _tenant.getId() + "/results/" + resultFilename);
            setHref(url);
            setTarget("_blank");
            if (text == null) {
                setText(resultId);
            } else {
                setText(text);
            }
        }
    }

}
