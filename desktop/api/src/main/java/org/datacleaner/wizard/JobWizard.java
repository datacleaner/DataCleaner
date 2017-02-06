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
import org.datacleaner.metadata.ColumnMeaningCollection;
import org.datacleaner.metadata.DefaultColumnMeaningCollection;
import org.datacleaner.util.IconUtils;

/**
 * Defines a wizard which is shown on the DataCleaner welcome screen. The wizard can be used to create a job,
 * which can either be opened in DataCleaner or be directly run.
 */
public interface JobWizard {
    /**
     * Text for next button in wizard.
     */
    String BUTTON_NEXT_TEXT = "Next >";

    /**
     * Icon for next button in wizard.
     */
    String BUTTON_NEXT_ICON = IconUtils.ACTION_FORWARD;

    /**
     * Gets all possible column meanings for mapping phase.
     * @return
     */
    default ColumnMeaningCollection getAvailableColumnMeanings() {
        return new DefaultColumnMeaningCollection();
    }

    /**
     * Checks if all necessary components to use the wizard are available.
     *
     * @return <code>true</code> if the wizard can be used
     */
    boolean isAvailable();

    /**
     * Gets the icon which is shown for the wizard on the welcome screen, or <code>null</code> if none is
     * defined for the wizard.
     *
     * @return an {@link Icon}
     */
    Icon getIcon();

    /**
     * Gets the title for the wizard.
     *
     * @return Title for the wizard
     */
    String getTitle();

    /**
     * Gets the description of the wizard. Describes what the wizard can be used for.
     *
     * @return description of the wizard
     */
    String getDescription();

    /**
     * Starts wizard when it is activated from the welcome screen, after a Datastore has been selected to use
     * with it.
     *
     * @param configuration
     *            {@link DataCleanerConfiguration} passed by DataCleaner
     * @param selectedDatastore
     *            {@link Datastore} which is selected to use the wizard with
     * @param callback
     *            {@link JobWizardCallback} which contains some signaling logic
     */
    void startWizard(DataCleanerConfiguration configuration, Datastore selectedDatastore, JobWizardCallback callback);

    /**
     * Gets the category the JobWizard belongs to.
     *
     * @return A String representing the category
     */
    String getCategory();
}
