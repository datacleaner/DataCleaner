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
package org.datacleaner.monitor.server.job;

import org.datacleaner.monitor.configuration.TenantContext;
import org.datacleaner.monitor.job.ExecutionLogger;

/**
 * Simple implementation of {@link CustomJobCallback}.
 */
final class CustomJobCallbackImpl implements CustomJobCallback {

    private final ExecutionLogger _executionLogger;
    private final TenantContext _tenantContext;

    public CustomJobCallbackImpl(TenantContext tenantContext, ExecutionLogger executionLogger) {
        _tenantContext = tenantContext;
        _executionLogger = executionLogger;
    }
    
    @Override
    public TenantContext getTenantContext() {
        return _tenantContext;
    }

    @Override
    public void log(String message) {
        _executionLogger.log(message);
        _executionLogger.flushLog();
    }

    @Override
    public void log(String message, Throwable throwable) {
        _executionLogger.log(message, throwable);
        _executionLogger.flushLog();
    }

}
