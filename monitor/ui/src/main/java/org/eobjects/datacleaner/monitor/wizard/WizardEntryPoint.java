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

import org.eobjects.datacleaner.monitor.shared.JavaScriptCallbacks;
import org.eobjects.datacleaner.monitor.util.ErrorHandler;

import com.google.gwt.core.client.GWT;

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
        GWT.setUncaughtExceptionHandler(ErrorHandler.getUncaughtExceptionHandler());
        JavaScriptCallbacks.exposeApi();
    }


}
