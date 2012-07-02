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
package org.eobjects.datacleaner.lucene.ui;

import javax.swing.JComponent;

import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.lucene.SearchIndexCatalog;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.windows.AbstractDialog;

/**
 * Dialog for modifying the {@link SearchIndexCatalog}.
 */
public class ConfigureSearchIndicesDialog extends AbstractDialog {

    private static final long serialVersionUID = 1L;

    private final SearchIndexCatalog _catalog;
    private final UserPreferences _userPreferences;

    public ConfigureSearchIndicesDialog(WindowContext windowContext, SearchIndexCatalog catalog,
            UserPreferences userPreferences) {
        super(windowContext, Images.BANNER_IMAGE);
        _catalog = catalog;
        _userPreferences = userPreferences;
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
        final CreateSearchIndexPanel createSearchIndexPanel = new CreateSearchIndexPanel(_catalog, _userPreferences);

        final DCPanel panel = new DCPanel(Images.WATERMARK_IMAGE, 95, 95);
        panel.setPreferredSize(getDialogWidth(), 400);

        panel.add(createSearchIndexPanel);

        return panel;
    }

}
