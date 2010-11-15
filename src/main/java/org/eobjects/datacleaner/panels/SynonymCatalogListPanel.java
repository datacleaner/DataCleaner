package org.eobjects.datacleaner.panels;

import javax.swing.JLabel;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.jdesktop.swingx.VerticalLayout;

public final class SynonymCatalogListPanel extends DCPanel {

	private static final long serialVersionUID = 1L;

	private final AnalyzerBeansConfiguration _configuration;

	public SynonymCatalogListPanel(AnalyzerBeansConfiguration configuration) {
		super();
		_configuration = configuration;
		
		setLayout(new VerticalLayout(4));

		// TODO: This is just a very early implementation, simply displaying the
		// synonym catalogs in the configuration
		String[] names = _configuration.getReferenceDataCatalog().getSynonymCatalogNames();
		for (String name : names) {
			add(new JLabel(name));
		}
	}
}
