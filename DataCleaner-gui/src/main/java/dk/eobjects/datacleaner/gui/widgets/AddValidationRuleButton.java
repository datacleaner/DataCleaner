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
package dk.eobjects.datacleaner.gui.widgets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dk.eobjects.datacleaner.gui.GuiHelper;
import dk.eobjects.datacleaner.gui.windows.ValidatorWindow;
import dk.eobjects.datacleaner.validator.IValidationRuleDescriptor;
import dk.eobjects.datacleaner.validator.ValidationRuleConfiguration;
import dk.eobjects.datacleaner.validator.ValidatorManager;

public class AddValidationRuleButton extends JButton implements ActionListener {

	private static final long serialVersionUID = 451961366258616382L;
	protected final Log _log = LogFactory.getLog(getClass());
	private ValidatorWindow _window;

	public AddValidationRuleButton(ValidatorWindow validatorWindow) {
		super("Add validation rule", GuiHelper
				.getImageIcon("images/toolbar_add.png"));
		_window = validatorWindow;
		addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		final JPopupMenu popup = new JPopupMenu("Validation rules");

		IValidationRuleDescriptor[] validationRuleDescriptors = ValidatorManager
				.getValidationRuleDescriptors();
		for (IValidationRuleDescriptor descriptor : validationRuleDescriptors) {
			popup.add(createValidationRuleItem(descriptor));
		}
		popup.show(this, 0, this.getHeight());
	}

	private JMenuItem createValidationRuleItem(
			final IValidationRuleDescriptor descriptor) {
		final Icon icon = GuiHelper.getImageIcon(descriptor.getIconPath());
		final String displayName = descriptor.getDisplayName();
		JMenuItem item = new JMenuItem(displayName, icon);
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ValidationRuleConfiguration configuration = new ValidationRuleConfiguration(
						descriptor);
				_window.addTab(configuration);
			}
		});
		return item;
	}
}