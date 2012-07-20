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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import javax.swing.SwingWorker;

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
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.user.MonitorConnection;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.windows.FileTransferProgressWindow;
import org.eobjects.datacleaner.windows.MonitorConnectionDialog;
import org.eobjects.metamodel.util.FileHelper;
import org.jdesktop.swingx.action.OpenBrowserAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract {@link SwingWorker} and {@link ActionListener} for publishing a file
 * to the DataCleaner monitor webapp.
 */
public abstract class PublishFileToMonitorActionListener extends SwingWorker<Map<?, ?>, Task> implements ActionListener {

    private static final Logger logger = LoggerFactory.getLogger(PublishFileToMonitorActionListener.class);
    
    private final WindowContext _windowContext;
    private final UserPreferences _userPreferences;
    private final HttpClient _httpClient;

    private FileTransferProgressWindow _progressWindow;

    public PublishFileToMonitorActionListener(WindowContext windowContext, UserPreferences userPreferences,
            HttpClient httpClient) {
        super();
        _windowContext = windowContext;
        _userPreferences = userPreferences;
        _httpClient = httpClient;
    }

    protected abstract String getTransferredFilename();
    
    protected abstract String getUploadUrl(MonitorConnection monitorConnection);
    
    protected abstract InputStream getTransferStream();
    
    protected abstract long getExpectedSize();
    
    protected boolean openBrowserWhenDone() {
        return false;
    }

    @Override
    public final void actionPerformed(ActionEvent e) {
        MonitorConnection monitorConnection = _userPreferences.getMonitorConnection();
        if (monitorConnection == null) {
            MonitorConnectionDialog dialog = new MonitorConnectionDialog(_windowContext, _userPreferences, _httpClient);
            dialog.open();
        } else {

            _progressWindow = new FileTransferProgressWindow(_windowContext, null,
                    new String[] { getTransferredFilename() });
            _progressWindow.open();

            // start the swing worker
            execute();
        }
    }
    
    @Override
    protected Map<?, ?> doInBackground() throws Exception {

        final MonitorConnection monitorConnection = _userPreferences.getMonitorConnection();
        monitorConnection.prepareClient(_httpClient);

        final String uploadUrl = getUploadUrl(monitorConnection);
        logger.debug("Upload url: {}", uploadUrl);

        final HttpPost request = new HttpPost(uploadUrl);
        
        final long expectedSize = getExpectedSize();

        publish(new Task() {
            @Override
            public void execute() throws Exception {
                _progressWindow.setExpectedSize(getTransferredFilename(), expectedSize);
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
                return expectedSize;
            }

            @Override
            public void writeTo(OutputStream out) throws IOException {
                long progress = 0;
                final InputStream in = getTransferStream();
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
                                _progressWindow.setProgress(getTransferredFilename(), updatedProgress);
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
                return getTransferredFilename();
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
            final InputStream content = response.getEntity().getContent();
            final String contentString;
            try {
                contentString = FileHelper.readInputStreamAsString(content, FileHelper.DEFAULT_ENCODING);
            } finally {
                FileHelper.safeClose(content);
            }
            
            final ObjectMapper objectMapper = new ObjectMapper();
            try {
                final Map<?, ?> responseMap = objectMapper.readValue(contentString, Map.class);
                
                return responseMap;
            } catch (Exception e) {
                logger.warn("Received non-JSON response:\n{}", contentString);
                logger.error("Failed to parse response as JSON", e);
                return null;
            }
        } else {
            logger.warn("Upload response status: {}", statusLine);
            final String reasonPhrase = statusLine.getReasonPhrase();
            WidgetUtils.showErrorMessage("Server reported error",
                    "Server replied with status " + statusLine.getStatusCode() + ":\n" + reasonPhrase, null);
            return null;
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
    protected void done() {
        final Map<?, ?> responseMap;

        try {
            responseMap = get();
        } catch (Exception e) {
            WidgetUtils.showErrorMessage("Error transfering file(s)!", e);
            return;
        }

        _progressWindow.setFinished(getTransferredFilename());

        if (openBrowserWhenDone() && responseMap != null) {
            final MonitorConnection monitorConnection = _userPreferences.getMonitorConnection();
            final OpenBrowserAction openBrowserAction = new OpenBrowserAction(monitorConnection.getBaseUrl()
                    + "/repository" + responseMap.get("repository_path"));
            openBrowserAction.actionPerformed(null);
        }
    }
}
