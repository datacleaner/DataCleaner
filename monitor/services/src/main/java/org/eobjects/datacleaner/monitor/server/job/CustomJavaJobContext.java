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
package org.eobjects.datacleaner.monitor.server.job;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;

import org.eobjects.datacleaner.monitor.job.JobContext;
import org.eobjects.datacleaner.monitor.job.XmlJobContext;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.eobjects.metamodel.util.Action;
import org.eobjects.metamodel.util.FileHelper;

/**
 * {@link JobContext} for custom Java jobs.
 */
public class CustomJavaJobContext implements XmlJobContext {

    private final RepositoryFile _file;

    public CustomJavaJobContext(RepositoryFile file) {
        _file = file;
    }

    @Override
    public String getName() {
        final int extensionLength = CustomJavaJobEngine.EXTENSION.length();
        final String filename = _file.getName();
        return filename.substring(0, filename.length() - extensionLength);
    }

    @Override
    public RepositoryFile getJobFile() {
        return _file;
    }

    @Override
    public String getGroupName() {
        return "Custom Java jobs";
    }

    @Override
    public Map<String, String> getVariables() {
        return Collections.emptyMap();
    }

    @Override
    public void toXml(final OutputStream out) {
        _file.readFile(new Action<InputStream>() {
            @Override
            public void run(InputStream in) throws Exception {
                FileHelper.copy(in, out);
            }
        });
    }

}
