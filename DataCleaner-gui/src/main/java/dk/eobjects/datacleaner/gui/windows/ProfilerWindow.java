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
package dk.eobjects.datacleaner.gui.windows;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import dk.eobjects.datacleaner.data.ColumnSelection;
import dk.eobjects.datacleaner.data.DataContextSelection;
import dk.eobjects.datacleaner.execution.ExecutionConfiguration;
import dk.eobjects.datacleaner.gui.DataCleanerGui;
import dk.eobjects.datacleaner.gui.GuiHelper;
import dk.eobjects.datacleaner.gui.dialogs.ExecutionConfigurationDialog;
import dk.eobjects.datacleaner.gui.model.ExtensionFilter;
import dk.eobjects.datacleaner.gui.panels.ColumnSelectionPanel;
import dk.eobjects.datacleaner.gui.panels.ConfigurationPanelManager;
import dk.eobjects.datacleaner.gui.panels.IConfigurationPanel;
import dk.eobjects.datacleaner.gui.panels.MetadataPanel;
import dk.eobjects.datacleaner.gui.setup.GuiConfiguration;
import dk.eobjects.datacleaner.gui.widgets.AddProfileButton;
import dk.eobjects.datacleaner.gui.widgets.ConfigurationPanelTabCloseListener;
import dk.eobjects.datacleaner.gui.widgets.OpenDatabaseButton;
import dk.eobjects.datacleaner.gui.widgets.OpenFileButton;
import dk.eobjects.datacleaner.gui.widgets.RunProfilerButton;
import dk.eobjects.datacleaner.gui.widgets.SchemaTree;
import dk.eobjects.datacleaner.gui.widgets.SchemaTreeMouseListener;
import dk.eobjects.datacleaner.profiler.IProfileDescriptor;
import dk.eobjects.datacleaner.profiler.ProfilerJobConfiguration;
import dk.eobjects.datacleaner.util.DomHelper;
import dk.eobjects.datacleaner.util.WeakObservable;
import dk.eobjects.datacleaner.util.WeakObserver;
import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.thirdparty.tabs.CloseableTabbedPane;

public class ProfilerWindow extends AbstractWindow implements WeakObserver {

	public static final String NODE_NAME = "profiler";

	public static final ExtensionFilter EXTENSION_FILTER = new ExtensionFilter(
			"DataCleaner profiler configuration (.dcp)", "dcp");

	private DataContextSelection _dataContextSelection;
	private ColumnSelection _columnSelection;
	private CloseableTabbedPane _tabbedPane;
	private ExecutionConfiguration _executionConfiguration;
	private Map<JPanel, IConfigurationPanel> _configurationPanels = new HashMap<JPanel, IConfigurationPanel>();

	private JButton _optionsButton;

	@Override
	public void disposeInternal() {
		_tabbedPane.removeAll();

		if (_dataContextSelection != null) {
			_dataContextSelection.deleteObserver(_columnSelection);
			_dataContextSelection.deleteObserver(this);
			_dataContextSelection.selectNothing();
		}

		_dataContextSelection = null;
		_columnSelection = null;
		_configurationPanels = null;
	}

	public ProfilerWindow() {
		this(new DataContextSelection(), new ExecutionConfiguration());
	}

