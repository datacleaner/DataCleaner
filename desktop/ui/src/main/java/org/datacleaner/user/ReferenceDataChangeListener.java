package org.datacleaner.user;

/**
 * Interface for changing the value of a Reference
 * @param <ReferenceData>
 */
public interface ReferenceDataChangeListener<ReferenceData> {
    
    public void onAdd(ReferenceData referenceData);

    public void onChange(ReferenceData oldReferenceData, ReferenceData newreferenceData);
    
    public void onRemove(ReferenceData referenceData);

}
