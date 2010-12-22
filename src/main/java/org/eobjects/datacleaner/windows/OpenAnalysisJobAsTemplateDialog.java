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

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.SourceColumnMapping;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.job.AnalysisJobMetadata;
import org.eobjects.analyzer.job.JaxbJobReader;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.util.CollectionUtils;
import org.eobjects.datacleaner.actions.OpenAnalysisJobActionListener;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.SourceColumnComboBox;

import dk.eobjects.metamodel.schema.Column;

/**
 * Dialog for opening a job as a template. This feature allows the user to reuse
 * an existing job but on a new set of columns, typically from a different
 * datastore.
 * 
 * @author Kasper Sørensen
 */
public class OpenAnalysisJobAsTemplateDialog extends AbstractDialog {

	private static final long serialVersionUID = 1L;

	private static final ImageManager imageManager = ImageManager.getInstance();

	private final AnalyzerBeansConfiguration _configuration;
	private final File _file;
	private final AnalysisJobMetadata _metadata;
	private final SourceColumnMapping _sourceColumnMapping;
	private final DatastoreCatalog _datastoreCatalog;
	private final JComboBox _datastoreCombobox;
	private final Map<String, List<SourceColumnComboBox>> _sourceColumnComboBoxes;
	private final JButton _openButton;

	private volatile Datastore _datastore;

