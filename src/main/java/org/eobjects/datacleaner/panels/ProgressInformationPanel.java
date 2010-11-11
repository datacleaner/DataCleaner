package org.eobjects.datacleaner.panels;

import java.awt.BorderLayout;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.eobjects.datacleaner.util.WidgetUtils;

public class ProgressInformationPanel extends DCPanel {

	private static final long serialVersionUID = 1L;

	private final JTextArea _textArea = new JTextArea();
	private final JScrollPane _textAreaScroll;

	public ProgressInformationPanel() {
		super(WidgetUtils.BG_COLOR_BRIGHT, WidgetUtils.BG_COLOR_BRIGHTEST);
		setLayout(new BorderLayout());
		_textArea.setText("--- DataCleaner progress information user-log ---");
		_textAreaScroll = WidgetUtils.scrolleable(_textArea);
		add(_textAreaScroll, BorderLayout.CENTER);
	}

	public void addUserLog(String string) {
		_textArea.append("\nINFO: " + string);
	}

	public void addUserLog(String string, Throwable throwable) {
		StringWriter stringWriter = new StringWriter();
		stringWriter.append("\nERROR: ");
		stringWriter.append(string);
		stringWriter.append('\n');
		throwable.printStackTrace(new PrintWriter(stringWriter));
		_textArea.append(stringWriter.toString());
//		JScrollBar verticalScrollBar = _textAreaScroll.getVerticalScrollBar();
//		verticalScrollBar.setValue(verticalScrollBar.getMaximum());
	}
}
