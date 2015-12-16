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
 * Unique identifier of a source record by its source name, source record id and
 * record type key
 */

public class SourceRecordIdentifier {

    String recordTypeKey;
    String sourceName;
    String sourceRecordId;
    String recordTypeDescription;
    
    /**
     * Constructor taking a record type key
     * 
     * @param sourceName
     * @param sourceRecordId
     * @param recordTypeKey
     */
    public SourceRecordIdentifier(String sourceName, String sourceRecordId, String recordTypeKey) {
        this.sourceName = sourceName;
        this.sourceRecordId = sourceRecordId;
        this.recordTypeKey = recordTypeKey;
    }
    
    /**
     * Constructor taking record type key and description
     * 
     * @param sourceName
     * @param sourceRecordId
     * @param recordTypeKey
     * @param recordTypeDescription
     */
    public SourceRecordIdentifier(String sourceName, String sourceRecordId, String recordTypeKey, String recordTypeDescription) {
        this.sourceName = sourceName;
        this.sourceRecordId = sourceRecordId;
        this.recordTypeKey = recordTypeKey;
        this.recordTypeDescription = recordTypeDescription;
    }

    /**
     * Default constructor for JSON deserializing
     */
    public SourceRecordIdentifier() {
    }

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

    /**
     * Gets the record type key
     * 
     * @return The record type key
     */
    public String getRecordTypeKey() {
        return recordTypeKey;
    }

    /**
     * Sets the record type key
     * 
     * @param recordTypeKey
     */
    public void setRecordTypeKey(String recordTypeKey) {
        this.recordTypeKey = recordTypeKey;
    }
    
    /**
     * Gets the record type description
     * 
     * @return The record type description
     */
    public String getRecordTypeDescription() {
        return recordTypeDescription;
    }

    /**
     * Sets the record type description
     * 
     * @param recordTypeDescription
     */
    public void setRecordTypeDescription(String recordTypeDescription) {
        this.recordTypeDescription = recordTypeDescription;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((recordTypeKey == null) ? 0 : recordTypeKey.hashCode());
        result = prime * result + ((sourceName == null) ? 0 : sourceName.hashCode());
        result = prime * result + ((sourceRecordId == null) ? 0 : sourceRecordId.hashCode());
        result = prime * result + ((recordTypeDescription == null) ? 0 : recordTypeDescription.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SourceRecordIdentifier other = (SourceRecordIdentifier) obj;
        if (recordTypeKey == null) {
            if (other.recordTypeKey != null)
                return false;
        } else if (!recordTypeKey.equals(other.recordTypeKey))
            return false;
        if (sourceName == null) {
            if (other.sourceName != null)
                return false;
        } else if (!sourceName.equals(other.sourceName))
            return false;
        if (sourceRecordId == null) {
            if (other.sourceRecordId != null)
                return false;
        } else if (!sourceRecordId.equals(other.sourceRecordId))
            return false;
        if(recordTypeDescription == null){
            if(other.recordTypeDescription != null)
                return false;
        } else if (!recordTypeDescription.equals(other.recordTypeDescription))
            return false;
        return true;
    }
}

