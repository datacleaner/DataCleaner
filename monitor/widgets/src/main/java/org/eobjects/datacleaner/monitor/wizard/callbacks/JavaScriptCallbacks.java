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

package org.eobjects.datacleaner.monitor.wizard.callbacks;

public final class JavaScriptCallbacks {

	/**
	 * Called when the job wizard is finished
	 */
	public static native void onWizardFinished() /*-{
    													if ($wnd.datacleaner && $wnd.datacleaner.onWizardFinished) {
    														$wnd.datacleaner.onWizardFinished();
														}
													}-*/;

	public static native void onSimpleWizardPanelClosed() /*-{
																
																if ($wnd.datacleaner && $wnd.datacleaner.onSimpleWizardPanelClosed) {
																	$wnd.datacleaner.onSimpleWizardPanelClosed();
																}
															}-*/; 
		
}