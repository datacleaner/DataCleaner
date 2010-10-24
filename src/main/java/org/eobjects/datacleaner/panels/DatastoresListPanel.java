package org.eobjects.datacleaner.panels;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JToolBar;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.builder.WidgetFactory;
import org.eobjects.datacleaner.windows.AnalysisJobBuilderWindow;

public final class DatastoresListPanel extends DCPanel {

	private static final long serialVersionUID = 1L;

	private final AnalyzerBeansConfiguration _configuration;

	public DatastoresListPanel(AnalyzerBeansConfiguration configuration) {
		super();
		_configuration = configuration;
		setLayout(new BorderLayout());

		ImageManager imageManager = ImageManager.getInstance();

		JToolBar toolBar = WidgetFactory.createToolBar();
		JButton addDatastoreMenuItem = new JButton(imageManager.getImageIcon("images/actions/create_datastore.png"));
		addDatastoreMenuItem.setToolTipText("Add datastore");
		toolBar.add(addDatastoreMenuItem);

		JButton configureDriversItem = new JButton(imageManager.getImageIcon("images/menu/options.png"));
		configureDriversItem.setToolTipText("Configure database drivers");
		toolBar.add(configureDriversItem);

		add(toolBar, BorderLayout.NORTH);

		DCPanel datastoresPanel = new DCPanel();

		DatastoreCatalog datastoreCatalog = _configuration.getDatastoreCatalog();
		String[] datastoreNames = datastoreCatalog.getDatastoreNames();

		for (int i = 0; i < datastoreNames.length; i++) {
			String name = datastoreNames[i];
			final Datastore datastore = datastoreCatalog.getDatastore(name);

			Icon icon = IconUtils.getDatastoreIcon(datastore, IconUtils.ICON_SIZE_SMALL);

			JLabel dsIcon = WidgetFactory.createLabel(icon).toComponent();
			JLabel dsLabel = WidgetFactory.createLabel(name).toComponent();

			WidgetUtils.addToGridBag(dsIcon, datastoresPanel, 0, i);
			WidgetUtils.addToGridBag(dsLabel, datastoresPanel, 1, i);

			JButton editButton = WidgetFactory.createSmallButton("images/actions/edit.png").toComponent();
			editButton.setToolTipText("Edit datastore");
			WidgetUtils.addToGridBag(editButton, datastoresPanel, 2, i);

			JButton jobButton = WidgetFactory.createSmallButton("images/actions/create_job.png").toComponent();
			jobButton.setToolTipText("Create job");
			jobButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					AnalysisJobBuilderWindow window = new AnalysisJobBuilderWindow(_configuration, datastore);
					window.setVisible(true);
				}
			});
			WidgetUtils.addToGridBag(jobButton, datastoresPanel, 3, i);
		}

		add(datastoresPanel, BorderLayout.CENTER);
	}
}
