/**
 *  This file is part of DataCleaner.
 *
 *  DataCleaner is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DataCleaner is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DataCleaner.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.eobjects.datacleaner.gui.windows;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dk.eobjects.datacleaner.gui.GuiBuilder;
import dk.eobjects.datacleaner.gui.GuiHelper;

public class DownloadDialog extends JDialog {

	private static final long serialVersionUID = 4508004420458673048L;
	private static final Log _log = LogFactory.getLog(DownloadDialog.class);
	private String _downloadUrl;
	private File _file;
	private ActionListener _completeAction;
	private JLabel _statusLabel;
	private long _bytes;
	private boolean _finished = false;
	private boolean _disposed = false;

	public DownloadDialog(String downloadUrl, File file) {
		super();
		_downloadUrl = downloadUrl;
		_file = file;

		setLayout(new BorderLayout());
		setBackground(Color.WHITE);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setTitle("Downloading: " + _downloadUrl);
		setSize(250, 180);
		setResizable(false);
		GuiHelper.centerOnScreen(this);

		JPanel topPanel = GuiHelper.createPanel().applyBackground(Color.WHITE)
				.applyBorderLayout().toComponent();
		JLabel downloadingLabel = new GuiBuilder<JLabel>(new JLabel(
				"Downloading:")).applyBackground(
				Color.WHITE).applyHeaderFont().toComponent();
		downloadingLabel.setOpaque(true);
		downloadingLabel.setBorder(new EmptyBorder(4, 4, 4, 4));
		topPanel.add(downloadingLabel, BorderLayout.NORTH);
		JLabel fileLabel = new GuiBuilder<JLabel>(new JLabel(_file.getName()))
				.applyBackground(Color.WHITE).applyNormalFont().toComponent();
		fileLabel.setOpaque(true);
		fileLabel.setBorder(new EmptyBorder(4, 4, 4, 4));
		topPanel.add(fileLabel, BorderLayout.CENTER);
		add(topPanel, BorderLayout.NORTH);

		JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		centerPanel.setBackground(Color.WHITE);
		ImageIcon workingIcon = GuiHelper
				.getImageIcon("images/working.gif");
		JLabel workingIconLabel = new JLabel(workingIcon);
		workingIconLabel.setBorder(new EmptyBorder(4, 4, 4, 4));
		workingIcon.setImageObserver(workingIconLabel);
		centerPanel.add(workingIconLabel);
		add(centerPanel, BorderLayout.CENTER);

		_statusLabel = new GuiBuilder<JLabel>(new JLabel("Read " + _bytes
				+ " bytes")).applyBackground(Color.WHITE).applyNormalFont()
				.toComponent();
		_statusLabel.setOpaque(true);
		_statusLabel.setBorder(new EmptyBorder(4, 4, 4, 4));
		add(_statusLabel, BorderLayout.SOUTH);
	}

	public void download() {
		InputStream inputStream = null;
		OutputStream outputStream = null;
		try {
			byte[] buffer = new byte[1024];
			outputStream = new FileOutputStream(_file);
			URL url = new URL(_downloadUrl);
			inputStream = url.openStream();
			_log.info("Begin download: " + _downloadUrl);
			_log.info("Saving as: " + _file.getAbsolutePath());
			for (int numChars = inputStream.read(buffer); numChars != -1; numChars = inputStream
					.read(buffer)) {
				if (_disposed) {
					break;
				}
				outputStream.write(buffer, 0, numChars);
				_bytes += numChars;
				_statusLabel.setText("Read " + _bytes + " bytes");
			}
			_log.info("End download: " + _downloadUrl);
			_finished = true;
		} catch (IOException e) {
			_log.error(e);
		} finally {
			_statusLabel.setText("Read " + _bytes + " bytes - Done!");
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					_log.warn("Could not close reader: " + e.getMessage(), e);
				}
			}
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					_log.warn("Could not close writer: " + e.getMessage(), e);
				}
			}
		}
		if (!_disposed) {
			_completeAction.actionPerformed(null);
			dispose();
		}
	}

	public void setCompleteAction(ActionListener actionListener) {
		_completeAction = actionListener;
	}

	@Override
	public void dispose() {
		super.dispose();
		_disposed = true;
		if (!_finished) {
			_log.info("Cancelling download: " + _downloadUrl);
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
			}
			_log.info("Deleting file: " + _file.getAbsolutePath());
			_file.delete();
		}
	}
}
