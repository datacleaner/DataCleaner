/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Neopost - Customer Information Management
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
package org.datacleaner.panels;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.inject.Inject;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.guice.InjectorBuilder;
import org.datacleaner.reference.DatastoreSynonymCatalog;
import org.datacleaner.reference.SynonymCatalog;
import org.datacleaner.reference.TextFileSynonymCatalog;
import org.datacleaner.user.MutableReferenceDataCatalog;
import org.datacleaner.user.SynonymCatalogChangeListener;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.widgets.DCPopupBubble;
import org.datacleaner.widgets.HelpIcon;
import org.datacleaner.windows.DatastoreSynonymCatalogDialog;
import org.datacleaner.windows.ReferenceDataDialog;
import org.datacleaner.windows.TextFileSynonymCatalogDialog;
import org.jdesktop.swingx.VerticalLayout;

import com.google.inject.Injector;

public final class SynonymCatalogListPanel extends DCPanel implements SynonymCatalogChangeListener {

	private static final long serialVersionUID = 1L;

	private static final ImageManager imageManager = ImageManager.get();
	private final DataCleanerConfiguration _configuration;
	private final MutableReferenceDataCatalog _catalog;
	private final DCPanel _listPanel;
	private final DCGlassPane _glassPane;
	private final InjectorBuilder _injectorBuilder;

	@Inject
	protected SynonymCatalogListPanel(DCGlassPane glassPane, DataCleanerConfiguration configuration,
			InjectorBuilder injectorBuilder) {
		super(WidgetUtils.COLOR_DEFAULT_BACKGROUND);
		_glassPane = glassPane;
		_configuration = configuration;
		_injectorBuilder = injectorBuilder;
		_catalog = (MutableReferenceDataCatalog) _configuration.getReferenceDataCatalog();
		_catalog.addSynonymCatalogListener(this);

		_listPanel = new DCPanel();
		_listPanel.setLayout(new VerticalLayout(4));

		updateComponents();

		final DCLabel newSynonymCatalogsLabel = DCLabel.dark("Create new synonym catalog:");
		newSynonymCatalogsLabel.setFont(WidgetUtils.FONT_HEADER1);

		final DCLabel existingSynonymCatalogsLabel = DCLabel.dark("Existing synonym catalogs:");
		existingSynonymCatalogsLabel.setFont(WidgetUtils.FONT_HEADER1);

		setLayout(new VerticalLayout(10));
		add(newSynonymCatalogsLabel);
		add(createNewSynonymCatalogsPanel());
		add(Box.createVerticalStrut(10));
		add(existingSynonymCatalogsLabel);
		setBorder(new EmptyBorder(10, 10, 10, 0));
		add(_listPanel);
	}

