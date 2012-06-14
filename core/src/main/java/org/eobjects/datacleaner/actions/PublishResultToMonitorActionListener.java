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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.util.Map;

import org.apache.commons.lang.SerializationUtils;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.eobjects.analyzer.result.AnalysisResult;
import org.eobjects.analyzer.result.SimpleAnalysisResult;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.user.MonitorConnection;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.util.FileFilters;
import org.eobjects.datacleaner.windows.MonitorConnectionDialog;
import org.eobjects.metamodel.util.Ref;
import org.jdesktop.swingx.action.OpenBrowserAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PublishResultToMonitorActionListener implements ActionListener {

    private static final Logger logger = LoggerFactory.getLogger(PublishResultToMonitorActionListener.class);

    private final WindowContext _windowContext;
    private final UserPreferences _userPreferences;
    private final Ref<AnalysisResult> _resultRef;

    public PublishResultToMonitorActionListener(WindowContext windowContext, UserPreferences userPreferences,
            Ref<AnalysisResult> resultRef) {
        _windowContext = windowContext;
        _userPreferences = userPreferences;
        _resultRef = resultRef;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        MonitorConnection monitorConnection = _userPreferences.getMonitorConnection();
        if (monitorConnection == null) {
            MonitorConnectionDialog dialog = new MonitorConnectionDialog(_windowContext, _userPreferences);
            dialog.open();
        } else {

            // TODO: Fix hardcoding.
            final String analysisName = "foobar";
            final String tenantId = "DC";

            final String uploadUrl = monitorConnection.getBaseUrl() + "/repository/" + tenantId + "/results/"
                    + analysisName;
            logger.debug("Upload url: {}", uploadUrl);

            final HttpClient client = new DefaultHttpClient();
            final HttpPost request = new HttpPost(uploadUrl);
            final AnalysisResult analysisResult = _resultRef.get();

            byte[] bytes = SerializationUtils.serialize(new SimpleAnalysisResult(analysisResult.getResultMap()));

            final MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            final InputStreamBody uploadFilePart = new InputStreamBody(new ByteArrayInputStream(bytes),
                    "application/octet-stream", analysisName + FileFilters.ANALYSIS_RESULT_SER.getExtension());
            entity.addPart("file", uploadFilePart);
            request.setEntity(entity);

            final Map<?, ?> responseMap;
            try {

                final HttpResponse response = client.execute(request);
                final StatusLine statusLine = response.getStatusLine();

                if (statusLine.getStatusCode() != 200) {
                    logger.warn("Upload response status: {}", statusLine);
                } else {
                    logger.info("Upload response status: {}", statusLine);
                }

                // parse the response as a JSON map
                responseMap = new ObjectMapper().readValue(response.getEntity().getContent(), Map.class);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }

            final OpenBrowserAction openBrowserAction = new OpenBrowserAction(monitorConnection.getBaseUrl()
                    + "/repository" + responseMap.get("repository_path"));
            openBrowserAction.actionPerformed(event);
        }
    }
}