	public OpenAnalysisJobAsTemplateDialog(AnalyzerBeansConfiguration configuration, File file, AnalysisJobMetadata metadata) {
		_configuration = configuration;
		_file = file;
		_metadata = metadata;
		_sourceColumnMapping = new SourceColumnMapping(metadata);

		_openButton = new JButton("Open job");
		_openButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JaxbJobReader reader = new JaxbJobReader(_configuration);
				try {
					SourceColumnMapping sourceColumnMapping = getSourceColumnMapping();

					AnalysisJobBuilder ajb = reader.create(new BufferedInputStream(new FileInputStream(_file)),
							sourceColumnMapping);
					OpenAnalysisJobActionListener.openJob(_file, _configuration, ajb);
					OpenAnalysisJobAsTemplateDialog.this.dispose();
				} catch (Exception e1) {
					throw new IllegalStateException(e1);
				}
			}
		});

		final List<String> columnPaths = _metadata.getSourceColumnPaths();
		_sourceColumnComboBoxes = new HashMap<String, List<SourceColumnComboBox>>();
		for (String columnPath : columnPaths) {
			int columnDelim = columnPath.lastIndexOf('.');
			assert columnDelim != -1;

			// this tablePath will be used to group together columns from the
			// same original table
			final String tablePath = columnPath.substring(0, columnDelim);

			final SourceColumnComboBox comboBox = new SourceColumnComboBox();
			comboBox.setName(columnPath);
			comboBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Column col = comboBox.getSelectedItem();
					if (col != null) {
						// make sure all comboboxes in a group use the same
						// table
						List<SourceColumnComboBox> comboBoxes = _sourceColumnComboBoxes.get(tablePath);
						for (SourceColumnComboBox sameTableComboBox : comboBoxes) {
							sameTableComboBox.setModel(_datastore, col.getTable());
						}
					}

				}
			});

			if (!_sourceColumnComboBoxes.containsKey(tablePath)) {
				_sourceColumnComboBoxes.put(tablePath, new ArrayList<SourceColumnComboBox>());
			}

			_sourceColumnComboBoxes.get(tablePath).add(comboBox);
		}

		_openButton.setEnabled(false);
		_datastoreCatalog = configuration.getDatastoreCatalog();

		final String[] datastoreNames = _datastoreCatalog.getDatastoreNames();
		// the combobox will contain all datastore names and a null for
		// "not selected"
		final String[] comboBoxModel = CollectionUtils.array(new String[1], datastoreNames);
		_datastoreCombobox = new JComboBox(comboBoxModel);
		_datastoreCombobox.setEditable(false);
		_datastoreCombobox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final String datastoreName = (String) _datastoreCombobox.getSelectedItem();
				_datastore = _datastoreCatalog.getDatastore(datastoreName);

				if (_datastore == null) {
					// no datastore selected
					_openButton.setEnabled(false);
				} else {
					_openButton.setEnabled(true);
				}

				_sourceColumnMapping.setDatastore(_datastore);

				for (List<SourceColumnComboBox> comboBoxes : _sourceColumnComboBoxes.values()) {
					for (SourceColumnComboBox comboBox : comboBoxes) {
						comboBox.setModel(_datastore);
					}
				}
			}
		});
	}

	public SourceColumnMapping getSourceColumnMapping() {
		for (List<SourceColumnComboBox> comboBoxes : _sourceColumnComboBoxes.values()) {
			for (SourceColumnComboBox comboBox : comboBoxes) {
				_sourceColumnMapping.setColumn(comboBox.getName(), comboBox.getSelectedItem());
			}
		}
		return _sourceColumnMapping;
	}

	@Override
	protected String getBannerTitle() {
		return "Open as template";
	}

	@Override
	protected int getDialogWidth() {
		return 600;
	}

	@Override
	protected JComponent getDialogContent() {
		final DCPanel panel = new DCPanel();

		int row = 0;
		WidgetUtils.addToGridBag(new JLabel("<html><b>Original value:</b></html>"), panel, 1, row);
		WidgetUtils.addToGridBag(new JLabel("<html><b>New/mapped value:</b></html>"), panel, 2, row);

		row++;
		WidgetUtils.addToGridBag(new JLabel(imageManager.getImageIcon("images/model/datastore.png")), panel, 0, row);
		WidgetUtils.addToGridBag(new JLabel(_metadata.getDatastoreName()), panel, 1, row, GridBagConstraints.WEST);
		WidgetUtils.addToGridBag(_datastoreCombobox, panel, 2, row, GridBagConstraints.WEST);

		Set<String> tableNames = _sourceColumnComboBoxes.keySet();
		for (final String tableName : tableNames) {
			row++;
			final JLabel tableLabel = new JLabel("<html><b>" + tableName + "</b></html>");
			tableLabel.setIcon(imageManager.getImageIcon("images/model/table.png", IconUtils.ICON_SIZE_SMALL));
			WidgetUtils.addToGridBag(tableLabel, panel, 0, row, 2, 1, GridBagConstraints.WEST);

			final JButton clearButton = new JButton("Clear");
			clearButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					List<SourceColumnComboBox> comboBoxes = _sourceColumnComboBoxes.get(tableName);
					for (SourceColumnComboBox comboBox : comboBoxes) {
						comboBox.setModel(_datastore, false);
					}
				}
			});
			final DCPanel clearButtonPanel = new DCPanel();
			clearButtonPanel.add(clearButton);
			WidgetUtils.addToGridBag(clearButtonPanel, panel, 2, row, GridBagConstraints.CENTER);

			final List<SourceColumnComboBox> comboBoxes = _sourceColumnComboBoxes.get(tableName);
			for (SourceColumnComboBox comboBox : comboBoxes) {
				row++;
				WidgetUtils.addToGridBag(
						new JLabel(imageManager.getImageIcon("images/model/column.png", IconUtils.ICON_SIZE_SMALL)), panel,
						0, row);
				WidgetUtils.addToGridBag(new JLabel(comboBox.getName()), panel, 1, row, GridBagConstraints.WEST);
				WidgetUtils.addToGridBag(comboBox, panel, 2, row, GridBagConstraints.WEST);
			}
		}

		row++;
		final DCPanel openButtonPanel = new DCPanel();
		openButtonPanel.add(_openButton);
		WidgetUtils.addToGridBag(openButtonPanel, panel, 2, row, GridBagConstraints.EAST);

		return panel;
	}

	@Override
	protected String getWindowTitle() {
		return "Open analysis job as template";
	}

}
