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

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.DCLabel;
import org.jdesktop.swingx.VerticalLayout;

/**
 * Panel to select which datastore to use. Shown in the "source" tab, if no
 * datastore has been selected to begin with.
 * 
 * @author Kasper Sørensen
 */
public class SelectDatastorePanel extends DCPanel {

	private static final long serialVersionUID = 1L;

	private static final ImageManager imageManager = ImageManager.getInstance();
	private final DatastoreCatalog _datastoreCatalog;
	private final AnalysisJobBuilderWindow _analysisJobBuilderWindow;
	private final List<JCheckBox> _checkBoxes = new ArrayList<JCheckBox>();

	public SelectDatastorePanel(AnalyzerBeansConfiguration configuration, AnalysisJobBuilderWindow analysisJobBuilderWindow) {
		super();
		_datastoreCatalog = configuration.getDatastoreCatalog();
		_analysisJobBuilderWindow = analysisJobBuilderWindow;

		setBorder(WidgetUtils.BORDER_EMPTY);
		setLayout(new VerticalLayout(4));

		DCLabel headerLabel = DCLabel.dark("Please select a datastore for analysis");
		headerLabel.setFont(WidgetUtils.FONT_HEADER);
		headerLabel.setIcon(imageManager.getImageIcon("images/model/datastore.png"));
		add(headerLabel);

		String[] datastoreNames = _datastoreCatalog.getDatastoreNames();
		for (int i = 0; i < datastoreNames.length; i++) {
			final Datastore datastore = _datastoreCatalog.getDatastore(datastoreNames[i]);
			final Icon icon = IconUtils.getDatastoreIcon(datastore, IconUtils.ICON_SIZE_SMALL);
			final JCheckBox checkBox = new JCheckBox();
			checkBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					for (JCheckBox c : _checkBoxes) {
						if (checkBox == c) {
							c.setSelected(true);
						} else {
							c.setSelected(false);
						}
					}
				}
			});
			_checkBoxes.add(checkBox);
			final DCLabel datastoreNameLabel = DCLabel.dark(datastore.getName());
			datastoreNameLabel.setIcon(icon);
			datastoreNameLabel.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					checkBox.doClick();
				}
			});

			DCPanel panel = new DCPanel();
			panel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			panel.add(checkBox);
			panel.add(datastoreNameLabel);

			add(panel);
		}

		JButton button = new JButton("Analyze!", imageManager.getImageIcon("images/filetypes/analysis_job.png"));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int i = 0;
				for (JCheckBox c : _checkBoxes) {
					if (c.isSelected()) {
						String dsName = _datastoreCatalog.getDatastoreNames()[i];
						Datastore datastore = _datastoreCatalog.getDatastore(dsName);
						_analysisJobBuilderWindow.setDatastore(datastore);
						return;
					}
					i++;
				}
			}
		});
		DCPanel buttonPanel = new DCPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		buttonPanel.add(button);

		add(buttonPanel);
	}
}
