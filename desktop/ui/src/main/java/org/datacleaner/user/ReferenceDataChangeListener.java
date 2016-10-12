package org.datacleaner.user;

/**
 * Interface for changing the value of a Reference
 * @param <ReferenceData>
 */
public interface ReferenceDataChangeListener<ReferenceData> {
    
    public void onAdd(ReferenceData stringPattern);

    public void onChange(ReferenceData oldPattern, ReferenceData newPattern);
    
    public void onRemove(ReferenceData stringPattern);

}
