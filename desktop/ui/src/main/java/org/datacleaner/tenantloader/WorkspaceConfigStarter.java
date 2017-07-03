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
package org.datacleaner.tenantloader;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.xml.bind.JAXBException;

import org.datacleaner.user.DataCleanerHome;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.windows.WorkspaceConfigDialog;

/**
 * Class check of workspace configuration and start Workspace dialog.
 */
public class WorkspaceConfigStarter {

    private WorkspaceManager _workspaceManager;

    public WorkspaceConfigStarter() throws JAXBException {
        _workspaceManager = new WorkspaceManager(DataCleanerHome.getAsDataCleanerHomeFolder());
    }

    /**
     * Start workspace dialog or set default workspace.
     * @throws InterruptedException
     */
    public void start() throws InterruptedException {
        if (!_workspaceManager.showDialog()) {
            _workspaceManager.setWorkspaceToRun(_workspaceManager.getDefaultWorkspace());
            DataCleanerHome.reInit();
            return;
        }
        final Object lock = new Object();
        WidgetUtils.invokeSwingAction(() -> {
            final JFrame frame = new JFrame();
            final WorkspaceConfigDialog workspaceConfigDialog = new WorkspaceConfigDialog(frame, _workspaceManager);
            workspaceConfigDialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    try {
                        _workspaceManager.save();
                    } catch (JAXBException ex2) {
                        ex2.printStackTrace();
                    }
                    DataCleanerHome.reInit();
                    synchronized (lock) {
                        lock.notifyAll();
                    }
                    frame.dispose();
                }
            });

        });
        synchronized (lock) {
            lock.wait();
        }
    }
}
