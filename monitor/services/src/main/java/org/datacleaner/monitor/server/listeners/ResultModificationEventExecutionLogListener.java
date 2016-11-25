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
package org.datacleaner.monitor.server.listeners;

import java.io.OutputStream;

import org.apache.metamodel.util.Action;
import org.datacleaner.monitor.configuration.TenantContext;
import org.datacleaner.monitor.configuration.TenantContextFactory;
import org.datacleaner.monitor.events.ResultModificationEvent;
import org.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.datacleaner.monitor.server.jaxb.JaxbExecutionLogReader;
import org.datacleaner.monitor.server.jaxb.JaxbExecutionLogWriter;
import org.datacleaner.monitor.shared.model.JobIdentifier;
import org.datacleaner.monitor.shared.model.TenantIdentifier;
import org.datacleaner.repository.RepositoryFile;
import org.datacleaner.repository.RepositoryFolder;
import org.datacleaner.util.FileFilters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class ResultModificationEventExecutionLogListener implements ApplicationListener<ResultModificationEvent> {

    private static final Logger logger = LoggerFactory.getLogger(ResultModificationEventExecutionLogListener.class);

    private final TenantContextFactory _contextFactory;

    @Autowired
    public ResultModificationEventExecutionLogListener(final TenantContextFactory contextFactory) {
        _contextFactory = contextFactory;
    }

    @Override
    public void onApplicationEvent(final ResultModificationEvent event) {
        final String tenant = event.getTenant();
        final TenantContext context = _contextFactory.getContext(tenant);

        final String oldFilename = event.getOldFilename();
        final String resultExtension = FileFilters.ANALYSIS_RESULT_SER.getExtension();
        final String executionLogExtension = FileFilters.ANALYSIS_EXECUTION_LOG_XML.getExtension();

        final String oldExecutionLogFilename = oldFilename.replace(resultExtension, executionLogExtension);

        final RepositoryFolder resultFolder = context.getResultFolder();
        final RepositoryFile oldFile = resultFolder.getFile(oldExecutionLogFilename);
        if (oldFile == null) {
            logger.warn("Could not find execution log for (old) result filename: {}. Skipping execution log update.",
                    oldFilename);
            return;
        }

        final String resultId = event.getNewFilename().replace(resultExtension, "");

        final JobIdentifier jobIdentifier = JobIdentifier.fromResultId(resultId);

        final ExecutionLog executionLog = oldFile.readFile(in -> {
            final JaxbExecutionLogReader reader = new JaxbExecutionLogReader();
            return reader.read(in, jobIdentifier, new TenantIdentifier(tenant));
        });

        executionLog.setResultId(resultId);
        final String newFilename = resultId + executionLogExtension;

        final JaxbExecutionLogWriter writer = new JaxbExecutionLogWriter();
        final RepositoryFile newFile = resultFolder.getFile(newFilename);
        final Action<OutputStream> writeAction = out -> writer.write(executionLog, out);
        if (newFile == null) {
            resultFolder.createFile(newFilename, writeAction);
        } else {
            newFile.writeFile(writeAction);
        }

        oldFile.delete();
    }
}
