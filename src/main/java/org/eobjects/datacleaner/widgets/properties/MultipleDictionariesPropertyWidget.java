package org.eobjects.datacleaner.widgets.properties;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.descriptors.BeanDescriptor;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.reference.Dictionary;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.WindowManager;
import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.VerticalLayout;

public class MultipleDictionariesPropertyWidget extends AbstractPropertyWidget<Dictionary[]> {

	private final ChangeListener CHANGE_LISTENER = new ChangeListener() {
		@Override
		public void stateChanged(ChangeEvent e) {
			fireValueChanged();
		}
	};

	private final ActionListener selectAllActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			for (JCheckBox cb : _checkBoxes) {
				cb.setSelected(true);
			}
		}
	};

	private final ActionListener selectNoneActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			for (JCheckBox cb : _checkBoxes) {
				cb.setSelected(false);
			}
		}
	};

	private static final long serialVersionUID = 1L;

	private final AbstractBeanJobBuilder<?, ?, ?> _beanJobBuilder;
	private final AnalyzerBeansConfiguration _configuration;
	private volatile JCheckBox[] _checkBoxes;

	public MultipleDictionariesPropertyWidget(AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
			ConfiguredPropertyDescriptor propertyDescriptor) {
		super(propertyDescriptor);
		_beanJobBuilder = beanJobBuilder;
		_configuration = WindowManager.getInstance().getMainWindow().getConfiguration();
		setLayout(new VerticalLayout(2));
		updateComponents();
	}

	private void updateComponents() {
		removeAll();
		String[] dictionaryNames = _configuration.getReferenceDataCatalog().getDictionaryNames();
		Dictionary[] currentValue = (Dictionary[]) _beanJobBuilder.getConfiguredProperty(getPropertyDescriptor());

		DCPanel buttonPanel = new DCPanel();
		buttonPanel.setLayout(new HorizontalLayout(2));

		JButton selectAllButton = new JButton("Select all");
		selectAllButton.addActionListener(selectAllActionListener);
		buttonPanel.add(selectAllButton);

		JButton selectNoneButton = new JButton("Select none");
		selectNoneButton.addActionListener(selectNoneActionListener);
		buttonPanel.add(selectNoneButton);

		add(buttonPanel);

		_checkBoxes = new JCheckBox[dictionaryNames.length];
		if (_checkBoxes.length == 0) {
			_checkBoxes = new JCheckBox[1];
			_checkBoxes[0] = new JCheckBox("- no dictionaries available -");
			_checkBoxes[0].setOpaque(false);
			_checkBoxes[0].setEnabled(false);
			add(_checkBoxes[0]);
		} else {
			int i = 0;
			for (String dictionaryName : dictionaryNames) {
				JCheckBox checkBox = new JCheckBox(dictionaryName, isEnabled(dictionaryName, currentValue));
				checkBox.setOpaque(false);
				checkBox.addChangeListener(CHANGE_LISTENER);
				_checkBoxes[i] = checkBox;
				add(checkBox);
				i++;
			}
		}
		fireValueChanged();
	}

	private boolean isEnabled(String dictionaryName, Dictionary[] currentValue) {
		if (currentValue == null || currentValue.length == 0) {
			// set all to true if this is the only inputcolumn property
			BeanDescriptor<?> beanDescriptor = getPropertyDescriptor().getBeanDescriptor();
			if (beanDescriptor.getConfiguredPropertiesForInput().size() == 1) {
				return true;
			}
			return false;
		}
		for (Dictionary dict : currentValue) {
			if (dictionaryName.equals(dict.getName())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isSet() {
		for (JCheckBox checkBox : _checkBoxes) {
			if (checkBox.isSelected()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Dictionary[] getValue() {
		List<Dictionary> result = new ArrayList<Dictionary>();
		for (int i = 0; i < _checkBoxes.length; i++) {
			if (_checkBoxes[i].isSelected()) {
				String dictionaryName = _checkBoxes[i].getText();
				result.add(_configuration.getReferenceDataCatalog().getDictionary(dictionaryName));
			}
		}
		return result.toArray(new Dictionary[result.size()]);
	}
}
