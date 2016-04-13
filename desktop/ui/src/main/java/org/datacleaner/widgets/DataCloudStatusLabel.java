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

import org.datacleaner.configuration.RemoteServerConfiguration;
import org.datacleaner.configuration.RemoteServerState;
import org.datacleaner.configuration.RemoteServerStateListener;
import org.datacleaner.descriptors.RemoteDescriptorProvider;
import org.datacleaner.panels.DCGlassPane;
import org.datacleaner.panels.DataCloudInformationPanel;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.WidgetUtils;

/**
 * Status Label for DataCloud
 */
public class DataCloudStatusLabel extends JLabel {
    private RemoteServerConfiguration _remoteServerConfiguration;
    private DataCloudInformationPanel _dataCloudInformationPanel;

    public DataCloudStatusLabel(final RemoteServerConfiguration remoteServerConfiguration, DCGlassPane glassPane) {
        super("DataCloud");
        setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
        _remoteServerConfiguration = remoteServerConfiguration;
        _dataCloudInformationPanel = new DataCloudInformationPanel(glassPane);

        _remoteServerConfiguration.addListener(new RemoteServerStateListenerImpl());

        setIcon(RemoteServerState.State.UNKNOWN);
        _dataCloudInformationPanel.setInformationStatus(new RemoteServerState(RemoteServerState.State.UNKNOWN, null, null));

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
        case UNKNOWN:
            setIcon(ImageManager.get().getImageIcon("images/datacloud/cloudNotUsed.png", IconUtils.ICON_SIZE_SMALL));
            break;
        case NO_CREDIT:
            setIcon(ImageManager.get().getImageIcon("images/datacloud/cloudNoCredit.png", IconUtils.ICON_SIZE_SMALL));
            break;
        case OK:
            setIcon(ImageManager.get().getImageIcon("images/datacloud/cloudOk.png", IconUtils.ICON_SIZE_SMALL));
            break;
        case ERROR:
            setIcon(ImageManager.get().getImageIcon("images/datacloud/cloudError.png", IconUtils.ICON_SIZE_SMALL));
            break;
        }
    }

    private void onMouseClick(){
        if (_dataCloudInformationPanel.isVisible()) {
            _dataCloudInformationPanel.moveOut(0);
        } else {
            _dataCloudInformationPanel.moveIn(0);
        }
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
