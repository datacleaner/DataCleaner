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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.IdentityHashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import org.eobjects.datacleaner.util.WidgetUtils;
import org.jdesktop.swingx.VerticalLayout;

import org.eobjects.metamodel.schema.Table;

public class ProgressInformationPanel extends DCPanel {

	private static final long serialVersionUID = 1L;

	private final JTextArea _textArea = new JTextArea();
	private final DCPanel _progressBarPanel;
	private final Map<Table, JProgressBar> _progressBars = new IdentityHashMap<Table, JProgressBar>();
	private final JScrollPane _textAreaScroll;

	public ProgressInformationPanel() {
		super();
		setLayout(new BorderLayout());
		_textArea.setText("--- DataCleaner progress information user-log ---");
		_textAreaScroll = WidgetUtils.scrolleable(_textArea);
		_textAreaScroll.setBorder(WidgetUtils.BORDER_THIN);

		_progressBarPanel = new DCPanel(WidgetUtils.BG_COLOR_DARK, WidgetUtils.BG_COLOR_DARK);
		_progressBarPanel.setLayout(new VerticalLayout(4));
		_progressBarPanel.setBorder(WidgetUtils.BORDER_EMPTY);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setDividerLocation(190);
		splitPane.setBorder(null);
		splitPane.add(WidgetUtils.scrolleable(_progressBarPanel));
		splitPane.add(_textAreaScroll);

		add(splitPane, BorderLayout.CENTER);
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

	private void appendMessage(String message) {
		_textArea.append(message);

		// moves the vertical scroll to the bottom
		JScrollBar verticalScrollBar = _textAreaScroll.getVerticalScrollBar();
		verticalScrollBar.setValue(verticalScrollBar.getMaximum());
	}

	public void setExpectedRows(Table table, int expectedRows) {
		boolean firstTable = _progressBars.isEmpty();

		JProgressBar progressBar = new JProgressBar(0, expectedRows);
		_progressBars.put(table, progressBar);

		JLabel tableLabel = new JLabel(table.getName());
		tableLabel.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
		
		JLabel rowsLabel = new JLabel(expectedRows + " rows");
		rowsLabel.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
		rowsLabel.setFont(WidgetUtils.FONT_SMALL);

		if (!firstTable) {
			_progressBarPanel.add(Box.createVerticalStrut(10));
		}
		
		_progressBarPanel.add(tableLabel);
		_progressBarPanel.add(rowsLabel);
		_progressBarPanel.add(progressBar);
		updateUI();
	}

	public void updateProgress(Table table, int currentRow) {
		JProgressBar progressBar = _progressBars.get(table);
		if (progressBar.getValue() < currentRow) {
			progressBar.setValue(currentRow);
		}
	}
}
