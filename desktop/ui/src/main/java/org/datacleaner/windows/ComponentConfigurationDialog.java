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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import org.datacleaner.actions.ComponentReferenceDocumentationActionListener;
import org.datacleaner.actions.RenameComponentActionListener;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.job.builder.ComponentRemovalListener;
import org.datacleaner.panels.ComponentBuilderPresenter;
import org.datacleaner.panels.DCBannerPanel;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.LabelUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.Alignment;
import org.datacleaner.widgets.ChangeRequirementButton;
import org.datacleaner.widgets.ChangeRequirementMenu;
import org.datacleaner.widgets.visualization.JobGraph;

/**
 * Dialog for configuring components that have been selected through the
 * {@link JobGraph}.
 */
public class ComponentConfigurationDialog extends AbstractDialog implements ComponentRemovalListener<ComponentBuilder> {

    private static final long serialVersionUID = 1L;

    private final ComponentBuilderPresenter _presenter;
    private final ComponentBuilder _componentBuilder;

    public ComponentConfigurationDialog(WindowContext windowContext, ComponentBuilder componentBuilder,
            ComponentBuilderPresenter presenter) {
        // super(null,
        // ImageManager.get().getImage("images/window/banner-logo.png"));
        super(windowContext, getBannerImage(componentBuilder));

        _componentBuilder = componentBuilder;
        _componentBuilder.addRemovalListener(this);
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

        final JButton renameButton = WidgetFactory.createDefaultButton("Rename", IconUtils.ACTION_RENAME);
        renameButton.addActionListener(new RenameComponentActionListener(_componentBuilder) {
            @Override
            protected void onNameChanged() {
                banner.setTitle2(getBannerTitle2(true));
                banner.updateUI();
            }
        });

        final JButton documentationButton = WidgetFactory.createDefaultButton("Documentation",
                IconUtils.MENU_DOCUMENTATION);
        documentationButton.addActionListener(new ComponentReferenceDocumentationActionListener(_componentBuilder
                .getAnalysisJobBuilder().getConfiguration(), _componentBuilder.getDescriptor()));

        banner.add(documentationButton);
        if (ChangeRequirementMenu.isRelevant(_componentBuilder)) {
            banner.add(new ChangeRequirementButton(_componentBuilder));
        }
        banner.add(renameButton);

        return banner;
    }

    @Override
    protected int getDialogWidth() {
        return 750;
    }

    @Override
    protected int getDialogHeightBuffer() {
        return 50;
    }

    @Override
    protected JComponent getDialogContent() {
        final JComponent configurationComponent = _presenter.createJComponent();

        final JButton closeButton = WidgetFactory.createPrimaryButton("Close", IconUtils.ACTION_CLOSE_BRIGHT);
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ComponentConfigurationDialog.this.dispose();
            }
        });

        final DCPanel panel = new DCPanel(WidgetUtils.COLOR_WELL_BACKGROUND);
        panel.setLayout(new BorderLayout());
        panel.add(configurationComponent, BorderLayout.CENTER);
        panel.add(DCPanel.flow(Alignment.CENTER, closeButton), BorderLayout.SOUTH);
        panel.setPreferredSize(700, 500);
        return panel;
    }

    @Override
    public void onRemove(ComponentBuilder componentBuilder) {
        close();
    }
}
