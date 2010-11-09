package org.eobjects.datacleaner.widgets.properties;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.reference.SynonymCatalog;
import org.eobjects.analyzer.util.CollectionUtils;
import org.eobjects.datacleaner.util.WindowManager;

public class SingleSynonymCatalogPropertyWidget extends AbstractPropertyWidget<SynonymCatalog> {

	private static final long serialVersionUID = 1L;
	private final JComboBox _comboBox;
	private final AnalyzerBeansConfiguration _configuration;

	public SingleSynonymCatalogPropertyWidget(ConfiguredPropertyDescriptor propertyDescriptor,
			AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder) {
		super(propertyDescriptor);

		_configuration = WindowManager.getInstance().getMainWindow().getConfiguration();
		String[] synonymCatalogNames = _configuration.getReferenceDataCatalog().getSynonymCatalogNames();

		if (!propertyDescriptor.isRequired()) {
			synonymCatalogNames = CollectionUtils.array(new String[1], synonymCatalogNames);
		}
		_comboBox = new JComboBox(synonymCatalogNames);
		_comboBox.setEditable(false);

		SynonymCatalog currentValue = (SynonymCatalog) beanJobBuilder.getConfiguredProperty(propertyDescriptor);
		if (currentValue != null) {
			_comboBox.setSelectedItem(currentValue.getName());
		}

		_comboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fireValueChanged();
			}
		});
	}

	@Override
	public SynonymCatalog getValue() {
		String synonymCatalogName = (String) _comboBox.getSelectedItem();
		return _configuration.getReferenceDataCatalog().getSynonymCatalog(synonymCatalogName);
	}

}
