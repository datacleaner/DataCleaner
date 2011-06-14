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
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.swing.SwingWorker;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.eobjects.analyzer.job.tasks.Task;
import org.eobjects.datacleaner.bootstrap.WindowManager;
import org.eobjects.datacleaner.user.DataCleanerHome;
import org.eobjects.datacleaner.util.HttpXmlUtils;
import org.eobjects.datacleaner.util.InvalidHttpResponseException;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.windows.DownloadProgressWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ActionListener and SwingWorker implementation for handling download of a
 * file. The progress will be displayed in a new window.
 * 
 * @author Kasper Sørensen
 */
public class DownloadFilesActionListener extends SwingWorker<File[], Task> implements ActionListener {

	private static final Logger logger = LoggerFactory.getLogger(DownloadFilesActionListener.class);

	private final String[] _urls;
	private final File[] _files;
	private final FileDownloadListener _listener;
	private final DownloadProgressWindow _downloadProgressWindow;
	private volatile boolean _cancelled = false;

	public DownloadFilesActionListener(String[] urls, FileDownloadListener listener, WindowManager windowManager) {
		this(urls, createTargetFilenames(urls), listener, windowManager);
	}

	public DownloadFilesActionListener(String[] urls, String[] targetFilenames, FileDownloadListener listener,
			WindowManager windowManager) {
		if (urls == null) {
			throw new IllegalArgumentException("urls cannot be null");
		}
		_urls = urls;
		_listener = listener;
		_files = new File[_urls.length];
		for (int i = 0; i < urls.length; i++) {
			String filename = targetFilenames[i];
			_files[i] = new File(DataCleanerHome.get(), filename);
		}
		_downloadProgressWindow = new DownloadProgressWindow(this, windowManager);
	}

	private static String[] createTargetFilenames(String[] urls) {
		String[] filenames = new String[urls.length];
		for (int i = 0; i < urls.length; i++) {
			String url = urls[i];
			if (url == null) {
				throw new IllegalArgumentException("urls[" + i + "] cannot be null");
			}
			String filename = url.substring(url.lastIndexOf('/') + 1);
			filenames[i] = filename;
		}
		return filenames;
	}

	public File[] getFiles() {
		return _files;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		_downloadProgressWindow.setVisible(true);
		execute();
	}

	@Override
	protected void process(final List<Task> chunks) {
		for (Task task : chunks) {
			try {
				task.execute();
			} catch (Exception e) {
				WidgetUtils.showErrorMessage("Error processing file chunk: " + task, e);
			}
		}
	}

	public void cancelDownload() {
		logger.info("Cancel of download requested");
		_cancelled = true;
	}

	@Override
	protected void done() {
		super.done();
		if (!_cancelled) {
			try {
				File[] files = get();
				_listener.onFilesDownloaded(files);
			} catch (Throwable e) {
				WidgetUtils.showErrorMessage("Error processing file!", e);
			}
		}
	}

	@Override
	protected File[] doInBackground() throws Exception {
		for (int i = 0; i < _urls.length; i++) {
			final String url = _urls[i];
			final File file = _files[i];

			InputStream inputStream = null;
			OutputStream outputStream = null;

			try {
				byte[] buffer = new byte[1024];

				final HttpClient httpClient = HttpXmlUtils.getHttpClient();
				final HttpGet method = new HttpGet(url);

				if (!_cancelled) {
					final HttpResponse response = httpClient.execute(method);

					if (response.getStatusLine().getStatusCode() != 200) {
						throw new InvalidHttpResponseException(response);
					}

					final HttpEntity responseEntity = response.getEntity();
					final long expectedSize = responseEntity.getContentLength();
					publish(new Task() {
						@Override
						public void execute() throws Exception {
							_downloadProgressWindow.setExpectedSize(file, expectedSize);
						}
					});

					inputStream = responseEntity.getContent();
					outputStream = new BufferedOutputStream(new FileOutputStream(file));

					long bytes = 0;
					for (int numBytes = inputStream.read(buffer); numBytes != -1; numBytes = inputStream.read(buffer)) {
						if (_cancelled) {
							break;
						}
						outputStream.write(buffer, 0, numBytes);
						bytes += numBytes;

						final long totalBytes = bytes;
						publish(new Task() {
							@Override
							public void execute() throws Exception {
								_downloadProgressWindow.setProgress(file, totalBytes);
							}
						});
					}

					if (!_cancelled) {
						publish(new Task() {
							@Override
							public void execute() throws Exception {
								_downloadProgressWindow.setFinished(file);
							}
						});
					}
				}
			} catch (IOException e) {
				throw new IllegalStateException(e);
			} finally {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (IOException e) {
						logger.warn("Could not close input stream: " + e.getMessage(), e);
					}
				}
				if (outputStream != null) {
					try {
						outputStream.flush();
						outputStream.close();
					} catch (IOException e) {
						logger.warn("Could not flush & close output stream: " + e.getMessage(), e);
					}
				}
			}

			if (_cancelled) {
				logger.info("Deleting non-finished download-file '{}'", file);
				file.delete();
			}
		}

		return _files;
	}
}
