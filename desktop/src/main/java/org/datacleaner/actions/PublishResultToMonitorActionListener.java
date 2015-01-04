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
package org.datacleaner.actions;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.swing.JOptionPane;

import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.vfs2.FileObject;
import org.datacleaner.result.AnalysisResult;
import org.datacleaner.result.SimpleAnalysisResult;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.guice.JobFile;
import org.datacleaner.guice.Nullable;
import org.datacleaner.user.MonitorConnection;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.FileFilters;
import org.datacleaner.windows.ResultWindow;
import org.apache.metamodel.util.Ref;

import com.google.common.base.Strings;

/**
 * Action listener invoked when the user clicks the "Publish to dq monitor"
 * button on the {@link ResultWindow}.
 */
public class PublishResultToMonitorActionListener extends PublishFileToMonitorActionListener {

    private final Ref<AnalysisResult> _resultRef;
    private final FileObject _jobFilename;

    private byte[] _bytes;
    private String _resultFilename;

    public PublishResultToMonitorActionListener(WindowContext windowContext, UserPreferences userPreferences,
            Ref<AnalysisResult> resultRef, @Nullable @JobFile FileObject jobFilename) {
        super(windowContext, userPreferences);
        _resultRef = resultRef;
        _jobFilename = jobFilename;
    }

    private byte[] getBytes() {
        if (_bytes == null) {
            final AnalysisResult analysisResult = _resultRef.get();
            _bytes = SerializationUtils.serialize(new SimpleAnalysisResult(analysisResult.getResultMap()));
        }
        return _bytes;
    }

    @Override
    protected boolean doBeforeAction() {
        if (_jobFilename == null) {
            final String jobName = JOptionPane.showInputDialog(null, "Enter the name of a (new or existing) job on the server that this result refers to?", "Job name on server",
                    JOptionPane.QUESTION_MESSAGE);
            if (Strings.isNullOrEmpty(jobName)) {
                return false;
            }
            _resultFilename = jobName;
        } else {
            final String jobExtension = FileFilters.ANALYSIS_XML.getExtension();

            String baseName = _jobFilename.getName().getBaseName();
            if (baseName.endsWith(jobExtension)) {
                baseName = baseName.substring(0, baseName.length() - jobExtension.length());
            }
            _resultFilename = baseName;
        }
        return true;
    }

    @Override
    protected String getTransferredFilename() {
        return _resultFilename;
    }

    @Override
    protected String getUploadUrl(MonitorConnection monitorConnection) {
        final String transferredFilename = getTransferredFilename();
        final String encodedFilename = encodeSpaces(transferredFilename);
        return monitorConnection.getBaseUrl() + "/repository/" + monitorConnection.getTenantId() + "/results/"
                + encodedFilename;
    }

    @Override
    protected long getExpectedSize() {
        return getBytes().length;
    }

    @Override
    protected InputStream getTransferStream() {
        return new ByteArrayInputStream(getBytes());
    }

    @Override
    protected boolean openBrowserWhenDone() {
        return true;
    }
}
