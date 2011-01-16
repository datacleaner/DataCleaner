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
import org.eobjects.datacleaner.user.MutableReferenceDataCatalog;
import org.eobjects.datacleaner.user.StringPatternChangeListener;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.windows.RegexPatternDialog;
import org.eobjects.datacleaner.windows.SimpleStringPatternDialog;

public class StringPatternListPanel extends DCPanel implements StringPatternChangeListener {

	private static final long serialVersionUID = 1L;

	private static final ImageManager imageManager = ImageManager.getInstance();
	private final AnalyzerBeansConfiguration _configuration;
	private final MutableReferenceDataCatalog _catalog;

	private JComboBox stringPatternsCombo;
	private StringPattern selectedStringPattern;
	private final DCPanel _stringPatternListPanel;
	private final JButton editButton = WidgetFactory.createSmallButton("images/actions/edit.png");
	private final JButton removeButton = WidgetFactory.createSmallButton("images/actions/remove.png");

	public StringPatternListPanel(AnalyzerBeansConfiguration configuration) {
		super(WidgetUtils.BG_COLOR_BRIGHT, WidgetUtils.BG_COLOR_BRIGHTEST);
		_configuration = configuration;
		_catalog = (MutableReferenceDataCatalog) _configuration.getReferenceDataCatalog();
		_catalog.addStringPatternListener(this);
		_stringPatternListPanel = new DCPanel();

		JToolBar toolBar = WidgetFactory.createToolBar();

		String[] stringPatternNames = _configuration.getReferenceDataCatalog().getStringPatternNames();
		stringPatternsCombo = new JComboBox(stringPatternNames);
		stringPatternsCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String selectedStringPatternName = (String) stringPatternsCombo.getSelectedItem();
				selectedStringPattern = _catalog.getStringPattern(selectedStringPatternName);
				if(_catalog.isStringPatternMutable(selectedStringPatternName)){
					editButton.setIcon(imageManager.getImageIcon("images/actions/edit.png", 16));
					editButton.setToolTipText("Edit or test pattern");
					removeButton.setEnabled(true);
				} else{
					editButton.setIcon(imageManager.getImageIcon("images/actions/test-pattern.png", 16));
					editButton.setToolTipText("Test pattern");
					removeButton.setEnabled(false);
				}
			}
		});

		_stringPatternListPanel.add(stringPatternsCombo);

		editButton.setToolTipText("Edit or test pattern");
		editButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (selectedStringPattern instanceof RegexStringPattern) {
					new RegexPatternDialog(selectedStringPattern.getName(), ((RegexStringPattern) selectedStringPattern)
							.getExpression(), _catalog).setVisible(true);
				} else if (selectedStringPattern instanceof SimpleStringPattern) {
					new SimpleStringPatternDialog(selectedStringPattern.getName(),
							((SimpleStringPattern) selectedStringPattern).getExpression(), _catalog).setVisible(true);
				}
			}
		});
		_stringPatternListPanel.add(editButton);

		removeButton.setToolTipText("Remove pattern");
		removeButton.addActionListener(new ActionListener() {
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
		_stringPatternListPanel.add(removeButton);
		// toolBar.add(_stringPatternListPanel);
		final JButton addButton = new JButton("New string pattern", imageManager.getImageIcon("images/actions/new.png"));
		addButton.setToolTipText("New string pattern");
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JPopupMenu popup = new JPopupMenu();

				JMenuItem regexStringPatternMenuItem = WidgetFactory.createMenuItem("Regular expression pattern",
						imageManager.getImageIcon("images/model/pattern.png", IconUtils.ICON_SIZE_SMALL));
				regexStringPatternMenuItem.setEnabled(true);
				regexStringPatternMenuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						new RegexPatternDialog(_catalog).setVisible(true);
					}
				});

				JMenuItem simpleStringPatternMenuItem = WidgetFactory.createMenuItem("Simple string pattern", imageManager
						.getImageIcon("images/model/pattern.png", IconUtils.ICON_SIZE_SMALL));
				simpleStringPatternMenuItem.setEnabled(true);
				simpleStringPatternMenuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						new SimpleStringPatternDialog(_catalog).setVisible(true);
					}
				});

				popup.add(regexStringPatternMenuItem);
				popup.add(simpleStringPatternMenuItem);

				popup.show(addButton, 0, addButton.getHeight());
			}
		});
		toolBar.add(addButton);

		updateComponents();

		setLayout(new BorderLayout());
		add(_stringPatternListPanel, BorderLayout.CENTER);
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
