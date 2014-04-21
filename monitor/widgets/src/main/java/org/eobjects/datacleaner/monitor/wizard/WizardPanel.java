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

import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Widget;

/**
 * Interface for controlling panel attributed required for wizard.
 * 
 */
public interface WizardPanel {

    /**
     * Sets header for the wizard on the top of the panel
     * 
     * @param header
     */
    public void setHeader(String header);

    /**
     * Can be used for adding buttons on WizardPanel
     * 
     * @param button
     */
    public void addButton(Button button);

    /**
     * Can be used for removing buttons from wizard Panel
     * 
     * @param button
     */
    public void removeButton(Button button);

    /**
     * Get button panel
     * 
     * @return ButtonPanel
     */
    public ButtonPanel getButtonPanel();

    /**
     * Remove all buttons
     */
    public void removeButtons();

    /**
     * Add a widget on the wizard panel
     * 
     * @param w
     */
    public void setWidget(Widget w);

    /**
     * Toggle visibilty of the Panel
     * 
     * @param b
     */
    public void setVisible(boolean b);

    /**
     * Add style to Panel
     * 
     * @param string
     */
    public void addStyleName(String string);

    /**
     * Add close handler for the wizard panel
     * 
     * @param closeHandler
     */
    public void addWizardCloseHandler(CloseHandler<? extends Widget> closeHandler);

    /**
     * Get the instance of current Wizard
     * 
     * @return
     */
    public Widget getInstance();

    /**
     * If this is implemented as a GWT PopupPanel, then calls the method
     * center() in the PopupPanel class, else does nothing
     */
    public void center();

    /**
     * Hides the wizard
     */
    public void hideWizard();
}
