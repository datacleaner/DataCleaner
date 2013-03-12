/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.actions;

import java.io.InputStream;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.provider.DelegateFileObject;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.user.MonitorConnection;
import org.eobjects.datacleaner.user.UserPreferences;

/**
 * ActionListener for publishing a job to the DC monitor.
 */
public class PublishJobToMonitorActionListener extends PublishFileToMonitorActionListener {

    private final DelegateFileObject _jobFile;

    public PublishJobToMonitorActionListener(DelegateFileObject jobFile, WindowContext windowContext,
            UserPreferences userPreferences) {
        super(windowContext, userPreferences);
        _jobFile = jobFile;
    }

    @Override
    protected String getTransferredFilename() {
        return _jobFile.getName().getBaseName();
    }

    @Override
    protected String getUploadUrl(MonitorConnection monitorConnection) {
        return _jobFile.getName().getURI();
    }

    @Override
    protected InputStream getTransferStream() {
        try {
            return _jobFile.getContent().getInputStream();
        } catch (FileSystemException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    protected long getExpectedSize() {
        try {
            return _jobFile.getContent().getSize();
        } catch (FileSystemException e) {
            throw new IllegalStateException(e);
        }
    }
}
