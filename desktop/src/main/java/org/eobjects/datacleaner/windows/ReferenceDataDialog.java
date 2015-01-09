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

import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.guice.InjectorBuilder;
import org.eobjects.datacleaner.panels.DCGlassPane;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.panels.DictionaryListPanel;
import org.eobjects.datacleaner.panels.StringPatternListPanel;
import org.eobjects.datacleaner.panels.SynonymCatalogListPanel;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.DescriptionLabel;
import org.eobjects.datacleaner.widgets.NeopostToolbarButton;
import org.eobjects.datacleaner.widgets.tabs.CloseableTabbedPane;

import com.google.inject.Injector;

public final class ReferenceDataDialog extends AbstractDialog {

    public static final int REFERENCE_DATA_ITEM_MAX_WIDTH = 280;

    private static final long serialVersionUID = 1L;
    private static final ImageManager imageManager = ImageManager.get();

    private final CloseableTabbedPane _tabbedPane;
    private final DCGlassPane _glassPane;
    private final InjectorBuilder _injectorBuilder;
    private volatile int _selectedTab;

    @Inject
    protected ReferenceDataDialog(WindowContext windowContext, InjectorBuilder injectorBuilder) {
        super(windowContext, imageManager.getImage("images/window/banner-reference-data.png"));
        _glassPane = new DCGlassPane(this);
        _tabbedPane = new CloseableTabbedPane(true);
        _tabbedPane.bindTabTitleToBanner(getBanner());
        _injectorBuilder = injectorBuilder;
    }

    private JComponent scrolleable(JComponent comp) {
        JScrollPane scroll = WidgetUtils.scrolleable(comp);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return scroll;
    }

    public void selectDictionariesTab() {
        _selectedTab = 0;
        updateSelectedTab();
    }

    public void selectSynonymsTab() {
        _selectedTab = 1;
        updateSelectedTab();
    }

    public void selectStringPatternsTab() {
        _selectedTab = 2;
        updateSelectedTab();
    }

    private void updateSelectedTab() {
        if (_tabbedPane.getTabCount() > _selectedTab) {
            _tabbedPane.setSelectedIndex(_selectedTab);
        }
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
        Injector injectorWithGlassPane = _injectorBuilder.with(DCGlassPane.class, _glassPane).createInjector();

        final DictionaryListPanel dictionaryListPanel = injectorWithGlassPane.getInstance(DictionaryListPanel.class);
        final SynonymCatalogListPanel synonymCatalogListPanel = injectorWithGlassPane
                .getInstance(SynonymCatalogListPanel.class);
        final StringPatternListPanel stringPatternListPanel = injectorWithGlassPane
                .getInstance(StringPatternListPanel.class);

        _tabbedPane.addTab("Dictionaries", new ImageIcon(imageManager.getImage("images/model/dictionary.png")),
                scrolleable(dictionaryListPanel));
        _tabbedPane.addTab("Synonyms", new ImageIcon(imageManager.getImage("images/model/synonym.png")),
                scrolleable(synonymCatalogListPanel));
        _tabbedPane.addTab("String patterns", new ImageIcon(imageManager.getImage("images/model/stringpattern.png")),
                scrolleable(stringPatternListPanel));

        _tabbedPane.setUnclosableTab(0);
        _tabbedPane.setUnclosableTab(1);
        _tabbedPane.setUnclosableTab(2);

        updateSelectedTab();

        _tabbedPane.setPreferredSize(new Dimension(getDialogWidth(), 550));

        final JButton closeButton = WidgetFactory.createButton("Close", "images/actions/save.png");
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ReferenceDataDialog.this.dispose();
            }
        });

        final JToolBar toolBar = WidgetFactory.createToolBar();
        toolBar.add(new NeopostToolbarButton());
        toolBar.add(WidgetFactory.createToolBarSeparator());
        toolBar.add(closeButton);

        final DCPanel toolBarPanel = new DCPanel(WidgetUtils.COLOR_ALTERNATIVE_BACKGROUND);
        toolBarPanel.setLayout(new BorderLayout());
        toolBarPanel.add(toolBar, BorderLayout.CENTER);

        final DCPanel panel = new DCPanel(WidgetUtils.COLOR_ALTERNATIVE_BACKGROUND);
        panel.setLayout(new BorderLayout());
        panel.add(
                new DescriptionLabel(
                        "Reference data is used throughout DataCleaner. In this dialog you can set up your own "
                                + "reference data items for identifying items in dictionaries (whitelists and blacklists), "
                                + "synonym catalogs (usually used for replacement) and in string patterns (used for pattern matching)."),
                BorderLayout.NORTH);
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
