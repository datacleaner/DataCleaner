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
 * Identifies a job creation wizard
 */
public class WizardIdentifier implements Serializable {

    private static final long serialVersionUID = 1L;

    private String _displayName;
    private int _expectedPageCount;
    private boolean _datastoreConsumer;

    public WizardIdentifier(String displayName) {
        this();
        _displayName = displayName;
        _datastoreConsumer = true;
    }
    
    public WizardIdentifier() {
    }

    public String getDisplayName() {
        return _displayName;
    }

    public void setDisplayName(String displayName) {
        this._displayName = displayName;
    }

    public int getExpectedPageCount() {
        return _expectedPageCount;
    }

    public void setExpectedPageCount(int expectedPageCount) {
        _expectedPageCount = expectedPageCount;
    }
    
    public boolean isDatastoreConsumer() {
        return _datastoreConsumer;
    }
    
    public void setDatastoreConsumer(boolean datastoreConsumer) {
        _datastoreConsumer = datastoreConsumer;
    }

    @Override
    public String toString() {
        return "JobWizardIdentifier[" + _displayName + "]";
    }
}
