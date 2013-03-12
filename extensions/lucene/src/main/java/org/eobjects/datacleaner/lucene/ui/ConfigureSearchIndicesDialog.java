/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.lucene.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JToolBar;

import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.lucene.SearchIndex;
import org.eobjects.datacleaner.lucene.SearchIndexCatalog;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.DCComboBox;
import org.eobjects.datacleaner.widgets.tabs.CloseableTabbedPane;
import org.eobjects.datacleaner.windows.AbstractDialog;

/**
 * Dialog for modifying the {@link SearchIndexCatalog}.
 */
public class ConfigureSearchIndicesDialog extends AbstractDialog {

    private static final long serialVersionUID = 1L;

    private final SearchIndexCatalog _catalog;
    private final UserPreferences _userPreferences;
    private final DCComboBox<String> _comboBox;

    public ConfigureSearchIndicesDialog(WindowContext windowContext, SearchIndexCatalog catalog,
            UserPreferences userPreferences, DCComboBox<String> comboBox) {
        super(windowContext, Images.BANNER_IMAGE);
        _catalog = catalog;
        _userPreferences = userPreferences;
        _comboBox = comboBox;
    }

    @Override
    public String getWindowTitle() {
        return "Configure search indices";
    }

    @Override
    protected String getBannerTitle() {
        return "Search indices\nPowered by Apache Lucene";
    }

    @Override
    protected int getDialogWidth() {
        return 500;
    }

    @Override
    protected JComponent getDialogContent() {
        final ConfigureSearchIndexPanel createSearchIndexPanel = new ConfigureSearchIndexPanel(_userPreferences);

        final ImageIcon saveIcon = ImageManager.getInstance().getImageIcon("images/actions/save.png");
        final JButton saveButton = WidgetFactory.createButton("Save", saveIcon);
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                final SearchIndex searchIndex = createSearchIndexPanel.createSearchIndex();
                if (searchIndex == null) {
                    return;
                }
                _catalog.addSearchIndex(searchIndex);
                _comboBox.setSelectedItem(searchIndex.getName());
                dispose();
            }
        });

        final CloseableTabbedPane tabbedPane = new CloseableTabbedPane();
        tabbedPane.addTab("Configure search index", ImageManager.getInstance().getImageIcon(IconUtils.MENU_OPTIONS),
                createSearchIndexPanel);
        tabbedPane.setUnclosableTab(0);

        final JToolBar toolBar = WidgetFactory.createToolBar();
        toolBar.add(WidgetFactory.createToolBarSeparator());
        toolBar.add(saveButton);

        final DCPanel toolBarPanel = new DCPanel(WidgetUtils.BG_COLOR_DARKEST, WidgetUtils.BG_COLOR_DARKEST);
        toolBarPanel.setLayout(new BorderLayout());
        toolBarPanel.add(toolBar, BorderLayout.CENTER);

        final DCPanel panel = new DCPanel();
        panel.setLayout(new BorderLayout());
        panel.setPreferredSize(getDialogWidth(), 360);
        panel.add(tabbedPane, BorderLayout.CENTER);
        panel.add(toolBarPanel, BorderLayout.SOUTH);

        return panel;
    }

}
