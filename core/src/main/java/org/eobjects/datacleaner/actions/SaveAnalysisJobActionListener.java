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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.inject.Inject;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.JaxbJobMetadataFactoryImpl;
import org.eobjects.analyzer.job.JaxbJobWriter;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.datacleaner.Main;
import org.eobjects.datacleaner.user.UsageLogger;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.util.FileFilters;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.DCFileChooser;
import org.eobjects.datacleaner.windows.AnalysisJobBuilderWindow;

public final class SaveAnalysisJobActionListener implements ActionListener {

	private final AnalysisJobBuilder _analysisJobBuilder;
	private final AnalysisJobBuilderWindow _window;
	private final UserPreferences _userPreferences;
	private final UsageLogger _usageLogger;
	private final AnalyzerBeansConfiguration _configuration;

	@Inject
	protected SaveAnalysisJobActionListener(AnalysisJobBuilderWindow window,
			AnalysisJobBuilder analysisJobBuilder,
			UserPreferences userPreferences, UsageLogger usageLogger,
			AnalyzerBeansConfiguration configuration) {
		_window = window;
		_analysisJobBuilder = analysisJobBuilder;
		_userPreferences = userPreferences;
		_usageLogger = usageLogger;
		_configuration = configuration;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		_usageLogger.log("Save analysis job");

		AnalysisJob analysisJob = null;
		try {
			_window.applyPropertyValues();
			analysisJob = _analysisJobBuilder.toAnalysisJob();
		} catch (Exception e) {
			WidgetUtils.showErrorMessage("Errors in job",
					"Please fix the errors that exist in the job before saving it:\n\n"
							+ _window.getStatusLabelText(), e);
			return;
		}

		DCFileChooser fileChooser = new DCFileChooser(
				_userPreferences.getAnalysisJobDirectory());
		fileChooser.setFileFilter(FileFilters.ANALYSIS_XML);

		int result = fileChooser.showSaveDialog(_window.toComponent());
		if (result == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();

			if (!file.getName().endsWith(".xml")) {
				file = new File(file.getParentFile(), file.getName()
						+ FileFilters.ANALYSIS_XML.getExtension());
			}

			if (file.exists()) {
				int overwrite = JOptionPane.showConfirmDialog(
						_window.toComponent(),
						"Are you sure you want to overwrite the file '"
								+ file.getName() + "'?",
						"Overwrite existing file?", JOptionPane.YES_NO_OPTION);
				if (overwrite != JOptionPane.YES_OPTION) {
					return;
				}
			}

			_userPreferences.setAnalysisJobDirectory(file.getParentFile());

			String author = _userPreferences.getUsername();
			String jobName = null;
			String jobDescription = "Created with DataCleaner " + Main.VERSION;
			String jobVersion = null;

			final JaxbJobWriter writer = new JaxbJobWriter(_configuration,
					new JaxbJobMetadataFactoryImpl(author, jobName,
							jobDescription, jobVersion));

			BufferedOutputStream outputStream = null;
			try {
				outputStream = new BufferedOutputStream(new FileOutputStream(
						file));
				writer.write(analysisJob, outputStream);
				outputStream.flush();
				outputStream.close();
			} catch (IOException e1) {
				throw new IllegalStateException(e1);
			} finally {
				if (outputStream != null) {
					try {
						outputStream.close();
					} catch (Exception e2) {
						// do nothing
					}
				}
			}

			_userPreferences.addRecentJobFile(file);

			_window.setJobFilename(file.getName());
		}
	}
}