	public ProfilerWindow(DataContextSelection dataContextSelection,
			ExecutionConfiguration executionConfiguration) {
		super();
		_dataContextSelection = dataContextSelection;
		_dataContextSelection.addObserver(this);
		_executionConfiguration = executionConfiguration;
		_columnSelection = new ColumnSelection(_dataContextSelection);
		_tabbedPane = new CloseableTabbedPane();
		_tabbedPane.addTabCloseListener(new ConfigurationPanelTabCloseListener(
				_tabbedPane, _configurationPanels));
		_panel.setLayout(new BorderLayout());

		JButton saveProfilerButton = new JButton("Save", GuiHelper
				.getImageIcon("images/toolbar_save.png"));
		saveProfilerButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (_dataContextSelection.getDataContext() == null) {
					GuiHelper
							.showErrorMessage(
									"Nothing to save",
									"You haven't opened a datastore yet, so there's nothing to save.",
									null);
				} else {
					JFileChooser f = new JFileChooser();
					f.setSelectedFile(new File("my_profiler.dcp"));
					f.addChoosableFileFilter(EXTENSION_FILTER);
					if (f.showSaveDialog(_panel) == JFileChooser.APPROVE_OPTION) {
						File file = f.getSelectedFile();
						boolean saveFile = true;
						if (file.exists()) {
							if (JOptionPane.showConfirmDialog(_panel,
									"A file with the filename '"
											+ file.getName()
											+ "' already exists. Overwrite?",
									"Overwrite?", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
								saveFile = false;
							}
						}
						if (saveFile) {
							DocumentBuilder documentBuilder = DomHelper
									.getDocumentBuilder();
							Document document = documentBuilder.newDocument();
							DomHelper.transform(serialize(document),
									new StreamResult(file));
						}
					}
				}
			}
		});

		JToolBar toolbar = GuiHelper.createToolBar();
		toolbar.add(new OpenDatabaseButton(_dataContextSelection));
		toolbar.add(new OpenFileButton(_dataContextSelection));
		toolbar.add(new JSeparator(JSeparator.VERTICAL));
		toolbar.add(saveProfilerButton);
		_optionsButton = GuiHelper.createButton("Profiler options",
				"images/toolbar_configure.png").toComponent();
		_optionsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new ExecutionConfigurationDialog(_executionConfiguration, true)
						.setVisible(true);
			}
		});
		updateOptionsButton();
		toolbar.add(_optionsButton);
		toolbar.add(new AddProfileButton(this));
		toolbar.add(new RunProfilerButton(_dataContextSelection,
				_configurationPanels, _executionConfiguration));

		_panel.add(toolbar, BorderLayout.NORTH);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

		SchemaTree schemaTree = new SchemaTree(_dataContextSelection);
		schemaTree.addMouseListener(new SchemaTreeMouseListener(schemaTree,
				_dataContextSelection, _columnSelection));
		JScrollPane scrollSchemaTree = new JScrollPane(schemaTree);
		splitPane.add(scrollSchemaTree);

		ColumnSelectionPanel columnSelectionPanel = new ColumnSelectionPanel(
				_dataContextSelection, _columnSelection);
		MetadataPanel metadataPanel = new MetadataPanel(_columnSelection);
		_tabbedPane.addTab("Data selection", GuiHelper
				.getImageIcon("images/tab_data_selection.png"),
				columnSelectionPanel);
		_tabbedPane.addTab("Metadata", GuiHelper
				.getImageIcon("images/tab_metadata.png"), new JScrollPane(
				metadataPanel));
		_tabbedPane.setUnclosableTab(0);
		_tabbedPane.setUnclosableTab(1);

		splitPane.add(_tabbedPane);
		splitPane.setBackground(GuiHelper.BG_COLOR_DARKBLUE);

		_panel.add(splitPane, BorderLayout.CENTER);

		GuiHelper.silentNotification("profiler-window");
	}

	@Override
	public ImageIcon getFrameIcon() {
		return GuiHelper.getImageIcon("images/window_profile.png");
	}

	@Override
	public String getTitle() {
		return "Profiler";
	}

	public void addTab(ProfilerJobConfiguration configuration) {
		ConfigurationPanelManager configurationPanelManager = GuiConfiguration
				.getConfigurationPanelManager();
		IProfileDescriptor descriptor = configuration.getProfileDescriptor();
		IConfigurationPanel configurationPanel = configurationPanelManager
				.getPanelForProfile(descriptor.getProfileClass());
		configurationPanel.initialize(_tabbedPane, descriptor,
				_columnSelection, configuration);
		JPanel panel = configurationPanel.getPanel();
		panel.setBackground(GuiHelper.BG_COLOR_LIGHT);
		_configurationPanels.put(panel, configurationPanel);
		ImageIcon icon = GuiHelper.getImageIcon(descriptor.getIconPath());
		JScrollPane scrollPane = new JScrollPane(panel);
		scrollPane.setBorder(null);
		_tabbedPane.addTab(descriptor.getDisplayName(), icon, scrollPane);
	}

	public Node serialize(Document document) {
		Element profilerNode = document.createElement(NODE_NAME);
		profilerNode.setAttribute("version", DataCleanerGui.VERSION);
		profilerNode.appendChild(_dataContextSelection.serialize(document));
		profilerNode.appendChild(_executionConfiguration.serialize(document));
		for (IConfigurationPanel configurationPanel : _configurationPanels
				.values()) {
			ProfilerJobConfiguration configuration = (ProfilerJobConfiguration) configurationPanel
					.getJobConfiguration();
			profilerNode.appendChild(configuration.serialize(document));
		}
		return profilerNode;
	}

	public static ProfilerWindow deserialize(Node node) throws SQLException {
		Node dataContextSelectionNode = DomHelper.getChildNodesByName(node,
				DataContextSelection.NODE_NAME).get(0);
		DataContextSelection dataContextSelection = DataContextSelection
				.deserialize(dataContextSelectionNode);

		Node executionConfigurationNode = DomHelper.getChildNodesByName(node,
				ExecutionConfiguration.NODE_NAME).get(0);
		ExecutionConfiguration executionConfiguration;
		if (executionConfigurationNode == null) {
			executionConfiguration = new ExecutionConfiguration();
		} else {
			executionConfiguration = ExecutionConfiguration
					.deserialize(executionConfigurationNode);
		}

		ProfilerWindow window = new ProfilerWindow(dataContextSelection,
				executionConfiguration);

		DataContext dc = dataContextSelection.getDataContext();
		List<Node> configurationNodes = DomHelper.getChildNodesByName(node,
				ProfilerJobConfiguration.NODE_NAME);
		Set<Column> columns = new HashSet<Column>();
		List<ProfilerJobConfiguration> configurations = new ArrayList<ProfilerJobConfiguration>();
		List<IllegalArgumentException> configurationExceptions = new ArrayList<IllegalArgumentException>();
		for (Node configurationNode : configurationNodes) {
			try {
				ProfilerJobConfiguration configuration = ProfilerJobConfiguration
						.deserialize(configurationNode, dc);
				columns.addAll(Arrays.asList(configuration.getColumns()));
				configurations.add(configuration);
			} catch (IllegalArgumentException e) {
				configurationExceptions.add(e);
			}
		}
		for (Column column : columns) {
			window._columnSelection.toggleColumn(column);
		}
		for (ProfilerJobConfiguration configuration : configurations) {
			window.addTab(configuration);
		}
		if (!configurationExceptions.isEmpty()) {
			for (IllegalArgumentException e : configurationExceptions) {
				GuiHelper.showErrorMessage(e.getMessage(),
						"An error occurred while reading profile configuration: "
								+ e.getMessage(), e);
			}
		}
		return window;
	}

	public void update(WeakObservable observable) {
		if (observable instanceof DataContextSelection) {
			updateOptionsButton();
		}
	}

	private void updateOptionsButton() {
		if (_dataContextSelection.isJdbcSource()) {
			_optionsButton.setEnabled(true);
		} else {
			_optionsButton.setEnabled(false);

			// Don't (sub)optimize non SQL based datastores
			_executionConfiguration.setGroupByOptimizationEnabled(false);
			_executionConfiguration.setQuerySplitterSize(null);
		}
	}
}