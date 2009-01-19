package dk.eobjects.datacleaner.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;

import dk.eobjects.datacleaner.execution.ExecutionConfiguration;
import dk.eobjects.datacleaner.gui.GuiHelper;

public class ExecutionConfigurationDialog extends BanneredDialog implements
		ActionListener {

	private static final String NEVER_ITEM = "Never";
	private static final long serialVersionUID = 2687365078384594419L;
	private ExecutionConfiguration _executionConfiguration;
	private JCheckBox _drillToDetailCheckBox;
	private JCheckBox _groupByOptimizationCheckBox;
	private JComboBox _splitQueriesCombobox;
	private JComboBox _maxConnectionsCombobox;
	private JComboBox _maxQueriesPerConnectionCombobox;
	private JPanel _formPanel;

	public ExecutionConfigurationDialog(
			ExecutionConfiguration executionConfiguration,
			boolean includeDrillToDetails) {
		super(430, 680);
		_executionConfiguration = executionConfiguration;

		if (includeDrillToDetails) {
			// Drill to details
			int rowIndex = 7;
			rowIndex++;
			_drillToDetailCheckBox = GuiHelper.createCheckBox(
					"Enable drill-to-detail in profiler results", false)
					.toComponent();
			GuiHelper.addToGridBag(_drillToDetailCheckBox, _formPanel, 0,
					rowIndex, 3, 1);

			rowIndex++;
			JTextArea aboutDrillToDetail = GuiHelper.createLabelTextArea()
					.applyWhiteBackground().applyBorder().toComponent();
			aboutDrillToDetail
					.setText("Drill-to-detail enables you to click the profiling measures in order to investigate "
							+ "the root causes of the results. Providing this functionality takes up some memory "
							+ "to keep metadata about the details and in some cases the detail-content itself.");
			GuiHelper.addToGridBag(aboutDrillToDetail, _formPanel, 1, rowIndex,
					2, 1);

			_drillToDetailCheckBox.setSelected(_executionConfiguration
					.isDrillToDetailEnabled());
		}
		_groupByOptimizationCheckBox.setSelected(_executionConfiguration
				.isGroupByOptimizationEnabled());
		_maxConnectionsCombobox.setSelectedItem(_executionConfiguration
				.getMaxConnections());
		_maxQueriesPerConnectionCombobox
				.setSelectedItem(_executionConfiguration
						.getMaxQueriesPerConnection());

		if (_executionConfiguration.isQuerySplitterEnabled()) {
			int itemCount = _splitQueriesCombobox.getItemCount();
			long querySplitterSize = _executionConfiguration
					.getQuerySplitterSize();
			boolean foundItem = false;
			for (int i = 0; i < itemCount; i++) {
				Object item = _splitQueriesCombobox.getItemAt(i);
				if (item instanceof QuerySplitOption) {
					QuerySplitOption option = (QuerySplitOption) item;
					if (querySplitterSize == option.getNumRowsRequired()) {
						_splitQueriesCombobox.setSelectedIndex(i);
						foundItem = true;
						break;
					}
				}
			}
			if (!foundItem) {
				QuerySplitOption option = new QuerySplitOption(
						querySplitterSize);
				_splitQueriesCombobox.addItem(option);
				_splitQueriesCombobox.setSelectedItem(option);
			}
		}

		GridBagLayout layout = (GridBagLayout) _formPanel.getLayout();
		layout.columnWidths = new int[] { 30, 230, 130 };
	}

	@Override
	protected Component getContent() {
		JPanel panel = GuiHelper.createPanel().applyBorderLayout()
				.toComponent();
		panel.setBorder(GuiHelper.BORDER_WIDE);
		_formPanel = GuiHelper.createPanel().toComponent();

		// Max connections
		int rowIndex = 0;
		GuiHelper.addToGridBag(new JLabel("Max connections:"), _formPanel, 0,
				rowIndex, 2, 1);
		_maxConnectionsCombobox = new JComboBox(new Object[] { 1, 2, 3, 4 });
		GuiHelper.addToGridBag(_maxConnectionsCombobox, _formPanel, 2,
				rowIndex, 1, 1);

		// Max queries per connection
		rowIndex++;
		GuiHelper.addToGridBag(new JLabel(
				"Max simultanious queries per connection:"), _formPanel, 0,
				rowIndex, 2, 1);
		_maxQueriesPerConnectionCombobox = new JComboBox(new Object[] { 1, 2,
				3, 4 });
		GuiHelper.addToGridBag(_maxQueriesPerConnectionCombobox, _formPanel, 2,
				rowIndex, 1, 1);

		rowIndex++;
		JTextArea aboutMultithreading = GuiHelper.createLabelTextArea()
				.applyWhiteBackground().applyBorder().toComponent();
		aboutMultithreading
				.setText("The 'Max connections' and 'Max simultanious queries per connection' options "
						+ "define the multi-threading strategy when executing DataCleaner jobs. The more "
						+ "connections and simultanious queries you enable, the more working threads and "
						+ "thus higher utilization of CPU it will produce. However not all databases "
						+ "support multiple connections and multiple queries per connections, "
						+ "so tune these options to fit your database.");
		GuiHelper.addToGridBag(aboutMultithreading, _formPanel, 1, rowIndex, 2,
				1);

		// Group by optimization
		rowIndex++;
		_groupByOptimizationCheckBox = GuiHelper.createCheckBox(
				"Enable GROUP BY optimization", false).toComponent();
		GuiHelper.addToGridBag(_groupByOptimizationCheckBox, _formPanel, 0,
				rowIndex, 3, 1);
		rowIndex++;
		JTextArea aboutGroupByOptimization = GuiHelper.createLabelTextArea()
				.applyWhiteBackground().applyBorder().toComponent();
		aboutGroupByOptimization
				.setText("For tables with a lot of repeated values DataCleaner can save a lot "
						+ "of computation by adding GROUP BY clauses to the generated queries. "
						+ "For tables with a lot of distinct values this type of suboptimization "
						+ "is however a waste of resources.");
		GuiHelper.addToGridBag(aboutGroupByOptimization, _formPanel, 1,
				rowIndex, 2, 1);

		// Split query optimization
		rowIndex++;
		GuiHelper.addToGridBag(new JLabel("Split queries:"), _formPanel, 0,
				rowIndex, 2, 1);
		_splitQueriesCombobox = new JComboBox(new Object[] { NEVER_ITEM,
				new QuerySplitOption(5000), new QuerySplitOption(100000),
				new QuerySplitOption(200000), new QuerySplitOption(500000),
				new QuerySplitOption(1000000), new QuerySplitOption(2000000),
				new QuerySplitOption(5000000) });
		_splitQueriesCombobox.setRenderer(new DefaultListCellRenderer() {

			private static final long serialVersionUID = 7402962579815020361L;

			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				if (value instanceof QuerySplitOption) {
					QuerySplitOption option = (QuerySplitOption) value;
					return super.getListCellRendererComponent(list, " > "
							+ option.getNumRowsRequired() + " rows", index,
							isSelected, cellHasFocus);
				} else {
					return super.getListCellRendererComponent(list, value,
							index, isSelected, cellHasFocus);
				}
			}

		});
		GuiHelper.addToGridBag(_splitQueriesCombobox, _formPanel, 2, rowIndex,
				1, 1);
		rowIndex++;
		JTextArea aboutSplitQueryOptimization = GuiHelper.createLabelTextArea()
				.applyWhiteBackground().applyBorder().toComponent();
		aboutSplitQueryOptimization
				.setText("Split query optimization is used to generate queries that yield partitioned "
						+ "result sets and thus better leveling of queries on multiple threads. "
						+ "Splitting queries requires preliminary table-querying to establish split-query criteria.");
		GuiHelper.addToGridBag(aboutSplitQueryOptimization, _formPanel, 1,
				rowIndex, 2, 1);

		JToolBar toolbar = GuiHelper.createToolBar();
		JButton saveButton = GuiHelper.createButton("Save options",
				"images/toolbar_save.png").toComponent();
		saveButton.addActionListener(this);
		toolbar.add(saveButton);

		panel.add(new JScrollPane(_formPanel), BorderLayout.CENTER);
		panel.add(toolbar, BorderLayout.SOUTH);

		return panel;
	}

	@Override
	protected String getDialogTitle() {
		return "Profiler options";
	}

	/**
	 * The save button have been pressed
	 */
	public void actionPerformed(ActionEvent e) {
		if (_drillToDetailCheckBox != null) {
			_executionConfiguration
					.setDrillToDetailEnabled(_drillToDetailCheckBox
							.isSelected());
		}
		_executionConfiguration
				.setGroupByOptimizationEnabled(_groupByOptimizationCheckBox
						.isSelected());
		_executionConfiguration
				.setMaxConnections((Integer) _maxConnectionsCombobox
						.getSelectedItem());
		_executionConfiguration
				.setMaxQueriesPerConnection((Integer) _maxQueriesPerConnectionCombobox
						.getSelectedItem());
		Object item = _splitQueriesCombobox.getSelectedItem();
		if (item == NEVER_ITEM) {
			_executionConfiguration.setQuerySplitterSize(null);
		} else if (item instanceof QuerySplitOption) {
			QuerySplitOption option = (QuerySplitOption) item;
			_executionConfiguration.setQuerySplitterSize(option
					.getNumRowsRequired());
		} else {
			throw new IllegalStateException("Unknown item type: " + item);
		}
		dispose();
	}

	class QuerySplitOption {
		private long _numRowsRequired;

		public QuerySplitOption(long numRowsRequired) {
			_numRowsRequired = numRowsRequired;
		}

		public long getNumRowsRequired() {
			return _numRowsRequired;
		}

		@Override
		public int hashCode() {
			return (int) _numRowsRequired;
		}
	}
}
