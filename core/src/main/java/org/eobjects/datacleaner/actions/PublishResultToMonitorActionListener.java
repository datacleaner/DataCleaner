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
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import org.apache.commons.lang.SerializationUtils;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MIME;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.AbstractContentBody;
import org.apache.http.entity.mime.content.ContentBody;
import org.codehaus.jackson.map.ObjectMapper;
import org.eobjects.analyzer.result.AnalysisResult;
import org.eobjects.analyzer.result.SimpleAnalysisResult;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.user.MonitorConnection;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.windows.FileTransferProgressWindow;
import org.eobjects.datacleaner.windows.MonitorConnectionDialog;
import org.eobjects.datacleaner.windows.ResultWindow;
import org.eobjects.metamodel.util.Ref;
import org.jdesktop.swingx.action.OpenBrowserAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action listener invoked when the user clicks the "Publish to dq monitor"
 * button on the {@link ResultWindow}.
 */
public class PublishResultToMonitorActionListener implements ActionListener {

    private static final Logger logger = LoggerFactory.getLogger(PublishResultToMonitorActionListener.class);

    private final WindowContext _windowContext;
    private final UserPreferences _userPreferences;
    private final Ref<AnalysisResult> _resultRef;
    private final HttpClient _httpClient;

    public PublishResultToMonitorActionListener(WindowContext windowContext, UserPreferences userPreferences,
            Ref<AnalysisResult> resultRef, HttpClient httpClient) {
        _windowContext = windowContext;
        _userPreferences = userPreferences;
        _resultRef = resultRef;
        _httpClient = httpClient;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        MonitorConnection monitorConnection = _userPreferences.getMonitorConnection();
        if (monitorConnection == null) {
            MonitorConnectionDialog dialog = new MonitorConnectionDialog(_windowContext, _userPreferences, _httpClient);
            dialog.open();
        } else {

            // TODO: Fix hardcoding.
            final String analysisName = "foobar";
            final String tenantId = "DC";

            final String uploadUrl = monitorConnection.getBaseUrl() + "/repository/" + tenantId + "/results/"
                    + analysisName;
            logger.debug("Upload url: {}", uploadUrl);

            final HttpPost request = new HttpPost(uploadUrl);
            final AnalysisResult analysisResult = _resultRef.get();

            final byte[] bytes = SerializationUtils.serialize(new SimpleAnalysisResult(analysisResult.getResultMap()));

            final FileTransferProgressWindow progressWindow = new FileTransferProgressWindow(_windowContext, null,
                    new String[] { analysisName });
            progressWindow.setExpectedSize(analysisName, (long) bytes.length);
            progressWindow.open();

            final MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            final ContentBody uploadFilePart = new AbstractContentBody("application/octet-stream") {

                @Override
                public String getCharset() {
                    return null;
                }

                @Override
                public String getTransferEncoding() {
                    return MIME.ENC_BINARY;
                }

                @Override
                public long getContentLength() {
                    return bytes.length;
                }

                @Override
                public void writeTo(OutputStream out) throws IOException {
                    long progress = 0;
                    final ByteArrayInputStream in = new ByteArrayInputStream(bytes);
                    try {
                        byte[] tmp = new byte[4096];
                        int length;
                        while ((length = in.read(tmp)) != -1) {
                            out.write(tmp, 0, length);

                            // update the visual progress
                            progress = progress + length;
                            progressWindow.setProgress(analysisName, progress);
                        }
                        out.flush();
                    } finally {
                        in.close();
                    }
                }

                @Override
                public String getFilename() {
                    return analysisName;
                }
            };

            entity.addPart("file", uploadFilePart);
            request.setEntity(entity);

            final Map<?, ?> responseMap;
            try {

                final HttpResponse response = _httpClient.execute(request);
                final StatusLine statusLine = response.getStatusLine();

                progressWindow.setFinished(analysisName);

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
