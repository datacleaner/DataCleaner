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
package org.datacleaner.metamodel.datahub.update;

/**
 * Identifier for source records by record type id and source name
 * 
 * Warning: This class copies the contract of the corresponding DataHub REST service.
 * Changes will likely break the contract with the server.
 *
 */
public abstract class AbstractSourceRecordIdentifier {

    String sourceName;
    String sourceRecordId;

    /**
     * Sets the source name
     * 
     * @param sourceName
     */
    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    /**
     * Sets the source record id
     * 
     * @param sourceRecordId
     */
    public void setSourceRecordId(String sourceRecordId) {
        this.sourceRecordId = sourceRecordId;
    }

    /**
     * Gets the source name
     * 
     * @return The source name
     */
    public String getSourceName() {
        return sourceName;
    }

    /**
     * Gets the source record id
     * 
     * @return The source record id
     */
    public String getSourceRecordId() {
        return sourceRecordId;
    }

}

