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

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JToolBar;

import org.datacleaner.actions.RenameComponentActionListener;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.panels.ComponentBuilderPresenter;
import org.datacleaner.panels.DCBannerPanel;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.LabelUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.ChangeRequirementButton;
import org.datacleaner.widgets.NeopostToolbarButton;
import org.datacleaner.widgets.visualization.JobGraph;

/**
 * Dialog for configuring components that have been selected through the
 * {@link JobGraph}.
 */
public class ComponentConfigurationDialog extends AbstractDialog {

    private static final long serialVersionUID = 1L;

    private final ComponentBuilderPresenter _presenter;
    private final ComponentBuilder _componentBuilder;

    public ComponentConfigurationDialog(
            ComponentBuilder componentBuilder,
            AnalysisJobBuilder analysisJobBuilder, ComponentBuilderPresenter presenter) {
        // super(null,
        // ImageManager.get().getImage("images/window/banner-logo.png"));
        super(null, getBannerImage(componentBuilder));

        _componentBuilder = componentBuilder;
        _presenter = presenter;
    }

    private static Image getBannerImage(ComponentBuilder componentBuilder) {
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
        return getBannerTitle2(false);
    }

    @Override
    protected String getBannerTitle() {
        return _componentBuilder.getDescriptor().getDisplayName();
    }

    private String getBannerTitle2(boolean onlyIfDifferentThanTitle1) {
        final String title2 = LabelUtils.getLabel(_componentBuilder);
        if (onlyIfDifferentThanTitle1 && getBannerTitle().equals(title2)) {
            return null;
        }
        return title2;
    }

    @Override
    protected DCBannerPanel createBanner(Image bannerImage) {
        final DCBannerPanel banner = new DCBannerPanel(bannerImage, getBannerTitle());
        banner.setTitle2(getBannerTitle2(true));

        final JButton renameButton = new JButton("Rename", ImageManager.get().getImageIcon(IconUtils.ACTION_RENAME,
                IconUtils.ICON_SIZE_MEDIUM));
        renameButton.addActionListener(new RenameComponentActionListener(_componentBuilder) {
            @Override
            protected void onNameChanged() {
                banner.setTitle2(getBannerTitle2(true));
                banner.updateUI();
            }
        });
        
        banner.add(new ChangeRequirementButton(_componentBuilder));
        banner.add(renameButton);

        return banner;
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
