package org.datacleaner.metamodel.datahub.update;

/**
 * Unique identifier of a source record by its source name, source record id and
 * record type description
 * 
 * Warning: This class copies the contract of the corresponding DataHub REST service.
 * Changes will likely break the contract with the server.
 */

public class SourceRecordByDescriptionIdentifier extends AbstractSourceRecordIdentifier {

    String recordTypeDescription;

    /**
     * Constructor
     * 
     * @param sourceName
     * @param sourceRecordId
     * @param recordTypeDescription
     */
    public SourceRecordByDescriptionIdentifier(String sourceName, String sourceRecordId, String recordTypeDescription) {
        this.sourceName = sourceName;
        this.sourceRecordId = sourceRecordId;
        this.recordTypeDescription = recordTypeDescription;
    }

    /**
     * Default constructor for JSON deserializing
     */
    public SourceRecordByDescriptionIdentifier() {
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
    public void setrecordTypeDescription(String recordTypeDescription) {
        this.recordTypeDescription = recordTypeDescription;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((recordTypeDescription == null) ? 0 : recordTypeDescription.hashCode());
        result = prime * result + ((sourceName == null) ? 0 : sourceName.hashCode());
        result = prime * result + ((sourceRecordId == null) ? 0 : sourceRecordId.hashCode());
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
        SourceRecordByDescriptionIdentifier other = (SourceRecordByDescriptionIdentifier) obj;
        if (recordTypeDescription == null) {
            if (other.recordTypeDescription != null)
                return false;
        } else if (!recordTypeDescription.equals(other.recordTypeDescription))
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
        return true;
    }
}
