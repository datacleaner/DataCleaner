package org.eobjects.datacleaner.panels;

import javax.swing.JLabel;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.jdesktop.swingx.VerticalLayout;

public class DictionaryListPanel extends DCPanel {

	private static final long serialVersionUID = 1L;

	private final AnalyzerBeansConfiguration _configuration;

	public DictionaryListPanel(AnalyzerBeansConfiguration configuration) {
		super();
		_configuration = configuration;
		
		setLayout(new VerticalLayout(4));

		// TODO: This is just a very early implementation, simply displaying the
		// dictionaries in the configuration
		String[] names = _configuration.getReferenceDataCatalog().getDictionaryNames();
		for (String name : names) {
			add(new JLabel(name));
		}
	}
}
