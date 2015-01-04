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

import java.util.List;
import java.util.Map;

import org.datacleaner.monitor.shared.model.DCUserInputException;

/**
 * A controller interface for wizard pages
 */
public interface WizardPageController {
    
    /**
     * Gets a (0-based) page index.
     * 
     * @return
     */
    public Integer getPageIndex();

    /**
     * Gets a HTML form element which represents this page rendered for the
     * user.
     * 
     * @return
     */
    public String getFormInnerHtml();

    /**
     * Submits the presented form and requests the next page in the wizard.
     * 
     * @param formParameters
     * @return
     */
    public WizardPageController nextPageController(Map<String, List<String>> formParameters)
            throws DCUserInputException;
}
