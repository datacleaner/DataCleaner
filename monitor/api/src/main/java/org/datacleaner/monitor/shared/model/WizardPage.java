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
package org.datacleaner.monitor.shared.model;

import java.io.Serializable;

/**
 * The client-side representation of a wizard page
 */
public class WizardPage implements Serializable {

    public static final int PAGE_INDEX_FINISHED = -1000;

    private static final long serialVersionUID = 1L;

    private WizardSessionIdentifier sessionIdentifier;
    private String formInnerHtml;
    private Integer pageIndex;
    private Integer expectedPageCount;
    private String wizardResult;

    /**
     * Gets the HTML of the current page. If the wizard has finished (see
     * {@link #isFinished()}), this method will return null.
     * 
     * @return
     */
    public String getFormInnerHtml() {
        return formInnerHtml;
    }

    public void setFormInnerHtml(String formInnerHtml) {
        this.formInnerHtml = formInnerHtml;
    }

    /**
     * Gets the session identifier of the wizard.
     * 
     * @return
     */
    public WizardSessionIdentifier getSessionIdentifier() {
        return sessionIdentifier;
    }

    public void setSessionIdentifier(WizardSessionIdentifier sessionIdentifier) {
        this.sessionIdentifier = sessionIdentifier;
    }

    /**
     * Gets the index (0-based) of the current page of the wizard. In case the
     * wizard has finished, this method will return {@link #PAGE_INDEX_FINISHED}
     * .
     * 
     * @return
     */
    public Integer getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(Integer pageIndex) {
        this.pageIndex = pageIndex;
    }

    /**
     * Gets the expected count of pages as per the current state of the wizard
     * session
     * 
     * @return
     */
    public Integer getExpectedPageCount() {
        return expectedPageCount;
    }

    public void setExpectedPageCount(Integer expectedPageCount) {
        this.expectedPageCount = expectedPageCount;
    }

    /**
     * If a wizard has finished (see {@link #isFinished()}) then this method
     * will return the name of the entity (the 'result') that was created.
     * 
     * @return
     */
    public String getWizardResult() {
        return wizardResult;
    }

    public void setWizardResult(String wizardResult) {
        this.wizardResult = wizardResult;
    }

    /**
     * Determines if the wizard is finished
     * 
     * @return
     */
    public boolean isFinished() {
        return getPageIndex() == PAGE_INDEX_FINISHED;
    }
}
