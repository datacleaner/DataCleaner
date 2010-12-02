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
import java.util.Arrays;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JToolBar;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.datacleaner.user.MutableReferenceDataCatalog;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;

public class StringPatternListPanel extends DCPanel {

	private static final long serialVersionUID = 1L;

	private static final ImageManager imageManager = ImageManager.getInstance();
	private final AnalyzerBeansConfiguration _configuration;
	private final MutableReferenceDataCatalog _catalog;
	private final DCPanel _listPanel;

	public StringPatternListPanel(AnalyzerBeansConfiguration configuration) {
		super();
		_configuration = configuration;
		_catalog = (MutableReferenceDataCatalog) _configuration.getReferenceDataCatalog();
		_listPanel = new DCPanel();

		JToolBar toolBar = WidgetFactory.createToolBar();

		final JButton addMenuItem = new JButton("New pattern", imageManager.getImageIcon("images/actions/new.png"));
		addMenuItem.setToolTipText("New pattern");
		addMenuItem.setEnabled(false);
		toolBar.add(addMenuItem);

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

			String name = names[i];

			final JLabel patternLabel = new JLabel(name, icon, JLabel.LEFT);

			WidgetUtils.addToGridBag(patternLabel, _listPanel, 0, i);
		}
	}

}
