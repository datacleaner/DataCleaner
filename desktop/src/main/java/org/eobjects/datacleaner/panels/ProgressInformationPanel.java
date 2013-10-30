/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.panels;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import org.eobjects.analyzer.job.concurrent.PreviousErrorsExistException;
import org.eobjects.datacleaner.util.ProgressCounter;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.DCLabel;
import org.eobjects.datacleaner.widgets.DCProgressBar;
import org.eobjects.datacleaner.widgets.LoadingIcon;
import org.eobjects.metamodel.schema.Table;
import org.jdesktop.swingx.VerticalLayout;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class ProgressInformationPanel extends DCPanel {

    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormat.forPattern("HH:mm:ss");

    private final ImageManager imageManager = ImageManager.getInstance();
    private final JTextArea _textArea = new JTextArea();
    private final DCPanel _progressBarPanel;
    private final Map<Table, DCProgressBar> _progressBars = new IdentityHashMap<Table, DCProgressBar>();
    private final JScrollPane _textAreaScroll;
    private final Map<Table, ProgressCounter> _verboseCounter = new IdentityHashMap<Table, ProgressCounter>();
    private final JButton _stopButton;
    private final LoadingIcon _loadingIcon;
    private final DCLabel _loadingLabel;
    private volatile boolean _verboseLogging = false;

    public ProgressInformationPanel() {
        super();
        setLayout(new BorderLayout());
        _textArea.setText("--- DataCleaner progress information user-log ---");
        _textArea.setEditable(false);
        _textAreaScroll = WidgetUtils.scrolleable(_textArea);
        _textAreaScroll.setBorder(new CompoundBorder(WidgetUtils.BORDER_SHADOW, WidgetUtils.BORDER_THIN));

        _progressBarPanel = new DCPanel(WidgetUtils.BG_COLOR_DARK, WidgetUtils.BG_COLOR_DARK);
        _progressBarPanel.setLayout(new VerticalLayout(4));
        _progressBarPanel.setBorder(WidgetUtils.BORDER_EMPTY);

        _loadingIcon = new LoadingIcon();
        _loadingLabel = DCLabel.bright("Preparing...");
        _loadingLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
        _progressBarPanel.add(_loadingIcon);
        _progressBarPanel.add(_loadingLabel);

        final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(240);
        splitPane.setBorder(null);
        splitPane.add(WidgetUtils.scrolleable(_progressBarPanel));
        splitPane.add(_textAreaScroll);

        final JCheckBox verboseCheckBox = new JCheckBox("Verbose logging?");
        verboseCheckBox.setOpaque(false);
        verboseCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                _verboseLogging = verboseCheckBox.isSelected();
            }
        });

        _stopButton = new JButton("Cancel job", imageManager.getImageIcon("images/actions/stop.png",
                IconUtils.ICON_SIZE_SMALL));
        _stopButton.setMargin(new Insets(1, 1, 1, 1));
        _stopButton.setVisible(false);

        final DCPanel bottomPanel = new DCPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.add(verboseCheckBox, BorderLayout.WEST);
        bottomPanel.add(_stopButton, BorderLayout.EAST);

        add(splitPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public String getTextAreaText() {
        return _textArea.getText();
    }

    public void addStopActionListener(ActionListener actionListener) {
        _stopButton.addActionListener(actionListener);
        _stopButton.setVisible(true);
    }

    private String getTimestamp() {
        return new LocalTime().toString(DATE_TIME_FORMAT);
        // final String now = new DateTime().toString(DATE_TIME_FORMAT);
        // return now;
    }

    public void addUserLog(String string) {
        appendMessage("\n" + getTimestamp() + " INFO: " + string);
    }

    public void addUserLog(String string, Throwable throwable, boolean jobFinished) {
        StringWriter stringWriter = new StringWriter();
        stringWriter.append("\n" + getTimestamp() + "ERROR: ");
        stringWriter.append(string);
        if (throwable == null) {
            stringWriter.append('\n');
            stringWriter.append("(No stack trace provided)");
        } else if (throwable instanceof PreviousErrorsExistException) {
            stringWriter.append(' ');
            stringWriter.append(throwable.getMessage());
        } else {
            stringWriter.append('\n');
            PrintWriter printWriter = new PrintWriter(stringWriter);
            printStackTrace(printWriter, throwable);
        }
        appendMessage(stringWriter.toString());

        if (jobFinished) {
            _stopButton.setEnabled(false);
            _loadingLabel.setText("Stopped!");
            _loadingLabel.setVisible(true);
            _loadingIcon.setVisible(false);
            Collection<DCProgressBar> progressBars = _progressBars.values();
            for (DCProgressBar progressBar : progressBars) {
                progressBar.setEnabled(false);
                progressBar.setString("Stopped!");
            }
        }
    }

    /**
     * Prints stacktraces to the string writer, and investigates the throwable
     * hierarchy to check if there's any {@link SQLException}s which also has
     * "next" exceptions.
     * 
     * @param stringWriter
     * @param throwable
     */
    protected void printStackTrace(PrintWriter printWriter, Throwable throwable) {
        throwable.printStackTrace(printWriter);
        Throwable cause = throwable.getCause();
        while (cause != null) {
            if (cause instanceof SQLException) {
                SQLException nextException = ((SQLException) cause).getNextException();
                if (nextException != null) {
                    printWriter.print("Next exception: ");
                    printStackTrace(printWriter, nextException);
                }
            }
            cause = cause.getCause();
        }
    }

    private void appendMessage(final String message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                _textArea.append(message);

                // moves the vertical scroll to the bottom
                JScrollBar verticalScrollBar = _textAreaScroll.getVerticalScrollBar();
                verticalScrollBar.setValue(verticalScrollBar.getMaximum());
            }
        });
    }

    public void setExpectedRows(final Table table, final int expectedRows) {
        final DCProgressBar progressBar = getProgressBar(table, expectedRows);
        final DCLabel tableLabel = DCLabel.bright(table.getName());
        final DCLabel rowsLabel = DCLabel.bright("Approx. " + expectedRows + " rows");
        rowsLabel.setFont(WidgetUtils.FONT_SMALL);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                synchronized (_progressBarPanel) {
                    if (_progressBarPanel.getComponentCount() == 0) {
                        _progressBarPanel.add(Box.createVerticalStrut(10));
                    }

                    _progressBarPanel.add(tableLabel);
                    _progressBarPanel.add(rowsLabel);
                    _progressBarPanel.add(progressBar);
                    _progressBarPanel.updateUI();
                }
            }
        });
    }

    private DCProgressBar getProgressBar(Table table, int expectedRows) {

        synchronized (_progressBars) {
            DCProgressBar progressBar = _progressBars.get(table);
            if (progressBar == null) {
                if (expectedRows == -1) {
                    expectedRows = Integer.MAX_VALUE;
                }
                progressBar = new DCProgressBar(0, expectedRows);
                _progressBars.put(table, progressBar);

                // remove loading indicators
                _loadingIcon.setVisible(false);
                _loadingLabel.setVisible(false);
            } else {
                if (expectedRows != -1) {
                    progressBar.setMaximum(expectedRows);
                }
            }
            return progressBar;
        }
    }

    /**
     * Informs the panel that the progress for a table is updated
     * 
     * @param table
     * @param currentRow
     */
    public void updateProgress(final Table table, final int currentRow) {
        final DCProgressBar progressBar = getProgressBar(table, -1);
        boolean greater = progressBar.setValueIfGreater(currentRow);

        if (!greater) {
            // this may happen because of the multithreaded nature of the
            // execution - sometimes a notification can come in later than
            // previous notifications
            return;
        }

        if (_verboseLogging) {
            final ProgressCounter counter;
            synchronized (_verboseCounter) {
                ProgressCounter previousCount = _verboseCounter.get(table);
                if (previousCount == null) {
                    previousCount = new ProgressCounter();
                    _verboseCounter.put(table, previousCount);
                }
                counter = previousCount;
            }

            final boolean log;
            int previousCount = counter.get();
            if (currentRow - previousCount > 1000) {
                log = counter.setIfSignificantToUser(currentRow);
            } else {
                log = false;
            }
            if (log) {
                addUserLog("Progress for table '" + table.getName() + "': Row no. " + currentRow);
            }
        }
    }

    /**
     * Informs the panel that the progress for a table has finished.
     * 
     * @param table
     */
    public void updateProgressFinished(Table table) {
        final DCProgressBar progressBar = getProgressBar(table, -1);
        progressBar.setValueIfGreater(progressBar.getMaximum());
    }

    public void onSuccess() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                _loadingIcon.setVisible(false);
                _loadingLabel.setVisible(false);
                _stopButton.setEnabled(false);
                Collection<DCProgressBar> progressBars = _progressBars.values();
                for (DCProgressBar progressBar : progressBars) {
                    int maximum = progressBar.getMaximum();
                    progressBar.setValueIfGreater(maximum);
                }
            }
        });
    }

}
