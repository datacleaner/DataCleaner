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
        Object lock = new Object();
        WidgetUtils.invokeSwingAction(() -> {
            JFrame frame = new JFrame();
            WorkspaceConfigDialog workspaceConfigDialog = new WorkspaceConfigDialog(frame, _workspaceManager);
            workspaceConfigDialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    try {
                        _workspaceManager.save();
                    } catch (JAXBException e1) {
                        e1.printStackTrace();
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
