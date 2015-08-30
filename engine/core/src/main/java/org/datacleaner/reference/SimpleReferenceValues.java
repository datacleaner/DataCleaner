package org.datacleaner.reference;

import java.io.Serializable;

/**
 * @deprecated since DataCleaner 4.1 this is no longer used, but for
 *             deserialization compatibility it is retained.
 */
@Deprecated
public class SimpleReferenceValues implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Object[] _values;

    public SimpleReferenceValues(Object... values) {
        _values = values;
    }

    public Object[] getValues() {
        return _values;
    };
}
