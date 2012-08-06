/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.monitor.scheduling.model;

/**
 * Represents the severity of an {@link AlertDefinition}. Such severity is used
 * to categorize and determine appropriate actions in case an alert is raised.
 * 
 * The available severities are (in order of increasing severity):
 * 
 * <ul>
 * <li>INTELLIGENCE - for mostly informational and reporting oriented alerts.
 * This is the least severe type of alert.</li>
 * <li>SURVEILLANCE - this severity is used for alerts that monitor metrics that
 * are under surveillance because a data stewards suspects that the metric might
 * fluctuate or be going in the wrong direction.</li>
 * <li>WARNING - Alerts that are considered dangerous to the data quality.
 * Warnings will typically prompt the data steward to take action.</li>
 * <li>FATAL - Alerts that are raised in cases of fatal data issues in the
 * monitored system</li>
 * </ul>
 * 
 */
public enum AlertSeverity {

    INTELLIGENCE, SURVEILLANCE, WARNING, FATAL
}
