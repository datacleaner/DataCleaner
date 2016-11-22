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
package org.datacleaner.monitor.server.security;

import org.datacleaner.monitor.shared.model.SecurityRoles;

/**
 * Defines the interface of a monitor user.
 */
public interface User {

    /**
     * @deprecated use {@link #isAuthenticated()} instead.
     */
    @Deprecated
    boolean isLoggedIn();

    boolean isAuthenticated();

    String getUsername();

    String getTenant();

    boolean hasRole(String role);

    /**
     * Determines whether the user has the {@link SecurityRoles#VIEWER} role.
     *
     * @return whether the user has the {@link SecurityRoles#VIEWER} role.
     */
    boolean isViewer();

    /**
     * Determines whether the user has the {@link SecurityRoles#GOD} super role.
     *
     * @return whether the user has the {@link SecurityRoles#GOD} super role.
     */
    boolean isGod();

    /**
     * Determines whether the user has the {@link SecurityRoles#ADMIN} super
     * role.
     *
     * @return whether the user has the {@link SecurityRoles#ADMIN} super role.
     */
    boolean isAdmin();

    /**
     * Determines whether the user has the {@link SecurityRoles#TASK_QUERY} role
     *
     * @return
     */
    boolean isQueryAllowed();

    /**
     * Determines whether the user has the {@link SecurityRoles#JOB_EDITOR}
     * role.
     *
     * @return whether the user has the {@link SecurityRoles#JOB_EDITOR} role.
     */
    boolean isJobEditor();

    /**
     * Determines whether the user has the
     * {@link SecurityRoles#CONFIGURATION_EDITOR} role.
     *
     * @return
     */
    boolean isConfigurationEditor();

    /**
     * Determines whether the user has the
     * {@link SecurityRoles#DASHBOARD_EDITOR} role.
     *
     * @return whether the user has the {@link SecurityRoles#DASHBOARD_EDITOR}
     *         role.
     */
    boolean isDashboardEditor();

    /**
     * Determines whether the user has the {@link SecurityRoles#SCHEDULE_EDITOR}
     * role.
     *
     * @return whether the user has the {@link SecurityRoles#SCHEDULE_EDITOR}
     *         role.
     */
    boolean isScheduleEditor();

    /**
     * Determines whether the user has the {@link SecurityRoles#ENGINEER} super
     * role.
     *
     * @return whether the user has the {@link SecurityRoles#ENGINEER} super
     *         role.
     */
    boolean isEngineer();
}
