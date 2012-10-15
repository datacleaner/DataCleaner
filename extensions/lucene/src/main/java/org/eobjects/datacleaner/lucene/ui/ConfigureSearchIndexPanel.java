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

import java.io.File;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.lucene.FileSystemSearchIndex;
import org.eobjects.datacleaner.lucene.SearchIndex;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.DCLabel;
import org.eobjects.metamodel.util.FileHelper;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.VerticalLayout;

/**
 * Panel for creating a new search index.
 */
public class ConfigureSearchIndexPanel extends DCPanel {

    private static final long serialVersionUID = 1L;

    private final UserPreferences _userPreferences;

    private final JXTextField _nameTextField;
    private final JXTextField _descriptionTextField;
    private final JRadioButton _storageDatastoreFolderRadio;
    private final JRadioButton _storageTempFolderRadio;

    public ConfigureSearchIndexPanel(final UserPreferences userPreferences) {
        super(Images.WATERMARK_IMAGE, 95, 10);
        _userPreferences = userPreferences;
        
        setTitledBorder("Search index properties");

        _nameTextField = WidgetFactory.createTextField("Search index name");
        _descriptionTextField = WidgetFactory.createTextField("Description");

        int row = 0;
        WidgetUtils.addToGridBag(DCLabel.dark("Name:"), this, 0, row);
        WidgetUtils.addToGridBag(_nameTextField, this, 1, row);

        row++;
        WidgetUtils.addToGridBag(DCLabel.dark("Description:"), this, 0, row);
        WidgetUtils.addToGridBag(_descriptionTextField, this, 1, row);

        row++;
        WidgetUtils.addToGridBag(DCLabel.dark("Storage:"), this, 0, row);
        _storageDatastoreFolderRadio = new JRadioButton("DataCleaner's datastore folder", true);
        _storageDatastoreFolderRadio.setOpaque(false);
        _storageTempFolderRadio = new JRadioButton("My computer's temp folder", false);
        _storageTempFolderRadio.setOpaque(false);

        final DCPanel radioPanel = new DCPanel();
        radioPanel.setLayout(new VerticalLayout(2));
        radioPanel.add(_storageDatastoreFolderRadio);
        radioPanel.add(_storageTempFolderRadio);
        WidgetUtils.addToGridBag(radioPanel, this, 1, row);

        final ButtonGroup group = new ButtonGroup();
        group.add(_storageDatastoreFolderRadio);
        group.add(_storageTempFolderRadio);
        
    }

    public SearchIndex createSearchIndex() {
        final String name = _nameTextField.getText();
        if (StringUtils.isNullOrEmpty(name)) {
            WidgetUtils.showErrorMessage("No search index name", "Please fill in a search index name!", null);
            return null;
        }

        final File parentFolder;
        if (_storageDatastoreFolderRadio.isSelected()) {
            parentFolder = _userPreferences.getSaveDatastoreDirectory();
        } else {
            parentFolder = FileHelper.getTempDir();
        }
        final File folder = new File(parentFolder, "lucene." + name);
        folder.mkdirs();

        final FileSystemSearchIndex searchIndex = new FileSystemSearchIndex(name, folder);
        return searchIndex;
    }
}
