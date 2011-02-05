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

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.reference.RegexStringPattern;
import org.eobjects.analyzer.reference.SimpleStringPattern;
import org.eobjects.analyzer.reference.StringPattern;
import org.eobjects.datacleaner.regexswap.Regex;
import org.eobjects.datacleaner.regexswap.RegexSwapDialog;
import org.eobjects.datacleaner.regexswap.RegexSwapStringPattern;
import org.eobjects.datacleaner.user.MutableReferenceDataCatalog;
import org.eobjects.datacleaner.user.StringPatternChangeListener;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.HelpIcon;
import org.eobjects.datacleaner.windows.RegexStringPatternDialog;
import org.eobjects.datacleaner.windows.SimpleStringPatternDialog;
import org.jdesktop.swingx.action.OpenBrowserAction;

public class StringPatternListPanel extends DCPanel implements StringPatternChangeListener {

	private static final long serialVersionUID = 1L;

	private static final ImageManager imageManager = ImageManager.getInstance();
	private final AnalyzerBeansConfiguration _configuration;
	private final MutableReferenceDataCatalog _catalog;

	private final JComboBox stringPatternsCombo;
	private final JButton _editButton = WidgetFactory.createSmallButton("images/actions/edit.png");
	private final JButton _removeButton = WidgetFactory.createSmallButton("images/actions/remove.png");
	private volatile StringPattern selectedStringPattern;

	public StringPatternListPanel(AnalyzerBeansConfiguration configuration) {
		super(WidgetUtils.BG_COLOR_BRIGHT, WidgetUtils.BG_COLOR_BRIGHTEST);
		_configuration = configuration;
		_catalog = (MutableReferenceDataCatalog) _configuration.getReferenceDataCatalog();
		_catalog.addStringPatternListener(this);

		final JToolBar toolBar = WidgetFactory.createToolBar();

		final String[] stringPatternNames = _configuration.getReferenceDataCatalog().getStringPatternNames();
		stringPatternsCombo = new JComboBox(stringPatternNames);
		stringPatternsCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String selectedStringPatternName = (String) stringPatternsCombo.getSelectedItem();
				selectedStringPattern = _catalog.getStringPattern(selectedStringPatternName);
				if (selectedStringPattern instanceof RegexSwapStringPattern) {
					_editButton.setIcon(imageManager.getImageIcon("images/actions/website.png", IconUtils.ICON_SIZE_SMALL));
					_editButton.setToolTipText("View pattern on RegexSwap");
					_removeButton.setEnabled(true);
				} else if (_catalog.isStringPatternMutable(selectedStringPatternName)) {
					_editButton.setIcon(imageManager.getImageIcon("images/actions/edit.png", IconUtils.ICON_SIZE_SMALL));
					_editButton.setToolTipText("Edit or test pattern");
					_removeButton.setEnabled(true);
				} else {
					_editButton.setIcon(imageManager.getImageIcon("images/actions/test-pattern.png",
							IconUtils.ICON_SIZE_SMALL));
					_editButton.setToolTipText("Test pattern");
					_removeButton.setEnabled(false);
				}

			}
		});


		_editButton.setToolTipText("Edit or test pattern");
		_editButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (selectedStringPattern instanceof RegexSwapStringPattern) {
					Regex regex = ((RegexSwapStringPattern) selectedStringPattern).getRegex();
					OpenBrowserAction actionListener = new OpenBrowserAction(regex.createWebsiteUrl());
					actionListener.actionPerformed(event);
				} else if (selectedStringPattern instanceof RegexStringPattern) {
					new RegexStringPatternDialog(selectedStringPattern.getName(),
							((RegexStringPattern) selectedStringPattern).getExpression(), _catalog).setVisible(true);
				} else if (selectedStringPattern instanceof SimpleStringPattern) {
					new SimpleStringPatternDialog(selectedStringPattern.getName(),
							((SimpleStringPattern) selectedStringPattern).getExpression(), _catalog).setVisible(true);
				}
			}
		});

		_removeButton.setToolTipText("Remove pattern");
		_removeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (_catalog.isStringPatternMutable(selectedStringPattern.getName())) {
					int result = JOptionPane.showConfirmDialog(StringPatternListPanel.this,
							"Are you sure you wish to remove the string pattern '" + "" + "'?", "Confirm remove",
							JOptionPane.YES_NO_OPTION);
					if (result == JOptionPane.YES_OPTION) {
						_catalog.removeStringPattern(selectedStringPattern);
					}
				} else {
					JOptionPane.showMessageDialog(StringPatternListPanel.this, "This string pattern cannot be deleted.");
				}
			}
		});
		// toolBar.add(_stringPatternListPanel);
		final JButton addButton = new JButton("New string pattern", imageManager.getImageIcon("images/actions/new.png"));
		addButton.setToolTipText("New string pattern");
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final JMenuItem regexStringPatternMenuItem = WidgetFactory.createMenuItem("Regular expression pattern",
						imageManager.getImageIcon("images/model/stringpattern_regex.png", IconUtils.ICON_SIZE_SMALL));
				regexStringPatternMenuItem.setEnabled(true);
				regexStringPatternMenuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						new RegexStringPatternDialog(_catalog).setVisible(true);
					}
				});

				final JMenuItem simpleStringPatternMenuItem = WidgetFactory.createMenuItem("Simple string pattern",
						imageManager.getImageIcon("images/model/stringpattern_simple.png", IconUtils.ICON_SIZE_SMALL));
				simpleStringPatternMenuItem.setEnabled(true);
				simpleStringPatternMenuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						new SimpleStringPatternDialog(_catalog).setVisible(true);
					}
				});

				final JMenuItem regexSwapMenuItem = WidgetFactory.createMenuItem("Import from the RegexSwap",
						imageManager.getImageIcon("images/model/stringpattern_regexswap.png", IconUtils.ICON_SIZE_SMALL));
				regexSwapMenuItem.setEnabled(true);
				regexSwapMenuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						new RegexSwapDialog(_catalog).setVisible(true);
					}
				});

				final JPopupMenu popup = new JPopupMenu();
				popup.add(regexStringPatternMenuItem);
				popup.add(simpleStringPatternMenuItem);
				popup.add(regexSwapMenuItem);
				popup.show(addButton, 0, addButton.getHeight());
			}
		});
		toolBar.add(addButton);
		toolBar.add(Box.createHorizontalGlue());
		toolBar.add(new HelpIcon(
				"<b>String patterns</b><br>"
						+ "String patterns are used for matching strings against representational patterns that describe the structure of a string.<br>"
						+ "String patterns can be used throughout DataCleaner for matching, discovery and categorization."));
		toolBar.add(Box.createHorizontalStrut(4));

		updateComponents();

		DCPanel listPanel = new DCPanel();
		WidgetUtils.addToGridBag(stringPatternsCombo, listPanel, 0, 0, 1.0, 1.0);
		WidgetUtils.addToGridBag(_editButton, listPanel, 1, 0);
		WidgetUtils.addToGridBag(_removeButton, listPanel, 2, 0);

		setLayout(new BorderLayout());
		add(listPanel, BorderLayout.CENTER);
		add(toolBar, BorderLayout.NORTH);

	}

	private void updateComponents() {
		stringPatternsCombo.removeAllItems();

		String[] names = _catalog.getStringPatternNames();
		Arrays.sort(names);
		for (int i = 0; i < names.length; i++) {
			final String name = names[i];
			stringPatternsCombo.addItem(name);
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
