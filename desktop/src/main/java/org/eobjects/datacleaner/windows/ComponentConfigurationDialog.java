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
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JToolBar;

import org.eobjects.analyzer.descriptors.ComponentDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.util.LabelUtils;
import org.eobjects.datacleaner.panels.ComponentJobBuilderPresenter;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.NeopostToolbarButton;
import org.eobjects.datacleaner.widgets.visualization.JobGraph;

/**
 * Dialog for configuring components that have been selected through the
 * {@link JobGraph}.
 */
public class ComponentConfigurationDialog extends AbstractDialog {

    private static final long serialVersionUID = 1L;

    private final String _shortMessage;
    private final ComponentJobBuilderPresenter _presenter;

    public ComponentConfigurationDialog(
            AbstractBeanJobBuilder<? extends ComponentDescriptor<?>, ?, ?> componentBuilder,
            AnalysisJobBuilder analysisJobBuilder, ComponentJobBuilderPresenter presenter) {
        // super(null,
        // ImageManager.get().getImage("images/window/banner-logo.png"));
        super(null, getBannerImage(componentBuilder));

        _shortMessage = LabelUtils.getLabel(componentBuilder);
        _presenter = presenter;
    }

    private static Image getBannerImage(AbstractBeanJobBuilder<? extends ComponentDescriptor<?>, ?, ?> componentBuilder) {
        final ImageIcon descriptorIcon = IconUtils.getDescriptorIcon(componentBuilder.getDescriptor(),
                IconUtils.ICON_SIZE_LARGE);
        return descriptorIcon.getImage();
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

        final JButton closeButton = WidgetFactory.createButton("Close", "images/actions/save.png");
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ComponentConfigurationDialog.this.dispose();
            }
        });

        final JToolBar toolBar = WidgetFactory.createToolBar();
        toolBar.add(new NeopostToolbarButton());
        toolBar.add(WidgetFactory.createToolBarSeparator());
        toolBar.add(closeButton);

        final DCPanel toolBarPanel = new DCPanel(WidgetUtils.BG_COLOR_DARKEST, WidgetUtils.BG_COLOR_DARKEST);
        toolBarPanel.setLayout(new BorderLayout());
        toolBarPanel.add(toolBar, BorderLayout.CENTER);

        final DCPanel panel = new DCPanel(WidgetUtils.BG_COLOR_BRIGHT, WidgetUtils.BG_COLOR_BRIGHT);
        panel.setLayout(new BorderLayout());
        panel.add(configurationComponent, BorderLayout.CENTER);
        panel.add(toolBarPanel, BorderLayout.SOUTH);

        return panel;
    }
}
