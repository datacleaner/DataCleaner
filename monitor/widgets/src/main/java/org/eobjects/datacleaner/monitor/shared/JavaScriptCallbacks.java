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
package org.eobjects.datacleaner.monitor.shared;

import org.eobjects.datacleaner.monitor.shared.ClientConfig;
import org.eobjects.datacleaner.monitor.shared.DictionaryClientConfig;
import org.eobjects.datacleaner.monitor.shared.WizardService;
import org.eobjects.datacleaner.monitor.shared.WizardServiceAsync;
import org.eobjects.datacleaner.monitor.shared.model.DatastoreIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.WizardIdentifier;
import org.eobjects.datacleaner.monitor.wizard.DatastoreWizardController;
import org.eobjects.datacleaner.monitor.wizard.JobWizardController;
import org.eobjects.datacleaner.monitor.wizard.WizardPanel;
import org.eobjects.datacleaner.monitor.wizard.WizardPanelFactory;

import com.google.gwt.core.client.GWT;

public final class JavaScriptCallbacks {
	
	/**
	 * This method (if present on the host page), should return the name of the property 
	 * which should be used to group the jobs on the scheduling page. All those jobs
	 * which have this name as a key in their <property> tag (in the <job-metadata> tag present
	 * in the job metadata), will be shown grouped by the corresponding value present in 
	 * this <property> tag.
	 * 
	 * @return
	 */
    public static native String getJobGroupingCategory() /*-{
                                                          if ($wnd.datacleaner && $wnd.datacleaner.getJobGroupingCategory) {
                                                              var v = $wnd.datacleaner.getJobGroupingCategory();
                                                              if ((typeof v) == 'string') {
                                                                  return v;
                                                              }
                                                          }
                                                          return null;
                                                          }-*/;

    /**
     * Called when the user navigates in a wizard to the next step.
     * 
     * @param wizardDisplayName
     * @param stepIndex
     *            the index of the new active step
     * @param steps
     *            the total number of steps in the wizard given it's current
     *            state.
     * @return
     */
    public static native boolean onWizardProgress(String wizardDisplayName, int stepIndex, int steps) /*-{
                                                                                                      if ($wnd.datacleaner && $wnd.datacleaner.onWizardProgress) {
                                                                                                          var v = $wnd.datacleaner.onWizardProgress(wizardDisplayName, stepIndex, steps);
                                                                                                          if ((typeof v) == 'boolean') {
                                                                                                              return v;
                                                                                                          }
                                                                                                          return true;
                                                                                                      }
                                                                                                      return false;
                                                                                                      }-*/;

    /**
     * Called when the wizard is finished and the last screen is shown.
     * 
     * @param wizardDisplayName
     * @param resultEntityName
     * 
     * @return whether or not a callback was invoked
     */
    public static native boolean onWizardFinished(String wizardDisplayName, String resultEntityName) /*-{
                                                                                                     if ($wnd.datacleaner && $wnd.datacleaner.onWizardFinished) {
                                                                                                         var v = $wnd.datacleaner.onWizardFinished(wizardDisplayName, resultEntityName);
                                                                                                         if ((typeof v) == 'boolean') {
                                                                                                             return v;
                                                                                                         }
                                                                                                         return true;
                                                                                                     }
                                                                                                     return false;
                                                                                                     }-*/;

    /**
     * Called when a wizard is finished and the user clicks a button to close
     * it.
     * 
     * @param wizardDisplayName
     *            the name of the wizard that finished
     * @param wizardResultName
     *            the name of the job or datastore that was built using a wizard
     * 
     * @return whether or not a callback was invoked
     */
    public static native boolean onWizardPanelClosing(String wizardDisplayName, String wizardResultName) /*-{
                                                                                                         if ($wnd.datacleaner && $wnd.datacleaner.onWizardPanelClosing) {
                                                                                                             var v = $wnd.datacleaner.onWizardPanelClosing(wizardDisplayName, wizardResultName);
                                                                                                             if ((typeof v) == 'boolean') {
                                                                                                                 return v;
                                                                                                             }
                                                                                                             return true;
                                                                                                         }
                                                                                                         return false;
                                                                                                         }-*/;

    /**
     * Called when a wizard is closed/cancelled before finishing it.
     * 
     * @param wizardDisplayName
     *            the name of the wizard that was cancelled
     * 
     * @return whether or not a callback was invoked
     */
    public static native boolean onWizardCancelled(String wizardDisplayName) /*-{
                                                                             
                                                                             if ($wnd.datacleaner && $wnd.datacleaner.onWizardCancelled) {
                                                    						 	var v = $wnd.datacleaner.onWizardCancelled(wizardDisplayName);
                                                                                if ((typeof v) == 'boolean') {
                                                                                	return v;
                                                                                }
                                                                                return true;
                                                                             }
                                                                             return false;
                                                                             }-*/;

    /**
     * Called when the user clicks a button to close the execution status panel.
     * 
     * @return whether or not a callback was invoked
     */
    public static native boolean onExecutionStatusPanelClosing() /*-{
                                                                     
                                                                     if ($wnd.datacleaner && $wnd.datacleaner.onExecutionStatusPanelClosing) {
                                                                         var v = $wnd.datacleaner.onExecutionStatusPanelClosing();
                                                                         if ((typeof v) == 'boolean') {
                                                                             return v;
                                                                         }
                                                                         return true;
                                                                     }
                                                                     return false;
                                                                     
                                                                 }-*/;
    
