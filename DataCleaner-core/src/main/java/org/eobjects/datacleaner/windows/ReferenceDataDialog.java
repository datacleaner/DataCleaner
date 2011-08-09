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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.inject.Inject;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.guice.DCModule;
import org.eobjects.datacleaner.panels.DCGlassPane;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.panels.DictionaryListPanel;
import org.eobjects.datacleaner.panels.StringPatternListPanel;
import org.eobjects.datacleaner.panels.SynonymCatalogListPanel;
import org.eobjects.datacleaner.user.DCConfiguration;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.HumanInferenceToolbarButton;
import org.eobjects.datacleaner.widgets.tabs.CloseableTabbedPane;

public class ReferenceDataDialog extends AbstractDialog {

	private static final long serialVersionUID = 1L;
	private static final ImageManager imageManager = ImageManager.getInstance();
	private final AnalyzerBeansConfiguration configuration = DCConfiguration.get();
	private final CloseableTabbedPane _tabbedPane;

	@Inject
	protected ReferenceDataDialog(WindowContext windowContext, DCModule parentModule) {
		super(windowContext, imageManager.getImage("images/window/banner-reference-data.png"));
		final DCGlassPane glassPane = new DCGlassPane(this);

		_tabbedPane = new CloseableTabbedPane();

		final DictionaryListPanel dictionaryListPanel = new DictionaryListPanel(glassPane, configuration, windowContext,
				parentModule);
		final SynonymCatalogListPanel synonymCatalogListPanel = new SynonymCatalogListPanel(glassPane, configuration,
				windowContext, parentModule);
		final StringPatternListPanel stringPatternListPanel = new StringPatternListPanel(glassPane, configuration,
				windowContext);

		_tabbedPane.addTab("Dictionaries", new ImageIcon(imageManager.getImage("images/model/dictionary.png")),
				scrolleable(dictionaryListPanel));
		_tabbedPane.addTab("Synonyms", new ImageIcon(imageManager.getImage("images/model/synonym.png")),
				scrolleable(synonymCatalogListPanel));
		_tabbedPane.addTab("String patterns", new ImageIcon(imageManager.getImage("images/model/stringpattern.png")),
				scrolleable(stringPatternListPanel));

		_tabbedPane.setUnclosableTab(0);
		_tabbedPane.setUnclosableTab(1);
		_tabbedPane.setUnclosableTab(2);

		_tabbedPane.setPreferredSize(new Dimension(getDialogWidth(), 550));
	}

	private JComponent scrolleable(JComponent comp) {
		JScrollPane scroll = WidgetUtils.scrolleable(comp);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		return scroll;
	}

	public void selectDictionariesTab() {
		_tabbedPane.setSelectedIndex(0);
	}

	public void selectSynonymsTab() {
		_tabbedPane.setSelectedIndex(1);
	}

	public void selectStringPatternsTab() {
		_tabbedPane.setSelectedIndex(2);
	}

	@Override
	protected String getBannerTitle() {
		return "Reference data";
	}

	@Override
	protected int getDialogWidth() {
		return 400;
	}

	@Override
	protected boolean isWindowResizable() {
		return true;
	}

	@Override
	protected JComponent getDialogContent() {
		final JButton closeButton = WidgetFactory.createButton("Close", "images/actions/save.png");
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ReferenceDataDialog.this.dispose();
			}
		});

		final JToolBar toolBar = WidgetFactory.createToolBar();
		toolBar.add(new HumanInferenceToolbarButton());
		toolBar.add(WidgetFactory.createToolBarSeparator());
		toolBar.add(closeButton);

		final DCPanel toolBarPanel = new DCPanel(WidgetUtils.BG_COLOR_DARKEST, WidgetUtils.BG_COLOR_DARKEST);
		toolBarPanel.setLayout(new BorderLayout());
		toolBarPanel.add(toolBar, BorderLayout.CENTER);

		final DCPanel panel = new DCPanel(WidgetUtils.BG_COLOR_DARK, WidgetUtils.BG_COLOR_DARK);
		panel.setLayout(new BorderLayout());
		panel.add(_tabbedPane, BorderLayout.CENTER);
		panel.add(toolBarPanel, BorderLayout.SOUTH);
		panel.setPreferredSize(getDialogWidth(), 500);
		return panel;
	}

	@Override
	public Image getWindowIcon() {
		return imageManager.getImage("images/model/reference-data.png");
	}

	@Override
	public String getWindowTitle() {
		return getBannerTitle();
	}

}
