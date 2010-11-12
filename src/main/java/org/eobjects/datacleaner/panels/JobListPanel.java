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
import org.eobjects.datacleaner.windows.OpenAnalysisJobActionListener;

public class JobListPanel extends DCPanel {

	private static final long serialVersionUID = 1L;
	private final AnalyzerBeansConfiguration _configuration;

	public JobListPanel(AnalyzerBeansConfiguration configuration) {
		super();
		_configuration = configuration;

		setLayout(new BorderLayout());

		JToolBar toolBar = WidgetFactory.createToolBar();
		final JButton addJobButton = new JButton("New", ImageManager.getInstance()
				.getImageIcon("images/actions/new.png"));
		addJobButton.setToolTipText("Add analysis job");
		addJobButton.addActionListener(new ActionListener() {
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
				popupMenu.show(addJobButton, 0, addJobButton.getHeight());
			}
		});

		JButton openJobButton = new JButton("Open", ImageManager.getInstance().getImageIcon("images/actions/open.png"));
		openJobButton.setToolTipText("Open analysis job");
		openJobButton.addActionListener(new OpenAnalysisJobActionListener(_configuration));

		toolBar.add(addJobButton);
		toolBar.add(openJobButton);

		add(toolBar, BorderLayout.NORTH);
	}
}
