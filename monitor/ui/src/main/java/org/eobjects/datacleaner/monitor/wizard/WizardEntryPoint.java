/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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

package org.eobjects.datacleaner.monitor.wizard;

import org.eobjects.datacleaner.monitor.wizard.JobWizardPanel;

/**
 * The EntryPoint containing a method to launch a Job Wizard
 * 
 */

public class WizardEntryPoint implements com.google.gwt.core.client.EntryPoint {

    /**
     * Method which is called when the module is loaded
     */
    @Override
    public void onModuleLoad() {
        exportStartWizard();
    }

    /**
     * This method gets called by a custom javascript usually embedded in an
     * onclick method of an HTML button
     * 
     * @param tenantName
     * @param panelType
     * @param datastoreName
     * @param wizardDisplayName
     * @param htmlDivNameToShowWizardIn
     */
    public static void startWizard(String tenantName, String panelType, String datastoreName, String wizardDisplayName,
            String htmlDivNameToShowWizardIn) {
        new JobWizardPanel(tenantName, panelType, datastoreName, wizardDisplayName, htmlDivNameToShowWizardIn);
    }

    /**
     * A native Javascript method
     */
    public static native void exportStartWizard() /*-{
                                                   $wnd.startWizard =
                                                   @org.eobjects.datacleaner.monitor.wizard.WizardEntryPoint::startWizard(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;);
                                                   }-*/;

}
