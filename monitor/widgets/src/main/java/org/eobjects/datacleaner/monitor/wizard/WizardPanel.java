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

import org.eobjects.datacleaner.monitor.shared.widgets.ButtonPanel;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

/**
 * Interface for controlling panel attributes required for wizard.
 */
public interface WizardPanel {

    /**
     * Sets header for the wizard on the top of the panel
     * 
     * @param header
     */
    public void setHeader(String header);

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
     * Add close handler for the wizard panel
     * 
     * TODO: Shouldn't this method be moved to the controller rather than the
     * panel?
     * 
     * @param closeHandler
     */
    public void addWizardCloseHandler(WizardCloseHandler closeHandler);

    /**
     * Hides the wizard
     */
    public void hideWizard();
}
