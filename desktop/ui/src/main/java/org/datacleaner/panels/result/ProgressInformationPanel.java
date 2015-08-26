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
package org.datacleaner.panels.result;

import java.awt.BorderLayout;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import javax.swing.JTextArea;

import org.apache.metamodel.schema.Table;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.ErrorUtils;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ProgressCounter;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCTaskPaneContainer;
import org.datacleaner.windows.ResultWindow;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.VerticalLayout;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;

/**
 * Panel that shows various progress information widgets within the
 * {@link ResultWindow}.
 */
public class ProgressInformationPanel extends DCPanel {

    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormat.forPattern("HH:mm:ss");

    private final JTextArea _executionLogTextArea;
    private final DCPanel _progressBarPanel;
    private final ConcurrentMap<Table, TableProgressInformationPanel> _tableProgressInformationPanels;
    private final ConcurrentMap<Table, ProgressCounter> _progressTimingCounters;
    private final Stopwatch _stopWatch;

    public ProgressInformationPanel(boolean running) {
        super(WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        setLayout(new BorderLayout());
        _tableProgressInformationPanels = new ConcurrentHashMap<>();
        _progressTimingCounters = new ConcurrentHashMap<>();
        _stopWatch = Stopwatch.createUnstarted();
        _executionLogTextArea = new JTextArea();
        _executionLogTextArea.setText("--- DataCleaner progress information user-log ---");
        _executionLogTextArea.setEditable(false);
        _executionLogTextArea.setBackground(WidgetUtils.COLOR_DEFAULT_BACKGROUND);

        _progressBarPanel = new DCPanel(WidgetUtils.COLOR_ALTERNATIVE_BACKGROUND);
        _progressBarPanel.setLayout(new VerticalLayout(4));

        final JXTaskPane progressTaskPane = WidgetFactory.createTaskPane("Progress", IconUtils.ACTION_EXECUTE);
        progressTaskPane.add(_progressBarPanel);

        final JXTaskPane executionLogTaskPane = WidgetFactory.createTaskPane("Execution log", IconUtils.ACTION_LOG);
        executionLogTaskPane.add(_executionLogTextArea);

        final DCTaskPaneContainer taskPaneContainer = WidgetFactory.createTaskPaneContainer();
        if (running) {
            taskPaneContainer.add(progressTaskPane);
        }
        taskPaneContainer.add(executionLogTaskPane);

        add(WidgetUtils.scrolleable(taskPaneContainer), BorderLayout.CENTER);
    }

    public String getTextAreaText() {
        return _executionLogTextArea.getText();
    }

    private String getTimestamp() {
        return new LocalTime().toString(DATE_TIME_FORMAT);
    }

    public void addUserLog(String string) {
        appendMessage("\n" + getTimestamp() + " INFO: " + string);
    }

    public void addUserLog(String string, Throwable throwable, boolean jobFinished) {
        StringWriter stringWriter = new StringWriter();
        stringWriter.append("\n").append(getTimestamp()).append("ERROR: ");
        stringWriter.append(string);
        if (throwable == null) {
            stringWriter.append('\n');
            stringWriter.append("(No stack trace provided)");
        } else {
            throwable = ErrorUtils.unwrapForPresentation(throwable);

            final String exceptionMessage = throwable.getMessage();
            if (!Strings.isNullOrEmpty(exceptionMessage)) {
                stringWriter.append('\n');
                stringWriter.append('\n');
                stringWriter.append(exceptionMessage);
            }

            stringWriter.append('\n');
            stringWriter.append('\n');
            PrintWriter printWriter = new PrintWriter(stringWriter);
            printStackTrace(printWriter, throwable);
            stringWriter.append('\n');
        }
        appendMessage(stringWriter.toString());

        if (jobFinished) {
            final Collection<TableProgressInformationPanel> tableProgressInformationPanels = _tableProgressInformationPanels
                    .values();
            for (TableProgressInformationPanel tableProgressInformationPanel : tableProgressInformationPanels) {
                tableProgressInformationPanel.setProgressStopped(throwable != null);
            }
        }
    }

    /**
     * Prints stacktraces to the string writer, and investigates the throwable
     * hierarchy to check if there's any {@link SQLException}s which also has
     * "next" exceptions.
     *
     * @param printWriter
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
        WidgetUtils.invokeSwingAction(new Runnable() {
            @Override
            public void run() {
                _executionLogTextArea.append(message);
            }
        });
    }

    public void addProgressBar(final Table table, final int expectedRows) {
        final TableProgressInformationPanel tableProgressInformationPanel = getTableProgressInformationPanel(table,
                expectedRows);
        WidgetUtils.invokeSwingAction(new Runnable() {
            @Override
            public void run() {
                _progressBarPanel.add(tableProgressInformationPanel);
                tableProgressInformationPanel.setProgressMaximum(expectedRows);
                _progressBarPanel.updateUI();
            }
        });
    }

    private TableProgressInformationPanel getTableProgressInformationPanel(Table table, int expectedRows) {
        TableProgressInformationPanel tableProgressInformationPanel = _tableProgressInformationPanels.get(table);
        if (tableProgressInformationPanel == null) {
            if (expectedRows == -1) {
                expectedRows = Integer.MAX_VALUE;
            }
            tableProgressInformationPanel = new TableProgressInformationPanel(table, expectedRows);
            TableProgressInformationPanel previous = _tableProgressInformationPanels.putIfAbsent(table,
                    tableProgressInformationPanel);
            if (previous != null) {
                tableProgressInformationPanel = previous;
            }
        }
        return tableProgressInformationPanel;
    }

    /**
     * Informs the panel that the progress for a table is updated
     *
     * @param table
     * @param currentRow
     */
    public void updateProgress(final Table table, final int currentRow) {
        final TableProgressInformationPanel tableProgressInformationPanel = getTableProgressInformationPanel(table, -1);
        boolean greater = tableProgressInformationPanel.setProgress(currentRow);

        if (!greater) {
            // this may happen because of the multithreaded nature of the
            // execution - sometimes a notification can come in later than
            // previous notifications
            return;
        }

        ProgressCounter counter = _progressTimingCounters.get(table);
        if (counter == null) {
            counter = new ProgressCounter();
            ProgressCounter previous = _progressTimingCounters.put(table, counter);
            if (previous != null) {
                counter = previous;
            }
        }

        final boolean log;
        int previousCount = counter.get();
        if (currentRow - previousCount > 1000) {
            log = counter.setIfSignificantToUser(currentRow);
        } else {
            log = false;
        }
        if (log) {
            addUserLog("Progress of " + table.getName() + ": " + formatNumber(currentRow) + " rows processed");
        }
    }

    private String formatNumber(int number) {
        NumberFormat nf = NumberFormat.getInstance();
        return nf.format(number);
    }

    public void onCancelled() {
        appendMessage("\n--- DataCleaner job cancelled at " + getTimestamp() + " ---");
        Collection<TableProgressInformationPanel> tableProgressInformationPanels = _tableProgressInformationPanels
                .values();
        for (TableProgressInformationPanel tableProgressInformationPanel : tableProgressInformationPanels) {
            tableProgressInformationPanel.setProgressCancelled();
        }
    }

    /**
     * Informs the panel that the progress for a table has finished.
     *
     * @param table
     */
    public void updateProgressFinished(Table table) {
        final TableProgressInformationPanel tableProgressInformationPanel = getTableProgressInformationPanel(table, -1);
        tableProgressInformationPanel.setProgressFinished();
    }

    public void onSuccess() {
        if (_stopWatch.isRunning()) {
            _stopWatch.stop();
            _stopWatch.elapsed(TimeUnit.MINUTES);

            addUserLog("Job success! Elapsed time: " + _stopWatch);
        }

        Collection<TableProgressInformationPanel> tableProgressInformationPanels = _tableProgressInformationPanels
                .values();

        for (TableProgressInformationPanel tableProgressInformationPanel : tableProgressInformationPanels) {
            tableProgressInformationPanel.setProgressFinished();
        }
    }

    public void onBegin() {
        addUserLog("Job begin");
        _stopWatch.start();
    }
}
