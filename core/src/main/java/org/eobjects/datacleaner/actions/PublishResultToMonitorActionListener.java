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
package org.eobjects.datacleaner.actions;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.commons.lang.SerializationUtils;
import org.apache.http.client.HttpClient;
import org.eobjects.analyzer.result.AnalysisResult;
import org.eobjects.analyzer.result.SimpleAnalysisResult;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.user.MonitorConnection;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.windows.ResultWindow;
import org.eobjects.metamodel.util.Ref;

/**
 * Action listener invoked when the user clicks the "Publish to dq monitor"
 * button on the {@link ResultWindow}.
 */
public class PublishResultToMonitorActionListener extends PublishFileToMonitorActionListener {

    private final Ref<AnalysisResult> _resultRef;

    // TODO: Fix hardcoding.
    private final String analysisName = "foobar";

    private byte[] _bytes;

    public PublishResultToMonitorActionListener(WindowContext windowContext, UserPreferences userPreferences,
            Ref<AnalysisResult> resultRef, HttpClient httpClient) {
        super(windowContext, userPreferences, httpClient);
        _resultRef = resultRef;
    }

    private byte[] getBytes() {
        if (_bytes == null) {
            final AnalysisResult analysisResult = _resultRef.get();
            _bytes = SerializationUtils.serialize(new SimpleAnalysisResult(analysisResult.getResultMap()));
        }
        return _bytes;
    }

    @Override
    protected String getTransferredFilename() {
        return analysisName;
    }

    @Override
    protected String getUploadUrl(MonitorConnection monitorConnection) {
        return monitorConnection.getBaseUrl() + "/repository/" + monitorConnection.getTenantId() + "/results/" + analysisName;
    }

    @Override
    protected long getExpectedSize() {
        return getBytes().length;
    }

    @Override
    protected InputStream getTransferStream() {
        return new ByteArrayInputStream(getBytes());
    }

    @Override
    protected boolean openBrowserWhenDone() {
        return true;
    }
}
