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
