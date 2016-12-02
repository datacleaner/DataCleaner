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
 * Represents a session ID on the client side.
 */
public class WizardSessionIdentifier implements Serializable {

    private static final long serialVersionUID = 1L;

    private String sessionId;
    private WizardIdentifier wizardIdentifier;

    /**
     * Default no-arg constructor
     */
    public WizardSessionIdentifier() {
    }

    /**
     * Constructor with all arguments
     *
     * @param sessionId
     * @param wizardIdentifier
     */
    public WizardSessionIdentifier(final String sessionId, final WizardIdentifier wizardIdentifier) {
        this.sessionId = sessionId;
        this.wizardIdentifier = wizardIdentifier;
    }

    public WizardIdentifier getWizardIdentifier() {
        return wizardIdentifier;
    }

    public void setWizardIdentifier(final WizardIdentifier wizardIdentifier) {
        this.wizardIdentifier = wizardIdentifier;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(final String sessionId) {
        this.sessionId = sessionId;
    }
}
