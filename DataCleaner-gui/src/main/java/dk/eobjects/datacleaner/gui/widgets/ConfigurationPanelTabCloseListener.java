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

import java.awt.Component;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dk.eobjects.datacleaner.gui.panels.IConfigurationPanel;
import dk.eobjects.thirdparty.tabs.CloseableTabbedPane;
import dk.eobjects.thirdparty.tabs.TabCloseEvent;
import dk.eobjects.thirdparty.tabs.TabCloseListener;

public class ConfigurationPanelTabCloseListener implements TabCloseListener {

	private final static Log _log = LogFactory
			.getLog(ConfigurationPanelTabCloseListener.class);
	private CloseableTabbedPane _tabbedPane;
	private Map<JPanel, IConfigurationPanel> _configurationPanels;

	public ConfigurationPanelTabCloseListener(CloseableTabbedPane tabbedPane,
			Map<JPanel, IConfigurationPanel> configurationPanels) {
		_tabbedPane = tabbedPane;
		_configurationPanels = configurationPanels;
	}

	public void tabClosed(TabCloseEvent event) {
		int tabIndex = event.getClosedTab();
		Component component = _tabbedPane.getComponent(tabIndex);
		if (component instanceof JScrollPane) {
			component = ((JScrollPane) component).getViewport().getComponent(0);
		}
		_tabbedPane.remove(tabIndex);

		IConfigurationPanel configurationPanel = _configurationPanels
				.get(component);
		if (configurationPanel != null) {
			try {
				if (_log.isInfoEnabled()) {
					_log.info("Destroying configuration: "
							+ configurationPanel.getConfiguration());
				}
				configurationPanel.destroy();
			} catch (Exception e) {
				_log
						.info("Exception thrown while destroying configuration panel: "
								+ e.getMessage());
				_log.debug(e);
			}
			_configurationPanels.remove(component);
		}
	}

}