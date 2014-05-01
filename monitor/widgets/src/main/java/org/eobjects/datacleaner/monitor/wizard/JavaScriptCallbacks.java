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

import org.eobjects.datacleaner.monitor.shared.ClientConfig;
import org.eobjects.datacleaner.monitor.shared.DictionaryClientConfig;
import org.eobjects.datacleaner.monitor.shared.WizardService;
import org.eobjects.datacleaner.monitor.shared.WizardServiceAsync;
import org.eobjects.datacleaner.monitor.shared.model.DatastoreIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.WizardIdentifier;

import com.google.gwt.core.client.GWT;

public final class JavaScriptCallbacks {

    /**
     * Called when a wizard is finished
     */
    public static native void onWizardFinished() /*-{
                                                        if ($wnd.datacleaner && $wnd.datacleaner.onWizardFinished) {
                                                            $wnd.datacleaner.onWizardFinished();
                                                        }
                                                    }-*/;

    /**
     * Called when a wizard is closed
     */
    public static native void onWizardCancelled() /*-{
                                                                
                                                                if ($wnd.datacleaner && $wnd.datacleaner.onWizardCancelled) {
                                                                    $wnd.datacleaner.onWizardCancelled();
                                                                }
                                                            }-*/;

    /**
     * Exposes the DataCleaner wizard JS API.
     */
    public static void exposeApi() {
        exportStartJobWizard();
    }

    /**
     * Exports a JS method:
     * 
     * startJobWizard(datastoreName, wizardName, htmlDivId)
     */
    public static native void exportStartJobWizard() /*-{
                                                     if (!$wnd.datacleaner) {
                                                           $wnd.datacleaner = {};
                                                     }
                                                     $wnd.datacleaner.startJobWizard = @org.eobjects.datacleaner.monitor.wizard.JavaScriptCallbacks::startJobWizard(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;);   
                                                     }-*/;

    /**
     * This method gets called by a custom javascript usually embedded in an
     * onclick method of an HTML button
     * 
     * @param panelType
     * @param datastoreName
     * @param wizardDisplayName
     * @param htmlDivNameToShowWizardIn
     */
    public static void startWizard(String datastoreName, String wizardDisplayName, String htmlDivId) {

        final ClientConfig clientConfig = new DictionaryClientConfig();

        final WizardPanel wizardPanel = WizardPanelFactory.createWizardPanel(htmlDivId);
        final WizardIdentifier wizardIdentifier = new WizardIdentifier(wizardDisplayName);
        final WizardServiceAsync wizardService = GWT.create(WizardService.class);
        final TenantIdentifier tenant = clientConfig.getTenant();
        final DatastoreIdentifier datastoreIdentifier = new DatastoreIdentifier(datastoreName);

        final JobWizardController controller = new JobWizardController(wizardPanel, tenant, wizardIdentifier,
                datastoreIdentifier, wizardService);
        controller.startWizard();
    }
}