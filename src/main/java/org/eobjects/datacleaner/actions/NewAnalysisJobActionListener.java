package org.eobjects.datacleaner.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.datacleaner.user.UsageLogger;
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
			for (final String datastoreName : datastoreNames) {
				final JMenuItem menuItem = new JMenuItem("Using " + datastoreName);
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
