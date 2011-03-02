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
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.datacleaner.panels.DCGlassPane;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.DCLabel;
import org.eobjects.datacleaner.widgets.DCPopupBubble;
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
	private final DCGlassPane _glassPane;

	public SelectDatastorePanel(AnalyzerBeansConfiguration configuration, AnalysisJobBuilderWindow analysisJobBuilderWindow,
			DCGlassPane glassPane) {
		super();
		_datastoreCatalog = configuration.getDatastoreCatalog();
		_analysisJobBuilderWindow = analysisJobBuilderWindow;
		_glassPane = glassPane;

		setBorder(WidgetUtils.BORDER_EMPTY);
		setLayout(new VerticalLayout(4));

		final DCLabel headerLabel = DCLabel.dark("Select datastore for analysis");
		headerLabel.setFont(WidgetUtils.FONT_HEADER);
		add(headerLabel);

		final DCLabel createNewDatastoreLabel = DCLabel.dark("Create a new datastore:");
		createNewDatastoreLabel.setFont(WidgetUtils.FONT_HEADER);

		final DCPanel newDatastorePanel = new DCPanel();
		newDatastorePanel.setLayout(new VerticalLayout(4));
		newDatastorePanel.setBorder(new EmptyBorder(10, 10, 10, 0));
		newDatastorePanel.add(createNewDatastoreLabel);
		newDatastorePanel.add(createNewDatastorePanel());

		add(newDatastorePanel);

		final DCLabel existingDatastoresLabel = DCLabel.dark("Analyze an existing datastore:");
		existingDatastoresLabel.setFont(WidgetUtils.FONT_HEADER);

		final DCPanel existingDatastoresPanel = new DCPanel();
		existingDatastoresPanel.setLayout(new VerticalLayout(4));
		existingDatastoresPanel.setBorder(new EmptyBorder(10, 10, 10, 0));
		existingDatastoresPanel.add(existingDatastoresLabel);

		String[] datastoreNames = _datastoreCatalog.getDatastoreNames();
		for (int i = 0; i < datastoreNames.length; i++) {
			final Datastore datastore = _datastoreCatalog.getDatastore(datastoreNames[i]);
			final Icon icon = IconUtils.getDatastoreIcon(datastore);
			final JCheckBox checkBox = new JCheckBox();
			checkBox.setOpaque(false);
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
			final DCLabel datastoreNameLabel = DCLabel.dark("<html><b>" + datastore.getName() + "</b><br/>" + datastore + "</html>");
			datastoreNameLabel.setIconTextGap(10);
			datastoreNameLabel.setIcon(icon);
			datastoreNameLabel.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					checkBox.doClick();
				}
			});

			final DCPanel panel = new DCPanel();
			panel.setBorder(new MatteBorder(0, 2, 1, 0, WidgetUtils.BG_COLOR_MEDIUM));
			panel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
			panel.add(checkBox);
			panel.add(datastoreNameLabel);

			existingDatastoresPanel.add(panel);
		}
		
		if (!_checkBoxes.isEmpty()) {
			_checkBoxes.get(0).doClick();
		}

		add(existingDatastoresPanel);

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

	private DCPanel createNewDatastorePanel() {
		DCPanel panel = new DCPanel();
		panel.setBorder(new MatteBorder(0, 2, 1, 0, WidgetUtils.BG_COLOR_MEDIUM));
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
		panel.add(createNewDatastoreButton("CSV file", "Comma-separated values (CSV) file (or file with other separators)",
				"images/datastore-types/csv.png"));
		panel.add(createNewDatastoreButton("Excel spreadsheet",
				"Microsoft Excel spreadsheet. Either .xls (97-2003) or .xlsx (2007+) format.",
				"images/datastore-types/excel.png"));
		panel.add(createNewDatastoreButton("Access database", "", "images/datastore-types/access.png"));
		panel.add(createNewDatastoreButton("DBase database", "", "images/datastore-types/dbase.png"));
		panel.add(createNewDatastoreButton("XML file", "", "images/datastore-types/xml.png"));
		panel.add(createNewDatastoreButton("OpenOffice.org base database", "", "images/datastore-types/odb.png"));
		return panel;
	}

	private JButton createNewDatastoreButton(final String title, final String description, final String imagePath) {
		ImageIcon icon = imageManager.getImageIcon(imagePath);
		final JButton button = new JButton(icon);
		button.setMargin(new Insets(0, 0, 0, 0));
		button.setBorder(null);
		button.setOpaque(false);
		final DCPopupBubble popupBubble = new DCPopupBubble(_glassPane, "<html><b>" + title + "</b><br/>" + description
				+ "</html>", 0, 0, imagePath);
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				Point locationOnScreen = button.getLocationOnScreen();
				popupBubble.setLocationOnScreen(locationOnScreen.x + 15, locationOnScreen.y + button.getHeight());
				popupBubble.show();
			}

			@Override
			public void mouseExited(MouseEvent e) {
				popupBubble.hide();
			}
		});
		return button;
	}
}
