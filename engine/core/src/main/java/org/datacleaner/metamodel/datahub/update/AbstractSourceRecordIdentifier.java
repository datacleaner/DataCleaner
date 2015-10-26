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

