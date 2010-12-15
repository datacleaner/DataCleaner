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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.reference.StringPattern;
import org.eobjects.datacleaner.user.MutableReferenceDataCatalog;
import org.eobjects.datacleaner.user.StringPatternChangeListener;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;

public class StringPatternListPanel extends DCPanel implements StringPatternChangeListener {

	private static final long serialVersionUID = 1L;

	private static final ImageManager imageManager = ImageManager.getInstance();
	private final AnalyzerBeansConfiguration _configuration;
	private final MutableReferenceDataCatalog _catalog;
	private final DCPanel _listPanel;

	public StringPatternListPanel(AnalyzerBeansConfiguration configuration) {
		super();
		_configuration = configuration;
		_catalog = (MutableReferenceDataCatalog) _configuration.getReferenceDataCatalog();
		_catalog.addStringPatternListener(this);
		_listPanel = new DCPanel();

		JToolBar toolBar = WidgetFactory.createToolBar();

		final JButton addButton = new JButton("New string pattern", imageManager.getImageIcon("images/actions/new.png"));
		addButton.setToolTipText("New string pattern");
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JPopupMenu popup = new JPopupMenu();

				JMenuItem regexStringPatternMenuItem = WidgetFactory.createMenuItem("Regular expression pattern",
						imageManager.getImageIcon("images/model/pattern.png", IconUtils.ICON_SIZE_SMALL));
				regexStringPatternMenuItem.setEnabled(false);

				JMenuItem simpleStringPatternMenuItem = WidgetFactory.createMenuItem("Simple string pattern",
						imageManager.getImageIcon("images/model/pattern.png", IconUtils.ICON_SIZE_SMALL));
				simpleStringPatternMenuItem.setEnabled(false);

				popup.add(regexStringPatternMenuItem);
				popup.add(simpleStringPatternMenuItem);

				popup.show(addButton, 0, addButton.getHeight());
			}
		});
		toolBar.add(addButton);

		updateComponents();

		setLayout(new BorderLayout());
		add(toolBar, BorderLayout.NORTH);
		add(_listPanel, BorderLayout.CENTER);
	}

	private void updateComponents() {
		_listPanel.removeAll();

		String[] names = _catalog.getStringPatternNames();
		Arrays.sort(names);

		Icon icon = imageManager.getImageIcon("images/model/pattern.png", IconUtils.ICON_SIZE_SMALL);

		for (int i = 0; i < names.length; i++) {

			final String name = names[i];

			final StringPattern sp = _catalog.getStringPattern(name);

			final JLabel patternLabel = new JLabel(name, icon, JLabel.LEFT);
			WidgetUtils.addToGridBag(patternLabel, _listPanel, 0, i, 1.0, 0.0);

			if (_catalog.isStringPatternMutable(name)) {
				final JButton removeButton = WidgetFactory.createSmallButton("images/actions/remove.png");
				removeButton.setToolTipText("Remove string pattern");
				removeButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						int result = JOptionPane.showConfirmDialog(StringPatternListPanel.this,
								"Are you sure you wish to remove the string pattern '" + name + "'?", "Confirm remove",
								JOptionPane.YES_NO_OPTION);
						if (result == JOptionPane.YES_OPTION) {
							_catalog.removeStringPattern(sp);
						}
					}
				});
				WidgetUtils.addToGridBag(removeButton, _listPanel, 1, i);
			}
		}
		updateUI();
	}

	@Override
	public void onAdd(StringPattern stringPattern) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				updateComponents();
			}
		});
	}

	@Override
	public void onRemove(StringPattern stringPattern) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				updateComponents();
			}
		});
	}

}
