/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
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
