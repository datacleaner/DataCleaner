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

import java.util.Map;

import org.apache.commons.codec.net.URLCodec;
import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.job.ExecutionLogger;
import org.eobjects.datacleaner.monitor.job.JobEngine;
import org.eobjects.datacleaner.monitor.pentaho.jaxb.PentahoJobType;
import org.eobjects.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.springframework.stereotype.Component;

/**
 * A {@link JobEngine} for running Pentaho Data Integration (aka. Kettle) jobs
 * in within DataCleaner.
 */
@Component
public class PentahoJobEngine extends AbstractJobEngine<PentahoJobContext> {

    public static final String EXTENSION = ".pentaho.job.xml";

    public PentahoJobEngine() {
        super(EXTENSION);
    }

    @Override
    public String getJobType() {
        return "PentahoJob";
    }

    @Override
    public void executeJob(TenantContext tenantContext, ExecutionLog execution, ExecutionLogger executionLogger,
            Map<String, String> variables) throws Exception {
        final PentahoJobContext jobContext = getJobContext(tenantContext, execution.getJob());
        final PentahoJobType pentahoJobType = jobContext.getPentahoJobType();

        final URLCodec urlCodec = new URLCodec();
        final String encodedName = urlCodec.encode(pentahoJobType.getTransformationName());
        final String encodedId = urlCodec.encode(pentahoJobType.getTransformationId());

        final String url = "http://" + pentahoJobType.getCarteHostname() + ":" + pentahoJobType.getCartePort()
                + "/kettle/startTrans?xml=y&name=" + encodedName + "&id=" + encodedId;

        throw new UnsupportedOperationException("Not yet implemented, but carte URL is: " + url);
    }

    @Override
    protected PentahoJobContext getJobContext(TenantContext tenantContext, RepositoryFile file) {
        return new PentahoJobContext(file);
    }
}
