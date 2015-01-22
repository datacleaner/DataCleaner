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
package org.datacleaner.windows;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import org.apache.commons.lang.ArrayUtils;
import org.apache.metamodel.util.Action;
import org.datacleaner.actions.DownloadFilesActionListener;
import org.datacleaner.actions.PublishResultToMonitorActionListener;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCProgressBar;
import org.jdesktop.swingx.VerticalLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Window showing a progress indicator for a file transfer (typically over the
 * network). The window is updated externally, by the code that does the actual
 * download and file handling.
 * 
 * @see DownloadFilesActionListener
 * @see PublishResultToMonitorActionListener
 */
public class FileTransferProgressWindow extends AbstractDialog {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(FileTransferProgressWindow.class);

    private final Action<Void> _cancelCallback;
    private final JLabel[] _currentBytesLabels;
    private final JLabel[] _expectedBytesLabels;
    private final DCProgressBar[] _progressBars;
    private final JLabel[] _infoLabels;
    private final String[] _filenames;

    public FileTransferProgressWindow(WindowContext windowContext, Action<Void> cancelCallback, String[] filenames) {
        super(windowContext);
        setBackgroundColor(WidgetUtils.COLOR_ALTERNATIVE_BACKGROUND);

        _filenames = filenames;

        _progressBars = new DCProgressBar[_filenames.length];
        _infoLabels = new JLabel[_filenames.length];
        _currentBytesLabels = new JLabel[_filenames.length];
        _expectedBytesLabels = new JLabel[_filenames.length];

        _cancelCallback = cancelCallback;

        for (int i = 0; i < _filenames.length; i++) {
            final String filename = _filenames[i];
            _currentBytesLabels[i] = new JLabel("0");
            _currentBytesLabels[i].setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);

            _expectedBytesLabels[i] = new JLabel("??? bytes");
            _expectedBytesLabels[i].setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);

            _progressBars[i] = new DCProgressBar(0, 100);

            _infoLabels[i] = new JLabel("Transfering file '" + filename + "'", JLabel.CENTER);
            _infoLabels[i].setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
        }

    }

    @Override
    protected boolean onWindowClosing() {
        final boolean close = super.onWindowClosing();
        if (close) {
            if (_cancelCallback != null) {
                try {
                    _cancelCallback.run(null);
                } catch (Exception e) {
                    logger.error("Cancelling file transfer threw exception", e);
                }
            }
        }
        return close;
    }

    @Override
    protected String getBannerTitle() {
        return "Transfering...";
    }

    @Override
    protected int getDialogWidth() {
        return 460;
    }

    private int getIndex(String filename) {
        return ArrayUtils.indexOf(_filenames, filename);
    }

    public void setProgress(String filename, Long bytes) {
        final int index = getIndex(filename);

        final DecimalFormat formatter = new DecimalFormat("###,###");
        _currentBytesLabels[index].setText(formatter.format(bytes));
        final DCProgressBar progressBar = _progressBars[index];
        progressBar.setValueIfGreater((int) (bytes / 100));
    }

    public void setExpectedSize(String filename, Long bytes) {
        final int index = getIndex(filename);

        final DecimalFormat formatter = new DecimalFormat("###,###");
        _expectedBytesLabels[index].setText(formatter.format(bytes) + " bytes");
        _progressBars[index].setMaximum((int) (bytes / 100));
    }

    @Override
    protected JComponent getDialogContent() {
        final DCPanel centerPanel = new DCPanel();
        centerPanel.setLayout(new VerticalLayout(4));

        for (int i = 0; i < _filenames.length; i++) {

            final JLabel ofLabel = new JLabel(" of ");
            ofLabel.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);

            final DCPanel textPanel = new DCPanel();
            textPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
            textPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
            textPanel.add(_currentBytesLabels[i]);
            textPanel.add(ofLabel);
            textPanel.add(_expectedBytesLabels[i]);

            centerPanel.add(_infoLabels[i]);
            centerPanel.add(_progressBars[i]);
            centerPanel.add(textPanel);
        }

        final DCPanel mainPanel = new DCPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        return mainPanel;
    }

    @Override
    public String getWindowTitle() {
        return "Transfering...";
    }

    public void setFinished(String filename) {
        final int index = getIndex(filename);

        final String doneText = "Done!";

        _infoLabels[index].setText(doneText);

        for (int i = 0; i < _infoLabels.length; i++) {
            if (!doneText.equals(_infoLabels[i].getText())) {
                // return if not all files have transfered
                return;
            }
        }

        final Timer timer = new Timer(1500, null);
        final ActionListener listener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileTransferProgressWindow.this.dispose();
                timer.stop();
            }
        };
        timer.addActionListener(listener);
        timer.start();
    }

}