	private DCPanel createNewSynonymCatalogsPanel() {

		final JButton textFileSynonymCatalogButton = createButton(
				IconUtils.SYNONYM_CATALOG_TEXTFILE_IMAGEPATH,
				"<html><b>Text file synonym catalog</b><br/>Synonyms from a file with master terms and comma-separated synonyms.</html>");
		textFileSynonymCatalogButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Injector injector = _injectorBuilder.with(TextFileSynonymCatalog.class, null).createInjector();
				TextFileSynonymCatalogDialog dialog = injector.getInstance(TextFileSynonymCatalogDialog.class);
				dialog.setVisible(true);
			}
		});

		final JButton datastoreSynonymCatalogButton = createButton(IconUtils.SYNONYM_CATALOG_DATASTORE_IMAGEPATH,
				"<html><b>Datastore synonym catalog</b><br/>Synonym catalog based on columns in a registered datastore.</html>");
		datastoreSynonymCatalogButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Injector injector = _injectorBuilder.with(DatastoreSynonymCatalog.class, null).createInjector();
				DatastoreSynonymCatalogDialog dialog = injector.getInstance(DatastoreSynonymCatalogDialog.class);
				dialog.setVisible(true);
			}
		});

		final HelpIcon helpIcon = new HelpIcon("<b>Synonym catalogs</b><br>"
				+ "A synonym catalog is reference data used for replacing odd values with correct ones. "
				+ "A synonym in this sense can also be eg. a standardized value or a code for a given entity.<br>"
				+ "Synonyms can be used throughout DataCleaner for matching, replacing values and more..");

		final DCPanel panel = DCPanel.flow(textFileSynonymCatalogButton, datastoreSynonymCatalogButton,
				Box.createHorizontalStrut(100), helpIcon);
		panel.setBorder(WidgetUtils.BORDER_LIST_ITEM);
		return panel;
	}

	private JButton createButton(String imagePath, String description) {
		final JButton button = WidgetFactory.createImageButton(imageManager.getImageIcon(imagePath));

		final DCPopupBubble popupBubble = new DCPopupBubble(_glassPane, description, 0, 0, imagePath);
		popupBubble.attachTo(button);

		return button;
	}

	private void updateComponents() {
		_listPanel.removeAll();

		final String[] names = _catalog.getSynonymCatalogNames();
		Arrays.sort(names);

		final Icon icon = imageManager.getImageIcon("images/model/synonym.png");

		for (final String name : names) {
			final SynonymCatalog synonymCatalog = _catalog.getSynonymCatalog(name);

			final DCLabel synonymCatalogLabel = DCLabel.dark("<html><b>" + name + "</b><br/>"
					+ getDescription(synonymCatalog) + "</html>");
			synonymCatalogLabel.setIcon(icon);
			synonymCatalogLabel.setMaximumWidth(ReferenceDataDialog.REFERENCE_DATA_ITEM_MAX_WIDTH);

			final JButton editButton = WidgetFactory.createSmallButton(IconUtils.ACTION_EDIT);
			editButton.setToolTipText("Edit synonym catalog");

			if (synonymCatalog instanceof TextFileSynonymCatalog) {
				editButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						Injector injector = _injectorBuilder.with(TextFileSynonymCatalog.class, synonymCatalog)
								.createInjector();
						TextFileSynonymCatalogDialog dialog = injector.getInstance(TextFileSynonymCatalogDialog.class);
						dialog.setVisible(true);
					}
				});
			} else if (synonymCatalog instanceof DatastoreSynonymCatalog) {
				editButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						Injector injector = _injectorBuilder.with(DatastoreSynonymCatalog.class, synonymCatalog)
								.createInjector();
						DatastoreSynonymCatalogDialog dialog = injector
								.getInstance(DatastoreSynonymCatalogDialog.class);
						dialog.setVisible(true);
					}
				});
			} else {
				editButton.setEnabled(false);
			}

			final JButton removeButton = WidgetFactory.createSmallButton(IconUtils.ACTION_REMOVE);
			removeButton.setToolTipText("Remove synonym catalog");
			removeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					int result = JOptionPane.showConfirmDialog(SynonymCatalogListPanel.this,
							"Are you sure you wish to remove the synonym catalog '" + name + "'?", "Confirm remove",
							JOptionPane.YES_NO_OPTION);
					if (result == JOptionPane.YES_OPTION) {
						_catalog.removeSynonymCatalog(synonymCatalog);
					}
				}
			});

			if (!_catalog.isSynonymCatalogMutable(name)) {
				editButton.setEnabled(false);
				removeButton.setEnabled(false);
			}

			final DCPanel synonymCatalogPanel = new DCPanel();
			synonymCatalogPanel.setBorder(WidgetUtils.BORDER_LIST_ITEM);
			WidgetUtils.addToGridBag(synonymCatalogLabel, synonymCatalogPanel, 0, 0, 1.0, 0.0);
			WidgetUtils.addToGridBag(editButton, synonymCatalogPanel, 1, 0, GridBagConstraints.EAST);
			WidgetUtils.addToGridBag(removeButton, synonymCatalogPanel, 2, 0, GridBagConstraints.EAST);
			_listPanel.add(synonymCatalogPanel);
		}

		if (names.length == 0) {
			_listPanel.add(DCLabel.dark("(none)"));
		}

		updateUI();
	}

	private static String getDescription(SynonymCatalog synonymCatalog) {
		if (synonymCatalog.getDescription() != null) {
			return synonymCatalog.getDescription();
		}
		if (synonymCatalog instanceof TextFileSynonymCatalog) {
			return ((TextFileSynonymCatalog) synonymCatalog).getFilename();
		} else if (synonymCatalog instanceof DatastoreSynonymCatalog) {
			DatastoreSynonymCatalog datastoreSynonymCatalog = (DatastoreSynonymCatalog) synonymCatalog;
			return datastoreSynonymCatalog.getDatastoreName() + ": "
					+ datastoreSynonymCatalog.getMasterTermColumnPath();
		}
		return "";
	}

	@Override
	public void onAdd(SynonymCatalog synonymCatalog) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				updateComponents();
			}
		});
	}

	@Override
	public void onRemove(SynonymCatalog synonymCatalog) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				updateComponents();
			}
		});
	}

	@Override
	public void removeNotify() {
		super.removeNotify();
		_catalog.removeSynonymCatalogListener(this);
	}
}
