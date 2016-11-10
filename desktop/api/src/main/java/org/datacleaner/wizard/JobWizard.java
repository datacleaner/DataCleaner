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
package org.datacleaner.wizard;

import javax.swing.Icon;

import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.connection.Datastore;
import org.datacleaner.util.IconUtils;

/**
 * Defines a wizard on the first screen of the commercial editions of
 * DataCleaner
 */
public interface JobWizard {
    static final String BUTTON_NEXT_TEXT = "Next >";
    static final String BUTTON_NEXT_ICON = IconUtils.ACTION_FORWARD;

    boolean isAvailable();

    Icon getIcon();

    String getTitle();

    String getDescription();

    void startWizard(DataCleanerConfiguration configuration, Datastore selectedDatastore,
            JobWizardCallback callback);

    String getCategory();
}
