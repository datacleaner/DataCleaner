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
package org.eobjects.datacleaner.windows;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.job.JaxbJobReader;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.util.FileFilters;

public class OpenAnalysisJobActionListener implements ActionListener {

	private final AnalyzerBeansConfiguration _configuration;

	public OpenAnalysisJobActionListener(AnalyzerBeansConfiguration configuration) {
		_configuration = configuration;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		UserPreferences userPreferences = UserPreferences.getInstance();

		JFileChooser fileChooser = new JFileChooser(userPreferences.getAnalysisJobDirectory());
		fileChooser.setFileFilter(FileFilters.ANALYSIS_XML);

		int openFileResult = fileChooser.showOpenDialog((Component) e.getSource());
		if (openFileResult == JFileChooser.APPROVE_OPTION) {
			JaxbJobReader reader = new JaxbJobReader(_configuration);
			File file = fileChooser.getSelectedFile();
			AnalysisJobBuilder ajb = reader.create(file);

			userPreferences.setAnalysisJobDirectory(file);

			AnalysisJobBuilderWindow window = new AnalysisJobBuilderWindow(_configuration, ajb, file.getName());
			window.setVisible(true);
		}

	}
}
