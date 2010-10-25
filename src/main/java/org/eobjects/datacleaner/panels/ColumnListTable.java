package org.eobjects.datacleaner.panels;

import java.awt.BorderLayout;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.datacleaner.actions.AddQuickTransformationActionListener;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.builder.WidgetFactory;
import org.eobjects.datacleaner.widgets.table.DCTable;
import org.jdesktop.swingx.HorizontalLayout;

import dk.eobjects.metamodel.schema.Table;

public final class ColumnListTable extends DCPanel {

	private static final long serialVersionUID = 1L;

	private static final String[] headers = new String[] { "Name", "Type", "" };
	private final ImageManager imageManager = ImageManager.getInstance();
	private final AnalyzerBeansConfiguration _configuration;
	private final AnalysisJobBuilder _analysisJobBuilder;
	private final Table _table;
	private final DCTable _columnTable;
	private final SortedSet<InputColumn<?>> _columns = new TreeSet<InputColumn<?>>();

	public ColumnListTable(Collection<? extends InputColumn<?>> columns, AnalyzerBeansConfiguration configuration,
			AnalysisJobBuilder analysisJobBuilder) {
		this(null, columns, configuration, analysisJobBuilder);
	}

	public ColumnListTable(Table table, AnalyzerBeansConfiguration configuration, AnalysisJobBuilder analysisJobBuilder) {
		this(table, null, configuration, analysisJobBuilder);
	}

	private ColumnListTable(Table table, Collection<? extends InputColumn<?>> columns,
			AnalyzerBeansConfiguration configuration, AnalysisJobBuilder analysisJobBuilder) {
		super();
		_table = table;
		_configuration = configuration;
		_analysisJobBuilder = analysisJobBuilder;

		setLayout(new BorderLayout());

		if (table != null) {
			JLabel tableNameLabel = new JLabel(table.getQualifiedLabel(), imageManager.getImageIcon(
					"images/model/column.png", IconUtils.ICON_SIZE_SMALL), JLabel.LEFT);
			tableNameLabel.setOpaque(false);
			tableNameLabel.setFont(WidgetUtils.FONT_HEADER);
			add(tableNameLabel, BorderLayout.NORTH);
		}

		_columnTable = new DCTable(headers);
		_columnTable.setColumnControlVisible(false);
		_columnTable.setRowHeight(28);

		JPanel tablePanel = _columnTable.toPanel();
		tablePanel.setBorder(WidgetUtils.BORDER_EMPTY);

		tablePanel.setBorder(new CompoundBorder(WidgetUtils.BORDER_EMPTY, WidgetUtils.BORDER_THIN));
		add(tablePanel, BorderLayout.CENTER);

		if (columns != null) {
			for (InputColumn<?> column : columns) {
				_columns.add(column);
			}
		}
		updateComponents();
	}

	private void updateComponents() {
		TableModel model = new DefaultTableModel(headers, _columns.size());
		int i = 0;
		Icon icon = imageManager.getImageIcon("images/model/column.png", IconUtils.ICON_SIZE_SMALL);
		for (InputColumn<?> column : _columns) {
			if (column instanceof MutableInputColumn<?>) {
				final JTextField textField = new JTextField(column.getName());
				final MutableInputColumn<?> mutableInputColumn = (MutableInputColumn<?>) column;
				textField.getDocument().addDocumentListener(new DocumentListener() {
					@Override
					public void removeUpdate(DocumentEvent e) {
						mutableInputColumn.setName(textField.getText());
					}

					@Override
					public void insertUpdate(DocumentEvent e) {
						mutableInputColumn.setName(textField.getText());
					}

					@Override
					public void changedUpdate(DocumentEvent e) {
						mutableInputColumn.setName(textField.getText());
					}
				});
				DCPanel panel = new DCPanel();
				panel.setLayout(new HorizontalLayout(4));
				panel.add(new JLabel(icon));
				panel.add(textField);
				model.setValueAt(panel, i, 0);
			} else {
				model.setValueAt(new JLabel(column.getName(), icon, JLabel.LEFT), i, 0);
			}
			model.setValueAt(column.getDataTypeFamily(), i, 1);

			final JButton transformButton = WidgetFactory.createSmallButton("images/component-types/transformer.png")
					.toComponent();

			transformButton.setToolTipText("Quick transformation");
			transformButton.addActionListener(new AddQuickTransformationActionListener(transformButton, _configuration,
					_analysisJobBuilder, column));

			DCPanel buttonPanel = new DCPanel();
			buttonPanel.add(transformButton);

			model.setValueAt(buttonPanel, i, 2);
			i++;
		}
		_columnTable.setModel(model);
	}

	public Table getTable() {
		return _table;
	}

	public void addColumn(InputColumn<?> column) {
		_columns.add(column);
		updateComponents();
	}

	public void removeColumn(InputColumn<?> column) {
		_columns.remove(column);
		updateComponents();
	}

	public void setColumns(List<? extends InputColumn<?>> columns) {
		_columns.clear();
		for (InputColumn<?> column : columns) {
			_columns.add(column);
		}
		updateComponents();
	}

	public int getColumnCount() {
		return _columns.size();
	}
}
