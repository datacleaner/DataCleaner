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
package org.datacleaner.widgets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JMenuItem;

import org.apache.commons.vfs2.FileObject;
import org.datacleaner.actions.OpenAnalysisJobActionListener;
import org.datacleaner.panels.DatastoreManagementPanel;
import org.datacleaner.util.FileFilters;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;

/**
 * Menu item that represents opening a "recent" job on the
 * {@link DatastoreManagementPanel} 's "more jobs" drop down menu.
 */
public class OpenAnalysisJobMenuItem extends JMenuItem implements ActionListener {

    private static final long serialVersionUID = 1L;

    private static final Icon ICON_JOB = ImageManager.get()
            .getImageIcon(IconUtils.MODEL_JOB, IconUtils.ICON_SIZE_SMALL);
    private static final Icon ICON_RESULT = ImageManager.get().getImageIcon(IconUtils.MODEL_RESULT,
            IconUtils.ICON_SIZE_SMALL);
    private static final Icon ICON_FILE = ImageManager.get().getImageIcon(IconUtils.FILE_FILE,
            IconUtils.ICON_SIZE_SMALL);

    private final OpenAnalysisJobActionListener _openAnalysisJobActionListener;
    private final FileObject _file;

    public OpenAnalysisJobMenuItem(final FileObject file,
            final OpenAnalysisJobActionListener openAnalysisJobActionListener) {
        super();
        _file = file;
        _openAnalysisJobActionListener = openAnalysisJobActionListener;

        final String title;
        final String filename = file.getName().getBaseName();
        final String jobExtension = FileFilters.ANALYSIS_XML.getExtension();
        final String resultExtension = FileFilters.ANALYSIS_XML.getExtension();

        if (filename.toLowerCase().endsWith(jobExtension)) {
            title = filename.substring(0, filename.length() - jobExtension.length());
            setIcon(ICON_JOB);
        } else if (filename.toLowerCase().endsWith(resultExtension)) {
            title = filename.substring(0, filename.length() - resultExtension.length());
            setIcon(ICON_RESULT);
        } else {
            title = filename;
            setIcon(ICON_FILE);
        }

        setText(title);

        addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        _openAnalysisJobActionListener.openFile(_file);
    }
}
