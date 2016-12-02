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
package org.datacleaner.monitor.pentaho;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.metamodel.util.FileHelper;
import org.datacleaner.descriptors.MetricDescriptor;
import org.datacleaner.descriptors.PlaceholderComponentJob;
import org.datacleaner.monitor.configuration.TenantContext;
import org.datacleaner.monitor.job.MetricJobContext;
import org.datacleaner.monitor.job.XmlJobContext;
import org.datacleaner.monitor.pentaho.jaxb.PentahoJobType;
import org.datacleaner.monitor.server.MetricValueUtils;
import org.datacleaner.monitor.shared.model.JobIdentifier;
import org.datacleaner.monitor.shared.model.JobMetrics;
import org.datacleaner.monitor.shared.model.MetricGroup;
import org.datacleaner.repository.RepositoryFile;

/**
 * Job context object for Pentaho jobs
 */
public class PentahoJobContext implements XmlJobContext, MetricJobContext {

    private final RepositoryFile _file;
    private final PentahoJobEngine _engine;
    private final TenantContext _tenantContext;

    public PentahoJobContext(final TenantContext tenantContext, final PentahoJobEngine engine,
            final RepositoryFile file) {
        _tenantContext = tenantContext;
        _engine = engine;
        _file = file;
    }

    @Override
    public TenantContext getTenantContext() {
        return _tenantContext;
    }

    @Override
    public PentahoJobEngine getJobEngine() {
        return _engine;
    }

    @Override
    public String getName() {
        final int extensionLength = PentahoJobEngine.EXTENSION.length();
        final String filename = _file.getName();
        return filename.substring(0, filename.length() - extensionLength);
    }

    @Override
    public RepositoryFile getJobFile() {
        return _file;
    }

    @Override
    public String getGroupName() {
        return getPentahoJobType().getGroupName();
    }

    public PentahoJobType getPentahoJobType() {
        return _file.readFile(in -> {
            final JaxbPentahoJobTypeAdaptor adaptor = new JaxbPentahoJobTypeAdaptor();
            return adaptor.unmarshal(in);
        });
    }

    @Override
    public Map<String, String> getVariables() {
        return Collections.emptyMap();
    }

    @Override
    public void toXml(final OutputStream out) {
        _file.readFile(in -> {
            FileHelper.copy(in, out);
        });
    }

    @Override
    public JobMetrics getJobMetrics() {
        @SuppressWarnings({ "unchecked", "rawtypes" }) final PlaceholderComponentJob<?> componentJob =
                new PlaceholderComponentJob(getName(), PentahoJobResult.class, PentahoJobResult.class);
        final Set<MetricDescriptor> metricDescriptors = componentJob.getResultMetrics();

        final MetricValueUtils utils = new MetricValueUtils();

        final MetricGroup metricGroup = utils.getMetricGroup(this, componentJob, metricDescriptors);
        final List<MetricGroup> metricGroups = new ArrayList<>();
        metricGroups.add(metricGroup);

        final JobMetrics metrics = new JobMetrics();
        metrics.setMetricGroups(metricGroups);
        metrics.setJob(new JobIdentifier(getName()));
        return metrics;
    }

    @Override
    public Map<String, String> getMetadataProperties() {
        return Collections.emptyMap();
    }
}
