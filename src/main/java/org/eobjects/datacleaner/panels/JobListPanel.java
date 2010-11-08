package org.eobjects.datacleaner.panels;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.widgets.builder.WidgetFactory;
import org.eobjects.datacleaner.windows.AnalysisJobBuilderWindow;

public class JobListPanel extends DCPanel {

	private static final long serialVersionUID = 1L;
	private final AnalyzerBeansConfiguration _configuration;

	public JobListPanel(AnalyzerBeansConfiguration configuration) {
		super();
		_configuration = configuration;

		setLayout(new BorderLayout());

		JToolBar toolBar = WidgetFactory.createToolBar();
		final JButton addAnalysisJobItem = new JButton("Add job", ImageManager.getInstance().getImageIcon(
				"images/actions/create_job.png"));

		addAnalysisJobItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JPopupMenu popupMenu = new JPopupMenu();
				String[] datastoreNames = _configuration.getDatastoreCatalog().getDatastoreNames();
				for (final String datastoreName : datastoreNames) {
					JMenuItem menuItem = new JMenuItem("Using " + datastoreName);
					menuItem.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							new AnalysisJobBuilderWindow(_configuration, datastoreName).setVisible(true);
						}
					});
					popupMenu.add(menuItem);
				}
				popupMenu.show(addAnalysisJobItem, 0, addAnalysisJobItem.getHeight());
			}
		});

		addAnalysisJobItem.setToolTipText("Add analysis job");
		toolBar.add(addAnalysisJobItem);

		add(toolBar, BorderLayout.NORTH);
	}
}
