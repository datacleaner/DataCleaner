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
package org.eobjects.datacleaner.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.datacleaner.user.UsageLogger;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.windows.AnalysisJobBuilderWindow;

public final class NewAnalysisJobActionListener implements ActionListener {

	private final AnalyzerBeansConfiguration _configuration;

	public NewAnalysisJobActionListener(AnalyzerBeansConfiguration configuration) {
		_configuration = configuration;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final Component comp = (Component) e.getSource();

		final JPopupMenu popupMenu = new JPopupMenu();
		final String[] datastoreNames = _configuration.getDatastoreCatalog().getDatastoreNames();

		if (datastoreNames == null || datastoreNames.length == 0) {
			JOptionPane.showMessageDialog(comp, "Please create a new datastore before you create a job",
					"No datastore available", JOptionPane.ERROR_MESSAGE);
		} else {
			Icon icon = ImageManager.getInstance().getImageIcon("images/filetypes/analysis_job.png",
					IconUtils.ICON_SIZE_SMALL);
			for (final String datastoreName : datastoreNames) {
				final JMenuItem menuItem = WidgetFactory.createMenuItem("Using " + datastoreName, icon);
				menuItem.setToolTipText("New analysis job using " + datastoreName);
				menuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						UsageLogger.getInstance().log("New analysis job");
						new AnalysisJobBuilderWindow(_configuration, datastoreName).setVisible(true);
					}
				});
				popupMenu.add(menuItem);
			}
			popupMenu.show(comp, 0, comp.getHeight());
		}
	}

}
