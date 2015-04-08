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

import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.reference.RegexStringPattern;
import org.datacleaner.reference.SimpleStringPattern;
import org.datacleaner.reference.StringPattern;
import org.datacleaner.regexswap.RegexSwapDialog;
import org.datacleaner.regexswap.RegexSwapStringPattern;
import org.datacleaner.user.MutableReferenceDataCatalog;
import org.datacleaner.user.StringPatternChangeListener;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.widgets.DCPopupBubble;
import org.datacleaner.widgets.HelpIcon;
import org.datacleaner.windows.ReferenceDataDialog;
import org.datacleaner.windows.RegexStringPatternDialog;
import org.datacleaner.windows.SimpleStringPatternDialog;
import org.jdesktop.swingx.VerticalLayout;

public class StringPatternListPanel extends DCPanel implements StringPatternChangeListener {

	private static final long serialVersionUID = 1L;

	private static final ImageManager imageManager = ImageManager.get();
	private final DataCleanerConfiguration _configuration;
	private final MutableReferenceDataCatalog _catalog;
	private final DCPanel _listPanel;
	private final DCGlassPane _glassPane;
	private final WindowContext _windowContext;
	private final UserPreferences _userPreferences;

	@Inject
	protected StringPatternListPanel(DCGlassPane glassPane, DataCleanerConfiguration configuration,
			WindowContext windowContext, UserPreferences userPreferences) {
		super(WidgetUtils.COLOR_DEFAULT_BACKGROUND);
		_glassPane = glassPane;
		_configuration = configuration;
		_windowContext = windowContext;
		_userPreferences = userPreferences;
		_catalog = (MutableReferenceDataCatalog) _configuration.getReferenceDataCatalog();
		_catalog.addStringPatternListener(this);

		_listPanel = new DCPanel();
		_listPanel.setLayout(new VerticalLayout(4));

		updateComponents();

		final DCLabel newStringPatternsLabel = DCLabel.dark("Create new string pattern:");
		newStringPatternsLabel.setFont(WidgetUtils.FONT_HEADER1);

		final DCLabel existingStringPatternsLabel = DCLabel.dark("Existing string patterns:");
		existingStringPatternsLabel.setFont(WidgetUtils.FONT_HEADER1);

		setLayout(new VerticalLayout(10));
		add(newStringPatternsLabel);
		add(createNewStringPatternsPanel());
		add(Box.createVerticalStrut(10));
		add(existingStringPatternsLabel);
		setBorder(new EmptyBorder(10, 10, 10, 0));
		add(_listPanel);
	}

	private DCPanel createNewStringPatternsPanel() {

		final JButton simpleStringPatternButton = createButton(IconUtils.STRING_PATTERN_SIMPLE_IMAGEPATH,
				"<html><b>Simple string pattern</b><br/>A string pattern based on simple string tokens, eg. 'Aaaaa 999'.</html>");
		simpleStringPatternButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new SimpleStringPatternDialog(_catalog, _windowContext).setVisible(true);
			}
		});

		final JButton regexStringPatternButton = createButton(IconUtils.STRING_PATTERN_REGEX_IMAGEPATH,
				"<html><b>Regular expression string pattern</b><br/>A very flexible string pattern, based on regular expressions.</html>");
		regexStringPatternButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new RegexStringPatternDialog(_catalog, _windowContext).setVisible(true);
			}
		});

		final JButton regexSwapStringPatternButton = createButton(IconUtils.STRING_PATTERN_REGEXSWAP_IMAGEPATH,
				"<html><b>Browse the RegexSwap</b><br/>Download patterns from DataCleaner's online RegexSwap.</html>");
		regexSwapStringPatternButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new RegexSwapDialog(_catalog, _windowContext, _userPreferences).setVisible(true);
			}
		});

		final HelpIcon helpIcon = new HelpIcon(
				"<b>String patterns</b><br>"
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
			stringPatternLabel.setMaximumWidth(ReferenceDataDialog.REFERENCE_DATA_ITEM_MAX_WIDTH);

			final JButton editButton = WidgetFactory.createSmallButton(IconUtils.ACTION_EDIT);
			editButton.setToolTipText("Edit string pattern");

			if (stringPattern instanceof RegexStringPattern) {
				editButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						RegexStringPatternDialog dialog = new RegexStringPatternDialog((RegexStringPattern) stringPattern,
								_catalog, _windowContext);
						dialog.setVisible(true);
					}
				});
			} else if (stringPattern instanceof SimpleStringPattern) {
				editButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						SimpleStringPatternDialog dialog = new SimpleStringPatternDialog(
								(SimpleStringPattern) stringPattern, _catalog, _windowContext);
						dialog.setVisible(true);
					}
				});
			} else {
				editButton.setEnabled(false);
			}

			final JButton removeButton = WidgetFactory.createSmallButton(IconUtils.ACTION_REMOVE);
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

	private static String getDescription(StringPattern stringPattern) {
		if (stringPattern.getDescription() != null) {
			return stringPattern.getDescription();
		}
		final String description;
		if (stringPattern instanceof RegexSwapStringPattern) {
			description = ((RegexSwapStringPattern) stringPattern).getRegex().getExpression();
		} else if (stringPattern instanceof RegexStringPattern) {
			description = ((RegexStringPattern) stringPattern).getExpression();
		} else if (stringPattern instanceof SimpleStringPattern) {
			description = ((SimpleStringPattern) stringPattern).getExpression();
		} else {
			description = "";
		}

		if (description == null) {
			return "";
		}
		if (description.length() > 30) {
			return description.substring(0, 27) + "...";
		}
		return description;
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
