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
import dk.eobjects.datacleaner.gui.windows.ProfilerWindow;
import dk.eobjects.datacleaner.profiler.IProfileDescriptor;
import dk.eobjects.datacleaner.profiler.ProfileConfiguration;
import dk.eobjects.datacleaner.profiler.ProfilerManager;

public class AddProfileButton extends JButton implements ActionListener {

	private static final long serialVersionUID = -3189155332753633377L;
	protected final Log _log = LogFactory.getLog(getClass());
	private ProfilerWindow _window;

	public AddProfileButton(ProfilerWindow profilerWindow) {
		super("Add profile", GuiHelper.getImageIcon("images/toolbar_add.png"));
		_window = profilerWindow;
		addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		final JPopupMenu popup = new JPopupMenu("Profiles");

		IProfileDescriptor[] profileDescriptors = ProfilerManager
				.getProfileDescriptors();
		for (IProfileDescriptor profileDescriptor : profileDescriptors) {
			JMenuItem item = createProfileItem(profileDescriptor);
			popup.add(item);
		}

		popup.show(this, 0, this.getHeight());
	}

	private JMenuItem createProfileItem(final IProfileDescriptor descriptor) {
		final Icon icon = GuiHelper.getImageIcon(descriptor.getIconPath());
		final String displayName = descriptor.getDisplayName();
		JMenuItem item = new JMenuItem(displayName, icon);
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ProfileConfiguration configuration = new ProfileConfiguration(
						descriptor);
				_window.addTab(configuration);
			}
		});
		return item;
	}
}