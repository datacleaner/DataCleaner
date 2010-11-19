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

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.eobjects.analyzer.job.tasks.Task;
import org.eobjects.datacleaner.util.HttpUtils;
import org.eobjects.datacleaner.windows.DownloadProgressWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ActionListener and SwingWorker implementation for handling download of a
 * file. The progress will be displayed in a new window.
 * 
 * @author Kasper Sørensen
 */
public class DownloadFileActionListener extends SwingWorker<File[], Task> implements ActionListener {

	private static final Logger logger = LoggerFactory.getLogger(DownloadFileActionListener.class);

	private final String[] _urls;
	private final File[] _files;
	private final FileDownloadListener _listener;
	private final DownloadProgressWindow _downloadProgressWindow;
	private volatile boolean _cancelled = false;

	public DownloadFileActionListener(String[] urls, FileDownloadListener listener) {
		if (urls == null) {
			throw new IllegalArgumentException("urls cannot be null");
		}
		_urls = urls;
		_listener = listener;
		_files = new File[_urls.length];
		for (int i = 0; i < urls.length; i++) {
			String url = _urls[i];
			if (url == null) {
				throw new IllegalArgumentException("urls[" + i + "] cannot be null");
			}
			String filename = url.substring(url.lastIndexOf('/') + 1);
			_files[i] = new File(filename);
		}
		_downloadProgressWindow = new DownloadProgressWindow(this);
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
	protected void process(List<Task> chunks) {
		for (Task task : chunks) {
			try {
				task.execute();
			} catch (Exception e) {
				// should never happen
				throw new IllegalArgumentException(e);
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
			_listener.onFilesDownloaded(_files);
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

				final HttpClient httpClient = HttpUtils.getHttpClient();
				final HttpGet method = new HttpGet(url);

				if (!_cancelled) {
					final HttpResponse response = httpClient.execute(method);

					final long expectedSize = response.getEntity().getContentLength();
					publish(new Task() {
						@Override
						public void execute() throws Exception {
							_downloadProgressWindow.setExpectedSize(file, expectedSize);
						}
					});

					inputStream = response.getEntity().getContent();
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
