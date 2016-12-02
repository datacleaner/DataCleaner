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
package org.datacleaner.monitor.shared.model;

/**
 * Enumerates the security roles used in the DC monitor
 */
public interface SecurityRoles {

    String VIEWER = "ROLE_VIEWER";
    String JOB_EDITOR = "ROLE_JOB_EDITOR";
    String DASHBOARD_EDITOR = "ROLE_DASHBOARD_EDITOR";
    String SCHEDULE_EDITOR = "ROLE_SCHEDULE_EDITOR";
    String CONFIGURATION_EDITOR = "ROLE_CONFIGURATION_EDITOR";
    String RESULT_EDITOR = "ROLE_RESULT_EDITOR";

    // task-specific privileges
    String TASK_QUERY = "ROLE_TASK_QUERY";
    String TASK_SLAVE_EXECUTOR = "ROLE_SLAVE_EXECUTOR";
    String TASK_ATOMIC_EXECUTOR = "ROLE_ATOMIC_EXECUTOR";

    // super roles
    /**
     * ENGINEER role is for editors of both schedules and jobs
     */
    String ENGINEER = "ROLE_ENGINEER";

    /**
     * ADMIN role is for administrators of a tenant. He can do everything within
     * the boundary of a tenant.
     */
    String ADMIN = "ROLE_ADMIN";

    /**
     * GOD role is for multi-tenant administrators, selected people who can do
     * everything for everyone.
     */
    String GOD = "ROLE_GOD";
}
