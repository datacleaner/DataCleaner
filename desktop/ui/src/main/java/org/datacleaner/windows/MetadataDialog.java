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
package org.datacleaner.windows;

import javax.swing.JComponent;

import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.panels.MetadataPanel;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;

/**
 * A dialog containing metadata about a job and it's data source
 */
public class MetadataDialog extends AbstractDialog {

    private static final long serialVersionUID = 1L;
    
    private final AnalysisJobBuilder _jobBuilder;

    public MetadataDialog(WindowContext windowContext, AnalysisJobBuilder jobBuilder) {
        super(windowContext, ImageManager.get().getImage(IconUtils.MODEL_METADATA));
        _jobBuilder = jobBuilder;
    }

    @Override
    public String getWindowTitle() {
        return "Job metadata";
    }

    @Override
    protected String getBannerTitle() {
        return "Job metadata";
    }

    @Override
    protected int getDialogWidth() {
        return 600;
    }

    @Override
    protected JComponent getDialogContent() {
        return new MetadataPanel(_jobBuilder);
    }

}
