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
package dk.eobjects.datacleaner.gui.dialogs;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import dk.eobjects.datacleaner.gui.GuiHelper;
import dk.eobjects.datacleaner.gui.widgets.AddWindowButton;
import dk.eobjects.datacleaner.gui.widgets.OpenFileActionListener;
import dk.eobjects.datacleaner.gui.windows.ComparatorWindow;
import dk.eobjects.datacleaner.gui.windows.ProfilerWindow;
import dk.eobjects.datacleaner.gui.windows.ValidatorWindow;

public class NewTaskDialog extends BanneredDialog {

	private static final long serialVersionUID = -8480059695682450837L;
	private JPanel _panel;

	public NewTaskDialog() {
		super(400, 510);
	}

	@Override
	protected Component getContent() {
		_panel = GuiHelper.createPanel().applyLayout(null).toComponent();
		_panel.setBorder(GuiHelper.BORDER_WIDE);

		JTextArea profileDesc = createDescLabel("Gain insight into the structure and content of your data. With a data profile you will know where to improve quality.");
		JTextArea validateDesc = createDescLabel("Create validation rules and test them on your data to ensure the continous integrity of your data's quality.");
		JTextArea compareDesc = createDescLabel("Compare columns, tables and schemas with each others! Find out if what you think is consistent really is! Please wait for DataCleaner 1.1 for this feature.");
		JTextArea openFileDesc = createDescLabel("... or you can load your profiling or validation work from a saved file.");

		addToPanel(
				new JLabel("Welcome to DataCleaner. What do you want to do?"),
				10, 5, 400, 20);

		addToPanel(createHeaderLabel("Profile"), 90, 30, 290, 20);
		addToPanel(profileDesc, 90, 50, 290, 70);
		addToPanel(createHeaderLabel("Validate"), 90, 130, 290, 20);
		addToPanel(validateDesc, 90, 150, 290, 70);
		addToPanel(createHeaderLabel("Compare"), 90, 230, 290, 20);
		addToPanel(compareDesc, 90, 250, 290, 70);
		addToPanel(openFileDesc, 45, 326, 250, 32);

		AddWindowButton profilerButton = new AddWindowButton(
				ProfilerWindow.class, GuiHelper
						.getImageIcon("images/task_profile.png"), this,
				"Profile");
		addToPanel(profilerButton, 10, 30, 64, 64);

		AddWindowButton validatorButton = new AddWindowButton(
				ValidatorWindow.class, GuiHelper
						.getImageIcon("images/task_validate.png"), this,
				"Validate");
		addToPanel(validatorButton, 10, 130, 64, 64);

		AddWindowButton comparatorButton = new AddWindowButton(
				ComparatorWindow.class, GuiHelper
						.getImageIcon("images/task_compare.png"), this,
				"Compare");
		addToPanel(comparatorButton, 10, 230, 64, 64);

		JButton openFileButton = GuiHelper.createButton(null,
				"images/toolbar_open.png").toComponent();
		openFileButton.addActionListener(new OpenFileActionListener());
		openFileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				NewTaskDialog.this.dispose();
			}
		});
		openFileButton.setToolTipText("Open file");
		addToPanel(openFileButton, 10, 330, 26, 26);

		return _panel;
	}

	@Override
	protected String getDialogTitle() {
		return "Select a DataCleaner task";
	}

	private JTextArea createDescLabel(String text) {
		JTextArea textArea = new JTextArea(text);
		Dimension dimension = new Dimension(320, 44);
		textArea.setPreferredSize(dimension);
		textArea.setSize(dimension);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setEditable(false);
		textArea.setBackground(_panel.getBackground());
		textArea.setFont(GuiHelper.FONT_NORMAL);
		return textArea;
	}

	private JLabel createHeaderLabel(String text) {
		JLabel label = new JLabel(text);
		Dimension dimension = new Dimension(320, 20);
		label.setPreferredSize(dimension);
		label.setSize(dimension);
		label.setFont(GuiHelper.FONT_HEADER);
		return label;
	}

	private void addToPanel(Component comp, int x, int y, int width, int height) {
		comp.setSize(width, height);
		comp.setLocation(x, y);
		_panel.add(comp);
	}
}