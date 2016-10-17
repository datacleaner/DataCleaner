package org.datacleaner.user;

/**
 * Interface for adding, removing, changing the value of a Reference
 * @param <ReferenceData>
 */
public interface ReferenceDataChangeListener<ReferenceData> {
    
    public void onAdd(ReferenceData referenceData);

    public void onChange(ReferenceData oldReferenceData, ReferenceData newReferenceData);
    
    public void onRemove(ReferenceData referenceData);

}
