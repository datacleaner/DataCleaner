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
package org.datacleaner.monitor.wizard;

import org.datacleaner.monitor.shared.widgets.ButtonPanel;
import org.datacleaner.monitor.shared.widgets.WizardProgressBar;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

/**
 * Interface for controlling panel attributes required for wizard.
 */
public interface WizardPanel {

    /**
     * Gets the ID of the HTML element of this WizardPanel, if it can be
     * referenced and reused for other wizard panel locations.
     * 
     * @return
     */
    public String getCustomHtmlDivId();

    /**
     * Adds a style class to the {@link WizardPanel}.
     * 
     * @param styleClass
     */
    public void addStyleClass(String styleClass);

    /**
     * Sets header for the wizard on the top of the panel
     * 
     * @param header
     */
    public void setHeader(String header);

    /**
     * Gets the {@link WizardProgressBar} of this wizard panel.
     * 
     * @return
     */
    public WizardProgressBar getProgressBar();

    /**
     * Get button panel
     * 
     * @return ButtonPanel
     */
    public ButtonPanel getButtonPanel();

    /**
     * Add a widget on the wizard panel
     * 
     * @param w
     */
    public void setContent(IsWidget w);

    /**
     * Gets the widget representing the complete wizard panel.
     * 
     * Mostly this should not be called. Rather simply use methods
     * {@link #setContent(IsWidget)}, {@link #setHeader(String)},
     * {@link #getButtonPanel()}, {@link #showWizard()} or {@link #hideWizard()}
     * .
     * 
     * @return
     */
    public Widget getWizardWidget();

    /**
     * Shows the wizard on the screen
     * 
     * @param b
     */
    public void showWizard();

    /**
     * Hides the wizard
     */
    public void hideWizard();

    /**
     * Requests the wizard panel to refresh it's UI. This is typically called
     * because some of the existing content has changed, and the UI may need to
     * adapt (center dialogs, repack frames etc.)
     */
    public void refreshUI();
}
