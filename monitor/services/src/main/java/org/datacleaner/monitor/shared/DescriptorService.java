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
package org.datacleaner.monitor.shared;

import java.util.Collection;

import javax.annotation.security.RolesAllowed;

import org.datacleaner.monitor.shared.model.DCSecurityException;
import org.datacleaner.monitor.shared.model.JobIdentifier;
import org.datacleaner.monitor.shared.model.JobMetrics;
import org.datacleaner.monitor.shared.model.MetricIdentifier;
import org.datacleaner.monitor.shared.model.SecurityRoles;
import org.datacleaner.monitor.shared.model.TenantIdentifier;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * Service interface for operations pertaining to descriptors and
 * meta-information about jobs.
 */
@RemoteServiceRelativePath("../gwtrpc/descriptorService")
public interface DescriptorService extends RemoteService {

    /**
     * Gets all available metrics for a job
     *
     * @param tenant
     * @param job
     * @return
     */
    @RolesAllowed({ SecurityRoles.DASHBOARD_EDITOR, SecurityRoles.SCHEDULE_EDITOR })
    JobMetrics getJobMetrics(TenantIdentifier tenant, JobIdentifier job)
            throws DCSecurityException, DescriptorNotFoundException;

    /**
     * Gets suggestions for parameter values of a particular metric
     *
     * @param tenant
     * @param metric
     * @return
     */
    @RolesAllowed({ SecurityRoles.DASHBOARD_EDITOR, SecurityRoles.SCHEDULE_EDITOR })
    Collection<String> getMetricParameterSuggestions(TenantIdentifier tenant, JobIdentifier jobIdentifier,
            MetricIdentifier metric) throws DCSecurityException;

}
