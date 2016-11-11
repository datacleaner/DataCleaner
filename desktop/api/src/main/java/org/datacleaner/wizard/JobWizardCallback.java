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

import javax.swing.JComponent;

import org.datacleaner.job.builder.AnalysisJobBuilder;

/**
 * Defines a callback for a JobWizard which contains some logic which may be invoked when or when done using a
 * {@link JobWizard}.
 */
public interface JobWizardCallback {

    /**
     * Sets the contents of the wizard panel to the given component, which is typically used within the flow
     * of the wizard for rendering.
     *
     * @param component
     *            {@link JComponent} with content for wizard panel
     */
    void setWizardContent(JComponent component);

    /**
     * Indicates the wizard is finished and passed the analysisJobBuilder which has been built using a
     * {@link JobWizard}.
     *
     * @param analysisJobBuilder
     *            {@link AnalysisJobBuilder} which has been built using the {@link JobWizard}
     */
    void setWizardFinished(AnalysisJobBuilder analysisJobBuilder);
}
