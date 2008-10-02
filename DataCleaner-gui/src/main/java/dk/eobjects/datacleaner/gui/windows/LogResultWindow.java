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

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.jdesktop.swingx.JXStatusBar;

import dk.eobjects.datacleaner.gui.GuiBuilder;
import dk.eobjects.datacleaner.gui.GuiHelper;
import dk.eobjects.thirdparty.tabs.CloseableTabbedPane;

public abstract class LogResultWindow extends AbstractWindow {

	private JTextArea _logTextArea;
	private CloseableTabbedPane _tabbedPane;
	private JProgressBar _progressBar;
	private JLabel _label;
	private String _title;

	@Override
	public void disposeInternal() {
		super.disposeInternal();
		_logTextArea = null;
		_tabbedPane = null;
		_progressBar = null;
		_label = null;
		_title = null;
	}

	public LogResultWindow(String title) {
		_title = title;
		new GuiBuilder<JPanel>(_panel).applyBorderLayout()
				.applyDarkBlueBackground();
		_tabbedPane = new CloseableTabbedPane();
		_logTextArea = GuiHelper.createLabelTextArea().toComponent();
		JScrollPane scrollPane = new JScrollPane(_logTextArea);

		addTab("Log", GuiHelper.getImageIcon("images/tab_log.png"), scrollPane);

		_panel.add(_tabbedPane, BorderLayout.CENTER);

		JXStatusBar statusBar = new GuiBuilder<JXStatusBar>(new JXStatusBar())
				.applyLightBackground().toComponent();
		JXStatusBar.Constraint c1 = new JXStatusBar.Constraint(
				JXStatusBar.Constraint.ResizeBehavior.FILL);
		_label = new JLabel("");
		statusBar.add(_label, c1);

		_progressBar = new JProgressBar(0, 100);
		statusBar.add(_progressBar, new JXStatusBar.Constraint(300));
		_panel.add(statusBar, BorderLayout.SOUTH);
	}

	protected void addTab(String title, ImageIcon icon, JComponent component) {
		int tabCount = _tabbedPane.getTabCount();
		_tabbedPane.setUnclosableTab(tabCount);
		_tabbedPane.addTab(title, icon, component);
		if (tabCount == 1) {
			_tabbedPane.setSelectedIndex(1);
		}
	}

	public void addLogMessage(String message) {
		_logTextArea.append(message + '\n');
	}

	public JProgressBar getProgressBar() {
		return _progressBar;
	}

	public void setStatusBarMessage(String message) {
		_label.setText(message);
	}

	@Override
	public String getTitle() {
		return _title;
	}
}