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

import javax.xml.bind.JAXBException;

import org.datacleaner.cli.CliArguments;
import org.datacleaner.user.DataCleanerHome;
import org.datacleaner.windows.WorkspaceConfigDialog;

/**
 * Class check of workspace configuration and start Workspace dialog.
 */
public class WorkspaceConfigStarter {

    private WorkspaceManager _workspaceManager;
    private CliArguments cliArguments;

    public WorkspaceConfigStarter(CliArguments cliArguments) throws JAXBException {
        _workspaceManager = new WorkspaceManager();
        this.cliArguments = cliArguments;
    }

    /**
     * Start workspace dialog or set default workspace.
     * @throws InterruptedException
     */
    public boolean start() throws InterruptedException {
        // If "Don't show again" was chosen in the past
        if (!_workspaceManager.showDialog() && !cliArguments.isWorkspaceSelection()) {
            _workspaceManager.setWorkspaceToRun(_workspaceManager.getDefaultWorkspace());
            DataCleanerHome.reInit();
            return true;
        }
        // Show the "workspace chooser" dialog
        final WorkspaceConfigDialog workspaceConfigDialog = new WorkspaceConfigDialog(null, _workspaceManager);
        if(workspaceConfigDialog.isShouldStart()) {
            try {
                _workspaceManager.save();
                DataCleanerHome.reInit();
                return true;
            } catch (JAXBException ex2) {
                throw new RuntimeException(ex2);
            }
        } else {
            return false;
        }
    }
}
