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

import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;

import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.RemoteServerConfiguration;
import org.datacleaner.configuration.RemoteServerState;
import org.datacleaner.configuration.RemoteServerStateListener;
import org.datacleaner.descriptors.RemoteDescriptorProvider;
import org.datacleaner.panels.DataCloudInformationPanel;
import org.datacleaner.panels.RightInformationPanel;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.windows.AbstractWindow;

/**
 * Status Label for DataCloud
 */
public class DataCloudStatusLabel extends JLabel {
    public static final String PANEL_NAME = "DataCloud";

    private RemoteServerConfiguration _remoteServerConfiguration;
    private DataCloudInformationPanel _dataCloudInformationPanel;
    private final RightInformationPanel _rightPanel;

    public DataCloudStatusLabel(RightInformationPanel rightPanel, final DataCleanerConfiguration configuration,
                                final UserPreferences userPreferences, WindowContext windowContext, AbstractWindow owner) {
        super(PANEL_NAME);
        _rightPanel = rightPanel;
        setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
        _remoteServerConfiguration = configuration.getEnvironment().getRemoteServerConfiguration();
        _dataCloudInformationPanel = new DataCloudInformationPanel(rightPanel, configuration, userPreferences, windowContext, owner);
        _rightPanel.addTabToPane(PANEL_NAME, _dataCloudInformationPanel);

        _remoteServerConfiguration.addListener(new RemoteServerStateListenerImpl());

        setIcon(RemoteServerState.State.NOT_CONNECTED);
        _dataCloudInformationPanel
                .setInformationStatus(new RemoteServerState(RemoteServerState.State.NOT_CONNECTED, null, null));

        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onMouseClick();
            }
        });
    }

    private void setIcon(RemoteServerState.State state) {
        switch (state) {
        case NOT_CONNECTED:
            setIcon(ImageManager.get().getImageIcon(IconUtils.CLOUD_GREY, IconUtils.ICON_SIZE_SMALL));
            break;
        case NO_CREDIT:
            setIcon(ImageManager.get().getImageIcon(IconUtils.CLOUD_ORANGE, IconUtils.ICON_SIZE_SMALL));
            break;
        case OK:
            setIcon(ImageManager.get().getImageIcon(IconUtils.CLOUD_GREEN, IconUtils.ICON_SIZE_SMALL));
            break;
        case ERROR:
            setIcon(ImageManager.get().getImageIcon(IconUtils.CLOUD_RED, IconUtils.ICON_SIZE_SMALL));
            break;
        }
    }

    private void onMouseClick(){
        _rightPanel.toggleWindow(PANEL_NAME);
    }

    private class RemoteServerStateListenerImpl implements RemoteServerStateListener{

        @Override
        public void onRemoteServerStateChange(final String remoteServerName, final RemoteServerState state) {
            if(RemoteDescriptorProvider.DATACLOUD_SERVER_NAME.equals(remoteServerName)){
                setIcon(state.getActualState());
                _dataCloudInformationPanel.setInformationStatus(state);
            }
        }
    }
}
