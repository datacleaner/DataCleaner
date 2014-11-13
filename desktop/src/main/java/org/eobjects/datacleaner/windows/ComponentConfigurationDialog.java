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

import javax.swing.JComponent;

import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.util.LabelUtils;
import org.eobjects.datacleaner.panels.ComponentJobBuilderPresenter;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.visualization.VisualizeJobGraph;

/**
 * Dialog for configuring components that have been selected through the
 * {@link VisualizeJobGraph}.
 */
public class ComponentConfigurationDialog extends AbstractDialog {

    private static final long serialVersionUID = 1L;

    private final String _shortMessage;
    private final ComponentJobBuilderPresenter _presenter;

    public ComponentConfigurationDialog(AbstractBeanJobBuilder<?, ?, ?> componentBuilder,
            AnalysisJobBuilder analysisJobBuilder, ComponentJobBuilderPresenter presenter) {
        super(null, ImageManager.get().getImage("images/window/banner-logo.png"));

        _shortMessage = LabelUtils.getLabel(componentBuilder);
        _presenter = presenter;
        setModal(true);
    }

    @Override
    protected boolean isWindowResizable() {
        return true;
    }

    @Override
    public String getWindowTitle() {
        return _shortMessage;
    }

    @Override
    protected String getBannerTitle() {
        return _shortMessage;
    }

    @Override
    protected int getDialogWidth() {
        return 600;
    }

    @Override
    protected JComponent getDialogContent() {
        final JComponent configurationComponent = _presenter.createJComponent();
        final DCPanel panel = new DCPanel(WidgetUtils.BG_COLOR_BRIGHT, WidgetUtils.BG_COLOR_BRIGHT);
        panel.setLayout(new BorderLayout());
        panel.add(configurationComponent, BorderLayout.CENTER);
        // TODO: Have a close button or react to window closed
        return panel;
    }
}
