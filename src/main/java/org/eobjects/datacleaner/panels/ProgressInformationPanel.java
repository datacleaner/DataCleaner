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
package org.eobjects.datacleaner.panels;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.IdentityHashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.DCLabel;
import org.eobjects.datacleaner.widgets.DCProgressBar;
import org.eobjects.datacleaner.widgets.LoadingIcon;
import org.eobjects.metamodel.schema.Table;
import org.jdesktop.swingx.VerticalLayout;

public class ProgressInformationPanel extends DCPanel {

	private static final long serialVersionUID = 1L;

	private final JTextArea _textArea = new JTextArea();
	private final DCPanel _progressBarPanel;
	private final Map<Table, DCProgressBar> _progressBars = new IdentityHashMap<Table, DCProgressBar>();
	private final JScrollPane _textAreaScroll;
	private volatile boolean _verboseLogging = false;
	private final Map<Table, Integer> _verboseCounter = new IdentityHashMap<Table, Integer>();

	public ProgressInformationPanel() {
		super();
		setLayout(new BorderLayout());
		_textArea.setText("--- DataCleaner progress information user-log ---");
		_textArea.setEditable(false);
		_textAreaScroll = WidgetUtils.scrolleable(_textArea);
		_textAreaScroll.setBorder(WidgetUtils.BORDER_THIN);

		_progressBarPanel = new DCPanel(WidgetUtils.BG_COLOR_DARK, WidgetUtils.BG_COLOR_DARK);
		_progressBarPanel.setLayout(new VerticalLayout(4));
		_progressBarPanel.setBorder(WidgetUtils.BORDER_EMPTY);

		_progressBarPanel.add(new LoadingIcon());
		_progressBarPanel.add(DCLabel.bright("Preparing..."));

		final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setDividerLocation(190);
		splitPane.setBorder(null);
		splitPane.add(WidgetUtils.scrolleable(_progressBarPanel));
		splitPane.add(_textAreaScroll);

		final JCheckBox verboseCheckBox = new JCheckBox("Verbose logging?");
		verboseCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				_verboseLogging = verboseCheckBox.isSelected();
			}
		});

		add(splitPane, BorderLayout.CENTER);
		add(verboseCheckBox, BorderLayout.SOUTH);
	}

	public void addUserLog(String string) {
		appendMessage("\nINFO: " + string);
	}

	public void addUserLog(String string, Throwable throwable) {
		StringWriter stringWriter = new StringWriter();
		stringWriter.append("\nERROR: ");
		stringWriter.append(string);
		stringWriter.append('\n');
		throwable.printStackTrace(new PrintWriter(stringWriter));
		appendMessage(stringWriter.toString());
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
				if (_progressBars.isEmpty()) {
					_progressBarPanel.removeAll();
				}
				_progressBars.put(table, progressBar);
			} else {
				if (expectedRows != -1) {
					progressBar.setMaximum(expectedRows);
				}
			}
			return progressBar;
		}
	}

	public void updateProgress(final Table table, final int currentRow) {
		final DCProgressBar progressBar = getProgressBar(table, -1);
		progressBar.setValueIfHigherAndSignificant(currentRow);

		if (_verboseLogging) {
			boolean log = false;
			synchronized (_verboseCounter) {
				Integer previousCount = _verboseCounter.get(table);
				if (previousCount == null) {
					previousCount = 0;
				}
				if (currentRow - previousCount > 1000) {
					_verboseCounter.put(table, currentRow);
					log = true;
				}
			}
			if (log) {
				addUserLog("Progress for table '" + table.getName() + "': Row no. " + currentRow);
			}
		}
	}
}
