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
package org.eobjects.datacleaner.windows;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.DecimalFormat;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import org.eobjects.datacleaner.actions.DownloadFilesActionListener;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.jdesktop.swingx.VerticalLayout;

/**
 * Window showing a progress indicator for a file download. The window is
 * updated by the DownloadFileActionListener, which does the actual download and
 * file handling.
 * 
 * @see DownloadFilesActionListener
 * 
 * @author Kasper Sørensen
 */
public class DownloadProgressWindow extends AbstractDialog {

	private static final long serialVersionUID = 1L;

	private final DownloadFilesActionListener _downloadFilesActionListener;
	private final JLabel[] _currentBytesLabels;
	private final JLabel[] _expectedBytesLabels;
	private final JProgressBar[] _progressIndicators;
	private final JLabel[] _infoLabels;
	private final File[] _files;

	public DownloadProgressWindow(DownloadFilesActionListener downloadFilesActionListener) {
		super();
		setTopBackgroundColor(WidgetUtils.BG_COLOR_DARK);
		setBottomBackgroundColor(WidgetUtils.BG_COLOR_LESS_DARK);

		_files = downloadFilesActionListener.getFiles();

		_progressIndicators = new JProgressBar[_files.length];
		_infoLabels = new JLabel[_files.length];
		_currentBytesLabels = new JLabel[_files.length];
		_expectedBytesLabels = new JLabel[_files.length];

		_downloadFilesActionListener = downloadFilesActionListener;

		for (int i = 0; i < _files.length; i++) {
			File file = _files[i];
			_currentBytesLabels[i] = new JLabel("0");
			_currentBytesLabels[i].setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);

			_expectedBytesLabels[i] = new JLabel("??? bytes");
			_expectedBytesLabels[i].setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);

			_progressIndicators[i] = new JProgressBar(JProgressBar.HORIZONTAL);
			_progressIndicators[i].setMinimum(0);
			_progressIndicators[i].setMaximum(100);
			_progressIndicators[i].setValue(0);

			_infoLabels[i] = new JLabel("Downloading file '" + file.getName() + "'", JLabel.CENTER);
			_infoLabels[i].setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
		}

	}

	@Override
	protected boolean onWindowClosing() {
		boolean close = super.onWindowClosing();
		if (close) {
			_downloadFilesActionListener.cancelDownload();
		}
		return close;
	}

	@Override
	protected String getBannerTitle() {
		return "Downloading...";
	}

	@Override
	protected int getDialogWidth() {
		return 460;
	}

	private int getIndex(File file) {
		for (int i = 0; i < _files.length; i++) {
			if (file.equals(_files[i])) {
				return i;
			}
		}
		return -1;
	}

	public void setProgress(File file, Long bytes) {
		int index = getIndex(file);

		DecimalFormat formatter = new DecimalFormat("###,###");
		_currentBytesLabels[index].setText(formatter.format(bytes));
		_progressIndicators[index].setValue((int) (bytes / 100));
	}

	public void setExpectedSize(File file, Long bytes) {
		int index = getIndex(file);

		DecimalFormat formatter = new DecimalFormat("###,###");
		_expectedBytesLabels[index].setText(formatter.format(bytes) + " bytes");
		_progressIndicators[index].setMaximum((int) (bytes / 100));
	}

	@Override
	protected JComponent getDialogContent() {
		DCPanel centerPanel = new DCPanel();
		centerPanel.setLayout(new VerticalLayout(4));

		for (int i = 0; i < _files.length; i++) {

			final JLabel ofLabel = new JLabel(" of ");
			ofLabel.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);

			final DCPanel textPanel = new DCPanel();
			textPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
			textPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			textPanel.add(_currentBytesLabels[i]);
			textPanel.add(ofLabel);
			textPanel.add(_expectedBytesLabels[i]);

			centerPanel.add(_infoLabels[i]);
			centerPanel.add(_progressIndicators[i]);
			centerPanel.add(textPanel);
		}

		final DCPanel mainPanel = new DCPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(centerPanel, BorderLayout.CENTER);

		return mainPanel;
	}

	@Override
	protected String getWindowTitle() {
		return "Downloading...";
	}

	public void setFinished(File file) {
		int index = getIndex(file);

		final String doneText = "Done!";

		_infoLabels[index].setText(doneText);

		for (int i = 0; i < _infoLabels.length; i++) {
			if (!doneText.equals(_infoLabels[i].getText())) {
				// return if not all files have downloaded
				return;
			}
		}

		final Timer timer = new Timer(1500, null);
		final ActionListener listener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DownloadProgressWindow.this.dispose();
				timer.stop();
			}
		};
		timer.addActionListener(listener);
		timer.start();

	}

}
