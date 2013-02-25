/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.monitor.shared.model;

import java.io.Serializable;

public class JobWizardPage implements Serializable {

    private static final long serialVersionUID = 1L;

    private JobWizardSessionIdentifier sessionIdentifier;
    private String formInnerHtml;
    private Integer pageIndex;
    private Integer expectedPageCount;

    public String getFormInnerHtml() {
        return formInnerHtml;
    }

    public void setFormInnerHtml(String formInnerHtml) {
        this.formInnerHtml = formInnerHtml;
    }

    public JobWizardSessionIdentifier getSessionIdentifier() {
        return sessionIdentifier;
    }

    public void setSessionIdentifier(JobWizardSessionIdentifier sessionIdentifier) {
        this.sessionIdentifier = sessionIdentifier;
    }

    public Integer getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(Integer pageIndex) {
        this.pageIndex = pageIndex;
    }

    public Integer getExpectedPageCount() {
        return expectedPageCount;
    }

    public void setExpectedPageCount(Integer expectedPageCount) {
        this.expectedPageCount = expectedPageCount;
    }
}
