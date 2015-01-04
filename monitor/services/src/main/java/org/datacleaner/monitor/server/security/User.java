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
package org.eobjects.datacleaner.monitor.server.security;

import org.eobjects.datacleaner.monitor.shared.model.SecurityRoles;

/**
 * Defines the interface of a monitor user.
 */
public interface User {

    public boolean isLoggedIn();

    public String getUsername();

    public String getTenant();

    public boolean hasRole(String role);

    /**
     * Determines whether the user has the {@link SecurityRoles#VIEWER} role.
     * 
     * @return whether the user has the {@link SecurityRoles#VIEWER} role.
     */
    public boolean isViewer();

    /**
     * Determines whether the user has the {@link SecurityRoles#GOD} super role.
     * 
     * @return whether the user has the {@link SecurityRoles#GOD} super role.
     */
    public boolean isGod();

    /**
     * Determines whether the user has the {@link SecurityRoles#ADMIN} super
     * role.
     * 
     * @return whether the user has the {@link SecurityRoles#ADMIN} super role.
     */
    public boolean isAdmin();

    /**
     * Determines whether the user has the {@link SecurityRoles#TASK_QUERY} role
     * 
     * @return
     */
    public boolean isQueryAllowed();

    /**
     * Determines whether the user has the {@link SecurityRoles#JOB_EDITOR}
     * role.
     * 
     * @return whether the user has the {@link SecurityRoles#JOB_EDITOR} role.
     */
    public boolean isJobEditor();

    /**
     * Determines whether the user has the
     * {@link SecurityRoles#CONFIGURATION_EDITOR} role.
     * 
     * @return
     */
    public boolean isConfigurationEditor();

    /**
     * Determines whether the user has the
     * {@link SecurityRoles#DASHBOARD_EDITOR} role.
     * 
     * @return whether the user has the {@link SecurityRoles#DASHBOARD_EDITOR}
     *         role.
     */
    public boolean isDashboardEditor();

    /**
     * Determines whether the user has the {@link SecurityRoles#SCHEDULE_EDITOR}
     * role.
     * 
     * @return whether the user has the {@link SecurityRoles#SCHEDULE_EDITOR}
     *         role.
     */
    public boolean isScheduleEditor();

    /**
     * Determines whether the user has the {@link SecurityRoles#ENGINEER} super
     * role.
     * 
     * @return whether the user has the {@link SecurityRoles#ENGINEER} super
     *         role.
     */
    public boolean isEngineer();
}
