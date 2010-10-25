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
import org.eobjects.datacleaner.user.DatastoreListener;
import org.eobjects.datacleaner.user.MutableDatastoreCatalog;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.builder.WidgetFactory;
import org.eobjects.datacleaner.windows.AnalysisJobBuilderWindow;
import org.eobjects.datacleaner.windows.OpenCsvFileDialog;

public final class DatastoresListPanel extends DCPanel implements DatastoreListener {

	private static final long serialVersionUID = 1L;

	private final AnalyzerBeansConfiguration _configuration;
	private final MutableDatastoreCatalog _catalog;
	private final DCPanel _datastoresPanel;

	public DatastoresListPanel(AnalyzerBeansConfiguration configuration) {
		super();
		_configuration = configuration;
		_catalog = (MutableDatastoreCatalog) _configuration.getDatastoreCatalog();
		_catalog.addListener(this);
		_datastoresPanel = new DCPanel();

		ImageManager imageManager = ImageManager.getInstance();

		JToolBar toolBar = WidgetFactory.createToolBar();
		JButton addDatastoreMenuItem = new JButton(imageManager.getImageIcon("images/actions/create_datastore.png"));
		addDatastoreMenuItem.setToolTipText("Add datastore");
		addDatastoreMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new OpenCsvFileDialog(_configuration).setVisible(true);
			}
		});
		toolBar.add(addDatastoreMenuItem);

		JButton configureDriversItem = new JButton(imageManager.getImageIcon("images/menu/options.png"));
		configureDriversItem.setToolTipText("Configure database drivers");
		toolBar.add(configureDriversItem);

		setLayout(new BorderLayout());
		add(toolBar, BorderLayout.NORTH);
		add(_datastoresPanel, BorderLayout.CENTER);

		updateComponents();

	}

	private void updateComponents() {
		_datastoresPanel.removeAll();

		DatastoreCatalog datastoreCatalog = _configuration.getDatastoreCatalog();
		String[] datastoreNames = datastoreCatalog.getDatastoreNames();

		for (int i = 0; i < datastoreNames.length; i++) {
			String name = datastoreNames[i];
			final Datastore datastore = datastoreCatalog.getDatastore(name);

			Icon icon = IconUtils.getDatastoreIcon(datastore, IconUtils.ICON_SIZE_SMALL);
			JLabel dsIcon = new JLabel(icon);

			if (name.length() > 15) {
				name = name.substring(0, 12) + "...";
			}
			JLabel dsLabel = new JLabel(name);

			JButton editButton = WidgetFactory.createSmallButton("images/actions/edit.png").toComponent();
			editButton.setToolTipText("Edit datastore");

			JButton jobButton = WidgetFactory.createSmallButton("images/actions/create_job.png").toComponent();
			jobButton.setToolTipText("Create job");
			jobButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					AnalysisJobBuilderWindow window = new AnalysisJobBuilderWindow(_configuration, datastore);
					window.setVisible(true);
				}
			});

			WidgetUtils.addToGridBag(dsIcon, _datastoresPanel, 0, i);
			WidgetUtils.addToGridBag(dsLabel, _datastoresPanel, 1, i);
			WidgetUtils.addToGridBag(editButton, _datastoresPanel, 2, i);
			WidgetUtils.addToGridBag(jobButton, _datastoresPanel, 3, i);
		}

		_datastoresPanel.updateUI();
	}

	@Override
	public void removeNotify() {
		super.removeNotify();
		_catalog.removeListener(this);
	}

	@Override
	public void onAdd(Datastore datastore) {
		updateComponents();
	}

	@Override
	public void onRemove(Datastore datastore) {
		updateComponents();
	}
}
