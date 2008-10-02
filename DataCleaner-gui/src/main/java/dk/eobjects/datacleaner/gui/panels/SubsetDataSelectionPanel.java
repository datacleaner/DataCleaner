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
package dk.eobjects.datacleaner.gui.panels;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EmptyBorder;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dk.eobjects.datacleaner.data.ColumnSelection;
import dk.eobjects.datacleaner.gui.GuiBuilder;
import dk.eobjects.datacleaner.gui.GuiHelper;
import dk.eobjects.datacleaner.profiler.IProfileDescriptor;
import dk.eobjects.datacleaner.util.WeakObservable;
import dk.eobjects.datacleaner.util.WeakObserver;
import dk.eobjects.datacleaner.validator.IValidationRuleDescriptor;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.ColumnType;

public abstract class SubsetDataSelectionPanel extends JPanel implements
		WeakObserver {

	private static final long serialVersionUID = 3797363932708302358L;
	private final Log _log = LogFactory.getLog(getClass());

	private ColumnSelection _dataSelection;
	private JRadioButton _allDataRadio;
	private JRadioButton _subsetRadio;
	private JPanel _subsetPanel;
	private Map<Column, JCheckBox> _subsetCheckBoxes = new HashMap<Column, JCheckBox>();

	public static SubsetDataSelectionPanel createPanel(
			ColumnSelection columnSelection, final IProfileDescriptor descriptor) {
		return new SubsetDataSelectionPanel(columnSelection) {
			private static final long serialVersionUID = -6276416154139628188L;

			@Override
			protected boolean isSupported(ColumnType type) {
				return descriptor.isSupported(type);
			}
		};
	}

	public static SubsetDataSelectionPanel createPanel(
			ColumnSelection columnSelection,
			final IValidationRuleDescriptor descriptor) {
		return new SubsetDataSelectionPanel(columnSelection) {
			private static final long serialVersionUID = 2052988553638873200L;

			@Override
			protected boolean isSupported(ColumnType type) {
				return descriptor.isSupported(type);
			}
		};
	}

	public SubsetDataSelectionPanel(ColumnSelection columnSelection) {
		super();
		new GuiBuilder<JPanel>(this).applyVerticalLayout().applyTitledBorder("Apply to columns")
				.applyLightBackground();
		_dataSelection = columnSelection;
		_dataSelection.addObserver(this);
		_allDataRadio = new JRadioButton("All selected data.", true);
		_allDataRadio.setBackground(GuiHelper.BG_COLOR_LIGHT);
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(_allDataRadio);
		add(_allDataRadio);
		_subsetRadio = new JRadioButton("Subset of data:");
		_subsetRadio.setBackground(GuiHelper.BG_COLOR_LIGHT);
		buttonGroup.add(_subsetRadio);
		add(_subsetRadio);
		_subsetPanel = GuiHelper.createPanel().applyVerticalLayout()
				.toComponent();
		_subsetPanel.setBorder(new EmptyBorder(0, 20, 0, 0));
		add(_subsetPanel);
		updateProfileDataPanel();
	}

	@Override
	public void removeNotify() {
		super.removeNotify();
		_log.debug("removeNotify()");
		_dataSelection.deleteObserver(this);
		_dataSelection = null;
		Collection<JCheckBox> checkBoxes = _subsetCheckBoxes.values();
		for (JCheckBox checkBox : checkBoxes) {
			ActionListener[] actionListeners = checkBox.getActionListeners();
			for (ActionListener actionListener : actionListeners) {
				checkBox.removeActionListener(actionListener);
			}
		}
		_subsetCheckBoxes = null;
	}

	private void updateProfileDataPanel() {
		_allDataRadio.setSelected(true);
		_allDataRadio.setEnabled(true);
		_subsetPanel.removeAll();
		_subsetCheckBoxes.clear();
		List<Column> columns = _dataSelection.getColumns();
		for (Column column : columns) {
			boolean enabled = isSupported(column.getType());
			_subsetPanel.add(createCheckBox(column, enabled));
			if (!enabled) {
				_allDataRadio.setEnabled(false);
				_allDataRadio.setSelected(false);
				_subsetRadio.setSelected(true);
			}
		}
		if (!_allDataRadio.isEnabled()) {
			Set<Entry<Column, JCheckBox>> entrySet = _subsetCheckBoxes
					.entrySet();
			for (Entry<Column, JCheckBox> entry : entrySet) {
				JCheckBox checkBox = entry.getValue();
				if (checkBox.isEnabled()) {
					checkBox.setSelected(true);
				}
			}
		}
		_subsetPanel.updateUI();
	}

	protected abstract boolean isSupported(ColumnType type);

	private Component createCheckBox(Column column, boolean enabled) {
		final JCheckBox checkBox = new JCheckBox(GuiHelper
				.getLabelForColumn(column), false);
		checkBox.setBackground(GuiHelper.BG_COLOR_LIGHT);
		checkBox.setEnabled(enabled);
		checkBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (checkBox.isSelected()) {
					_subsetRadio.setSelected(true);
				}
			}
		});
		_subsetCheckBoxes.put(column, checkBox);
		return checkBox;
	}

	public List<Column> getSelectedColumns() {
		if (_subsetRadio.isSelected()) {
			List<Column> columns = new ArrayList<Column>();
			Set<Entry<Column, JCheckBox>> entrySet = _subsetCheckBoxes
					.entrySet();
			for (Entry<Column, JCheckBox> entry : entrySet) {
				JCheckBox checkBox = entry.getValue();
				if (checkBox.isSelected()) {
					columns.add(entry.getKey());
				}
			}
			return columns;
		}
		return _dataSelection.getColumns();
	}

	public void update(WeakObservable o) {
		if (o instanceof ColumnSelection) {
			updateProfileDataPanel();
		}
	}

	public JRadioButton getAllDataRadio() {
		return _allDataRadio;
	}

	public JRadioButton getSubsetRadio() {
		return _subsetRadio;
	}

	public Map<Column, JCheckBox> getSubsetCheckBoxes() {
		return _subsetCheckBoxes;
	}

	public void setSelectedColumns(Column[] columns) {
		_allDataRadio.setSelected(false);
		_subsetRadio.setSelected(true);
		Set<Entry<Column, JCheckBox>> entrySet = _subsetCheckBoxes.entrySet();
		for (Entry<Column, JCheckBox> entry : entrySet) {
			Column column = entry.getKey();
			JCheckBox checkBox = entry.getValue();
			if (ArrayUtils.indexOf(columns, column) != -1) {
				checkBox.setSelected(true);
			} else {
				checkBox.setSelected(false);
			}
		}
	}
}