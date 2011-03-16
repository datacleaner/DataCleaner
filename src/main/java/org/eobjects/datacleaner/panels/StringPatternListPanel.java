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

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.reference.RegexStringPattern;
import org.eobjects.analyzer.reference.SimpleStringPattern;
import org.eobjects.analyzer.reference.StringPattern;
import org.eobjects.datacleaner.regexswap.RegexSwapDialog;
import org.eobjects.datacleaner.regexswap.RegexSwapStringPattern;
import org.eobjects.datacleaner.user.MutableReferenceDataCatalog;
import org.eobjects.datacleaner.user.StringPatternChangeListener;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.DCLabel;
import org.eobjects.datacleaner.widgets.DCPopupBubble;
import org.eobjects.datacleaner.widgets.HelpIcon;
import org.eobjects.datacleaner.windows.RegexStringPatternDialog;
import org.eobjects.datacleaner.windows.SimpleStringPatternDialog;
import org.jdesktop.swingx.VerticalLayout;

public class StringPatternListPanel extends DCPanel implements StringPatternChangeListener {

	private static final long serialVersionUID = 1L;

	private static final ImageManager imageManager = ImageManager.getInstance();
	private final AnalyzerBeansConfiguration _configuration;
	private final MutableReferenceDataCatalog _catalog;
	private final DCPanel _listPanel;
	private final DCGlassPane _glassPane;

	public StringPatternListPanel(DCGlassPane glassPane, AnalyzerBeansConfiguration configuration) {
		super(WidgetUtils.BG_COLOR_BRIGHT, WidgetUtils.BG_COLOR_BRIGHTEST);
		_glassPane = glassPane;
		_configuration = configuration;
		_catalog = (MutableReferenceDataCatalog) _configuration.getReferenceDataCatalog();
		_catalog.addStringPatternListener(this);

		_listPanel = new DCPanel();
		_listPanel.setLayout(new VerticalLayout(4));

		updateComponents();

		final DCLabel newStringPatternsLabel = DCLabel.dark("Create new string patterns:");
		newStringPatternsLabel.setFont(WidgetUtils.FONT_HEADER);

		final DCLabel existingStringPatternsLabel = DCLabel.dark("Existing string patterns:");
		existingStringPatternsLabel.setFont(WidgetUtils.FONT_HEADER);

		setLayout(new VerticalLayout(10));
		add(newStringPatternsLabel);
		add(createNewStringPatternsPanel());
		add(Box.createVerticalStrut(10));
		add(existingStringPatternsLabel);
		setBorder(new EmptyBorder(10, 10, 10, 0));
		add(_listPanel);
	}

	private DCPanel createNewStringPatternsPanel() {

		final JButton simpleStringPatternButton = createButton("images/model/stringpattern_simple.png",
				"<html><b>Simple string pattern</b><br/>A string pattern based on simple string tokens, eg. 'Aaaaa 999'.</html>");
		simpleStringPatternButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new SimpleStringPatternDialog(_catalog).setVisible(true);
			}
		});

		final JButton regexStringPatternButton = createButton("images/model/stringpattern_regex.png",
				"<html><b>Regular expression string pattern</b><br/>A very flexible string pattern, based on regular expressions.</html>");
		regexStringPatternButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new RegexStringPatternDialog(_catalog).setVisible(true);
			}
		});

		final JButton regexSwapStringPatternButton = createButton("images/model/stringpattern_regexswap.png",
				"<html><b>Browse the RegexSwap</b><br/>Download patterns from DataCleaner's online RegexSwap.</html>");
		regexSwapStringPatternButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new RegexSwapDialog(_catalog).setVisible(true);
			}
		});

		final HelpIcon helpIcon = new HelpIcon(
				"<b>StringPatterns</b><br>"
						+ "String pattern provides a way to match string values against patterns. This is often useful for validation or categorization of values in semi- or unstructured columns.");

		final DCPanel panel = DCPanel.flow(simpleStringPatternButton, regexStringPatternButton,
				regexSwapStringPatternButton, Box.createHorizontalStrut(100), helpIcon);
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

		final String[] names = _catalog.getStringPatternNames();
		Arrays.sort(names);

		final Icon icon = imageManager.getImageIcon("images/model/stringpattern.png");

		for (final String name : names) {
			final StringPattern stringPattern = _catalog.getStringPattern(name);

			final DCLabel stringPatternLabel = DCLabel.dark("<html><b>" + name + "</b><br/>" + getDescription(stringPattern)
					+ "</html>");
			stringPatternLabel.setIcon(icon);

			final JButton editButton = WidgetFactory.createSmallButton("images/actions/edit.png");
			editButton.setToolTipText("Edit string pattern");

			if (stringPattern instanceof RegexStringPattern) {
				editButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						RegexStringPatternDialog dialog = new RegexStringPatternDialog((RegexStringPattern) stringPattern,
								_catalog);
						dialog.setVisible(true);
					}
				});
			} else if (stringPattern instanceof SimpleStringPattern) {
				editButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						SimpleStringPatternDialog dialog = new SimpleStringPatternDialog(
								(SimpleStringPattern) stringPattern, _catalog);
						dialog.setVisible(true);
					}
				});
			} else {
				editButton.setEnabled(false);
			}

			final JButton removeButton = WidgetFactory.createSmallButton("images/actions/remove.png");
			removeButton.setToolTipText("Remove string pattern");
			removeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					int result = JOptionPane.showConfirmDialog(StringPatternListPanel.this,
							"Are you sure you wish to remove the string pattern '" + name + "'?", "Confirm remove",
							JOptionPane.YES_NO_OPTION);
					if (result == JOptionPane.YES_OPTION) {
						_catalog.removeStringPattern(stringPattern);
					}
				}
			});

			if (!_catalog.isStringPatternMutable(name)) {
				editButton.setEnabled(false);
				removeButton.setEnabled(false);
			}

			final DCPanel stringPatternPanel = new DCPanel();
			stringPatternPanel.setBorder(WidgetUtils.BORDER_LIST_ITEM);
			WidgetUtils.addToGridBag(stringPatternLabel, stringPatternPanel, 0, 0, 1.0, 0.0);
			WidgetUtils.addToGridBag(editButton, stringPatternPanel, 1, 0, GridBagConstraints.EAST);
			WidgetUtils.addToGridBag(removeButton, stringPatternPanel, 2, 0, GridBagConstraints.EAST);
			_listPanel.add(stringPatternPanel);
		}

		if (names.length == 0) {
			_listPanel.add(DCLabel.dark("(none)"));
		}

		updateUI();
	}

	private String getDescription(StringPattern stringPattern) {
		if (stringPattern instanceof RegexSwapStringPattern) {
			return ((RegexSwapStringPattern) stringPattern).getRegex().getExpression();
		} else if (stringPattern instanceof RegexStringPattern) {
			return ((RegexStringPattern) stringPattern).getExpression();
		} else if (stringPattern instanceof SimpleStringPattern) {
			return ((SimpleStringPattern) stringPattern).getExpression();
		}
		return "";
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

	@Override
	public void removeNotify() {
		super.removeNotify();
		_catalog.removeStringPatternListener(this);
	}
}