    /**
     * Called when the DataCleaner API has been initialized
     * 
     * @return
     */
    public static native boolean onApiInitialized() /*-{
                                                    if ($wnd.datacleaner && $wnd.datacleaner.onApiInitialized) {
                                                        var v = $wnd.datacleaner.onApiInitialized();
                                                        if ((typeof v) == 'boolean') {
                                                           return v;
                                                        }
                                                        return true;
                                                    }
                                                    return false;
                                                    }-*/;

    /**
     * Native method to call Javascript onError function, in case onError method
     * is not found on the page this method returns false.
     * 
     * @param message
     * @param userFeedback
     *            a boolean that is true if the error relates to the user's
     *            input or choices. Or false if the error is unexpected or
     *            produced by the system/code.
     * @return boolean
     */
    public static native boolean onError(String message, boolean userFeedback)/*-{
                                                                              if ($wnd.datacleaner && (typeof $wnd.datacleaner.onError == 'function')){
                                                                                  var v = $wnd.datacleaner.onError(message, userFeedback);
                                                                                  if ((typeof v) == 'boolean') {
                                                                                      return v;
                                                                                  }
                                                                                  return true;
                                                                              }
                                                                              return false;
                                                                              }-*/;

    /**
     * Exposes the DataCleaner wizard JS API.
     */
    public static void exposeApi() {
        exportStartJobWizard();
        exportStartDatastoreWizard();
        onApiInitialized();
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
                                                     $wnd.datacleaner.startJobWizard = @org.eobjects.datacleaner.monitor.shared.JavaScriptCallbacks::startJobWizard(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;);   
                                                     }-*/;

    /**
     * Exports a JS method:
     * 
     * startJobWizard(datastoreName, wizardName, htmlDivId)
     */
    public static native void exportStartDatastoreWizard() /*-{
                                                           if (!$wnd.datacleaner) {
                                                               $wnd.datacleaner = {};
                                                           }
                                                           $wnd.datacleaner.startDatastoreWizard = @org.eobjects.datacleaner.monitor.shared.JavaScriptCallbacks::startDatastoreWizard(Ljava/lang/String;Ljava/lang/String;);   
                                                           }-*/;

    /**
     * Starts a job wizard based on parameters given from a native JS call.
     * 
     * @param datastoreName
     * @param wizardDisplayName
     * @param htmlDivId
     */
    public static void startJobWizard(final String datastoreName, final String wizardDisplayName, final String htmlDivId) {
        GWT.log("JavaScriptCallbacks.startJobWizard(" + datastoreName + "," + wizardDisplayName + "," + htmlDivId + ")");

        final ClientConfig clientConfig = new DictionaryClientConfig();

        final WizardIdentifier wizardIdentifier = getWizardIdentifier(wizardDisplayName);

        final DatastoreIdentifier datastoreIdentifier;
        if (datastoreName == null) {
            datastoreIdentifier = null;
        } else {
            datastoreIdentifier = new DatastoreIdentifier(datastoreName);
        }

        final WizardPanel wizardPanel = WizardPanelFactory.createWizardPanel(htmlDivId);
        final WizardServiceAsync wizardService = GWT.create(WizardService.class);
        final TenantIdentifier tenant = clientConfig.getTenant();

        final JobWizardController controller = new JobWizardController(wizardPanel, tenant, wizardIdentifier,
                datastoreIdentifier, wizardService);

        GWT.log("Starting job wizard '" + wizardDisplayName + "'. Datastore=" + datastoreName + ", htmlDivId="
                + htmlDivId);

        controller.startWizard();
    }

    /**
     * Starts a datastore wizard based on parameters given from a native JS
     * call.
     * 
     * @param wizardDisplayName
     * @param htmlDivId
     */
    public static void startDatastoreWizard(final String wizardDisplayName, final String htmlDivId) {
        GWT.log("JavaScriptCallbacks.startDatastoreWizard(" + wizardDisplayName + "," + htmlDivId + ")");

        final ClientConfig clientConfig = new DictionaryClientConfig();

        final WizardIdentifier wizardIdentifier = getWizardIdentifier(wizardDisplayName);

        final WizardPanel wizardPanel = WizardPanelFactory.createWizardPanel(htmlDivId);
        final WizardServiceAsync wizardService = GWT.create(WizardService.class);
        final TenantIdentifier tenant = clientConfig.getTenant();

        final DatastoreWizardController controller = new DatastoreWizardController(wizardPanel, tenant,
                wizardIdentifier, wizardService);

        GWT.log("Starting datastore wizard '" + wizardDisplayName + "'. HtmlDivId=" + htmlDivId);

        controller.startWizard();
    }

    private static WizardIdentifier getWizardIdentifier(String wizardDisplayName) {
        if (wizardDisplayName == null || "".equals(wizardDisplayName.trim())) {
            return null;
        } else {
            return new WizardIdentifier(wizardDisplayName);
        }
    }
}