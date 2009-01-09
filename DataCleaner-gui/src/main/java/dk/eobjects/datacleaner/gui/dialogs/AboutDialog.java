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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;

import dk.eobjects.datacleaner.gui.GuiHelper;

public class AboutDialog extends BanneredDialog {

	private static final long serialVersionUID = -5222815622872491531L;

	public AboutDialog() {
		super();
		setSize(600, 500);
	}

	@Override
	protected Component getContent() {
		JPanel panel = GuiHelper.createPanel().applyBorderLayout()
				.applyDarkBlueBackground().toComponent();

		final JTextArea textArea = GuiHelper.createLabelTextArea()
				.toComponent();
		textArea.setText(GuiHelper.getCreditsText());
		JScrollPane scrollPane = new JScrollPane(textArea,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBorder(GuiHelper.BORDER_WIDE);
		panel.add(scrollPane, BorderLayout.CENTER);
		JToolBar toolbar = GuiHelper.createToolBar();
		JButton creditsButton = new JButton("Credits", GuiHelper
				.getImageIcon("images/toolbar_credits.png"));
		creditsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textArea.setText(GuiHelper.getCreditsText());
			}
		});
		toolbar.add(creditsButton);

		JButton licenceButton = new JButton("Licence", GuiHelper
				.getImageIcon("images/toolbar_licence.png"));
		licenceButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textArea.setText(GuiHelper.getLicenceText());
			}
		});
		toolbar.add(licenceButton);

		JButton changelogButton = new JButton("Changelog", GuiHelper
				.getImageIcon("images/toolbar_licence.png"));
		changelogButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textArea.setText(GuiHelper.getChangelogText());
			}
		});
		toolbar.add(changelogButton);

		panel.add(toolbar, BorderLayout.SOUTH);
		return panel;
	}

	@Override
	protected String getDialogTitle() {
		return "About DataCleaner";
	}

}