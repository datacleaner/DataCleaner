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
package org.eobjects.datacleaner.widgets;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.job.AnalysisJobMetadata;
import org.eobjects.analyzer.job.JaxbJobReader;
import org.eobjects.datacleaner.actions.OpenAnalysisJobActionListener;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.FileFilters;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.jdesktop.swingx.VerticalLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.eobjects.metamodel.util.ImmutableDate;

/**
 * The FileChooser "accessory" that will display analysis job information when
 * the user selected a job file.
 * 
 * @author Kasper Sørensen
 */
public class OpenAnalysisJobFileChooserAccessory extends DCPanel implements PropertyChangeListener {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(OpenAnalysisJobFileChooserAccessory.class);
	private final AnalyzerBeansConfiguration _configuration;
	private final DCFileChooser _fileChooser;
	private final DCPanel _panel;

	public OpenAnalysisJobFileChooserAccessory(AnalyzerBeansConfiguration configuration, DCFileChooser fileChooser) {
		super();
		_configuration = configuration;
		_panel = new DCPanel();
		_panel.setLayout(new VerticalLayout(0));
		_fileChooser = fileChooser;
		_fileChooser.addPropertyChangeListener(this);

		setBorder(new EmptyBorder(0, 10, 0, 0));
		setLayout(new BorderLayout());
		setVisible(false);

		final JLabel iconLabel = new JLabel(ImageManager.getInstance().getImageIcon("images/window/app-icon.png"));

		final JLabel headerLabel = new JLabel("<html>DataCleaner<br/>analysis job:</html>");
		headerLabel.setFont(WidgetUtils.FONT_HEADER);

		final DCPanel northPanel = new DCPanel();
		northPanel.setLayout(new VerticalLayout(0));
		northPanel.add(iconLabel);
		northPanel.add(Box.createVerticalStrut(10));
		northPanel.add(headerLabel);
		northPanel.add(Box.createVerticalStrut(10));

		add(northPanel, BorderLayout.NORTH);
		add(_panel, BorderLayout.CENTER);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(evt.getPropertyName())) {
			File selectedFile = _fileChooser.getSelectedFile();
			if (selectedFile != null
					&& selectedFile.getName().toLowerCase().endsWith(FileFilters.ANALYSIS_XML.getExtension())) {
				showFileInformation(selectedFile);
			} else {
				setVisible(false);
			}
		}
	}

	private void showFileInformation(final File file) {
		final JButton openJobButton = new JButton("Open analysis job");
		openJobButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				OpenAnalysisJobActionListener.openFile(file, _configuration);
				_fileChooser.cancelSelection();
			}
		});

		final JButton openAsTemplateButton = new JButton("Open as template");
		openAsTemplateButton
				.setToolTipText("Allows you to open the job with a different datastore and different source columns.");
		openAsTemplateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO: Use the JobReader.readColumnMapping method and present
				// alternatives.
				_fileChooser.cancelSelection();
			}
		});

		_panel.removeAll();

		try {
			JaxbJobReader reader = new JaxbJobReader(_configuration);
			AnalysisJobMetadata metadata = reader.readMetadata(new BufferedInputStream(new FileInputStream(file)));
			_panel.add(new JLabel("Datastore:"));
			_panel.add(new JLabel(metadata.getDatastoreName()));
			_panel.add(Box.createVerticalStrut(10));
			_panel.add(new JLabel("Source columns:"));
			List<String> paths = metadata.getSourceColumnPaths();
			for (String path : paths) {
				_panel.add(new JLabel(path));
			}
		} catch (Exception ex) {
			logger.warn("Exception occurred while reading job metadata", ex);
			_panel.add(new JLabel("Filename:"));
			_panel.add(new JLabel(file.getName()));
			_panel.add(Box.createVerticalStrut(10));
			_panel.add(new JLabel("Last modified:"));
			_panel.add(new JLabel(new ImmutableDate(file.lastModified()).toString()));
		}

		_panel.add(Box.createVerticalStrut(20));
		_panel.add(openJobButton);
		_panel.add(Box.createVerticalStrut(4));
		_panel.add(openAsTemplateButton);

		_panel.updateUI();
		setVisible(true);
	}
}
