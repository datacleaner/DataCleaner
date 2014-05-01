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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

public class RemoveWizardClickHandler implements ClickHandler {

    private ClickHandler clickHandler;
    private JobWizardController jobWizardController;

    public RemoveWizardClickHandler(ClickHandler clickHandler, JobWizardController jobWizardController) {
        this.clickHandler = clickHandler;
        this.jobWizardController = jobWizardController;
    }

    @Override
    public void onClick(ClickEvent event) {
        jobWizardController.closeWizardAfterFinishing();
        clickHandler.onClick(event);
    }

}
