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
import java.util.List;
import java.util.Map;

import javax.swing.SwingWorker;

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
import org.eobjects.analyzer.job.tasks.Task;
import org.eobjects.analyzer.result.AnalysisResult;
import org.eobjects.analyzer.result.SimpleAnalysisResult;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.user.MonitorConnection;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.util.WidgetUtils;
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
public class PublishResultToMonitorActionListener extends SwingWorker<Map<?, ?>, Task> implements ActionListener {

    private static final Logger logger = LoggerFactory.getLogger(PublishResultToMonitorActionListener.class);

    private final WindowContext _windowContext;
    private final UserPreferences _userPreferences;
    private final Ref<AnalysisResult> _resultRef;
    private final HttpClient _httpClient;

    private FileTransferProgressWindow _progressWindow;

    // TODO: Fix hardcoding.
    private final String analysisName = "foobar";
    private final String tenantId = "DC";

    public PublishResultToMonitorActionListener(WindowContext windowContext, UserPreferences userPreferences,
            Ref<AnalysisResult> resultRef, HttpClient httpClient) {
        super();
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

            _progressWindow = new FileTransferProgressWindow(_windowContext, null, new String[] { analysisName });
            _progressWindow.open();

            // start the swing worker
            execute();
        }
    }

    @Override
    protected void process(List<Task> chunks) {
        for (Task task : chunks) {
            try {
                task.execute();
            } catch (Exception e) {
                WidgetUtils.showErrorMessage("Error processing transfer chunk: " + task, e);
            }
        }
    }

    @Override
    protected Map<?, ?> doInBackground() throws Exception {

        final MonitorConnection monitorConnection = _userPreferences.getMonitorConnection();
        monitorConnection.prepareClient(_httpClient);

        final String uploadUrl = monitorConnection.getBaseUrl() + "/repository/" + tenantId + "/results/"
                + analysisName;
        logger.debug("Upload url: {}", uploadUrl);

        final HttpPost request = new HttpPost(uploadUrl);

        final AnalysisResult analysisResult = _resultRef.get();

        final byte[] bytes = SerializationUtils.serialize(new SimpleAnalysisResult(analysisResult.getResultMap()));

        publish(new Task() {
            @Override
            public void execute() throws Exception {
                _progressWindow.setExpectedSize(analysisName, (long) bytes.length);
            }
        });

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

                        final long updatedProgress = progress;

                        publish(new Task() {
                            @Override
                            public void execute() throws Exception {
                                _progressWindow.setProgress(analysisName, updatedProgress);
                            }
                        });
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

        final HttpResponse response;
        try {
            response = _httpClient.execute(request);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        final StatusLine statusLine = response.getStatusLine();

        if (statusLine.getStatusCode() == 200) {
            logger.info("Upload response status: {}", statusLine);

            // parse the response as a JSON map
            final Map<?, ?> responseMap = new ObjectMapper().readValue(response.getEntity().getContent(), Map.class);

            return responseMap;
        } else {
            logger.warn("Upload response status: {}", statusLine);
            final String reasonPhrase = statusLine.getReasonPhrase();
            WidgetUtils.showErrorMessage("Server reported error",
                    "Server replied with status " + statusLine.getStatusCode() + ":\n" + reasonPhrase, null);
            return null;
        }
    }

    @Override
    protected void done() {
        final Map<?, ?> responseMap;

        try {
            responseMap = get();
        } catch (Exception e) {
            WidgetUtils.showErrorMessage("Error transfering file(s)!", e);
            return;
        }

        _progressWindow.setFinished(analysisName);

        if (responseMap != null) {
            final MonitorConnection monitorConnection = _userPreferences.getMonitorConnection();
            final OpenBrowserAction openBrowserAction = new OpenBrowserAction(monitorConnection.getBaseUrl()
                    + "/repository" + responseMap.get("repository_path"));
            openBrowserAction.actionPerformed(null);
        }
    }
}
