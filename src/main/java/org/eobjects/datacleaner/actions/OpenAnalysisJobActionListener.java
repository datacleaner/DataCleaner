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
package org.eobjects.datacleaner.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.job.AnalysisJobMetadata;
import org.eobjects.analyzer.job.JaxbJobReader;
import org.eobjects.analyzer.job.NoSuchDatastoreException;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.datacleaner.user.UsageLogger;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.util.FileFilters;
import org.eobjects.datacleaner.widgets.DCFileChooser;
import org.eobjects.datacleaner.widgets.OpenAnalysisJobFileChooserAccessory;
import org.eobjects.datacleaner.windows.AnalysisJobBuilderWindow;
import org.eobjects.datacleaner.windows.OpenAnalysisJobAsTemplateDialog;

/**
 * ActionListener that will display an "Open file" dialog which allows the user
 * to select a job file.
 * 
 * The class also contains a few reusable static methods for opening job files
 * without showing the dialog.
 * 
 * @author Kasper Sørensen
 */
public class OpenAnalysisJobActionListener implements ActionListener {

	private final AnalyzerBeansConfiguration _configuration;
	private final AnalysisJobBuilderWindow _window;

	public OpenAnalysisJobActionListener(AnalysisJobBuilderWindow window, AnalyzerBeansConfiguration configuration) {
		_window = window;
		_configuration = configuration;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		UsageLogger.getInstance().log("Open analysis job");

		UserPreferences userPreferences = UserPreferences.getInstance();
		DCFileChooser fileChooser = new DCFileChooser(userPreferences.getAnalysisJobDirectory());
		fileChooser.setAccessory(new OpenAnalysisJobFileChooserAccessory(_configuration, fileChooser));

		fileChooser.setFileFilter(FileFilters.ANALYSIS_XML);
		int openFileResult = fileChooser.showOpenDialog((Component) event.getSource());

		if (openFileResult == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			openFile(file, _configuration);
			if (_window != null && !_window.isDatastoreSet()) {
				_window.dispose();
			}
		}
	}

	public static void openFile(File file, AnalyzerBeansConfiguration configuration) {
		JaxbJobReader reader = new JaxbJobReader(configuration);
		try {
			AnalysisJobBuilder ajb = reader.create(file);

			openJob(file, configuration, ajb);
		} catch (NoSuchDatastoreException e) {
			AnalysisJobMetadata metadata = reader.readMetadata(file);
			int result = JOptionPane.showConfirmDialog(null, e.getMessage()
					+ "\n\nDo you wish to open this job as a template?", "Error: " + e.getMessage(),
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
			if (result == JOptionPane.OK_OPTION) {
				OpenAnalysisJobAsTemplateDialog dialog = new OpenAnalysisJobAsTemplateDialog(configuration, file, metadata);
				dialog.setVisible(true);
			}
		}
	}

	public static void openJob(File file, AnalyzerBeansConfiguration configuration, AnalysisJobBuilder ajb) {
		UserPreferences userPreferences = UserPreferences.getInstance();
		userPreferences.setAnalysisJobDirectory(file.getParentFile());
		userPreferences.addRecentJobFile(file);

		AnalysisJobBuilderWindow window = new AnalysisJobBuilderWindow(configuration, ajb, file.getName());
		window.setVisible(true);
	}
}
