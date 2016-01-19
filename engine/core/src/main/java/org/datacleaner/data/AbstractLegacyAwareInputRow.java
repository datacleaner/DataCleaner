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
package org.datacleaner.data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectInputStream.GetField;
import java.io.ObjectStreamField;
import java.lang.reflect.Field;
import java.util.Collection;

import org.datacleaner.api.InputRow;

/**
 * Abstract super-class for {@link InputRow} implementations that are aware of
 * (and impacted by) the change of {@link InputRow#getId()} which was changed
 * from type int to long.
 * 
 * To enable deserialization of old objects where the value is stored as an int,
 * but should be deserialized into a long, this class provides a mechanism for
 * converting the values.
 */
abstract class AbstractLegacyAwareInputRow extends AbstractInputRow {

    private static final long serialVersionUID = 1L;

    protected abstract String getFieldNameForOldId();

    protected abstract String getFieldNameForNewId();

    protected abstract Collection<String> getFieldNamesInAdditionToId();

    /**
     * Subclasses should call this method within their
     * readObject(ObjectInputStream) invocations
     * 
     * @param stream
     * @throws IOException
     * @throws ClassNotFoundException
     */
    protected void doReadObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        final GetField readFields = stream.readFields();

        for (String fieldName : getFieldNamesInAdditionToId()) {
            final Object value = readFields.get(fieldName, null);
            setField(fieldName, value);
        }

        // fix issue of deserializing _rowNumber in it's previous int form
        final long rowNumber;
        final ObjectStreamField legacyRowNumberField = readFields.getObjectStreamClass().getField(
                getFieldNameForOldId());
        if (legacyRowNumberField != null) {
            rowNumber = readFields.get(getFieldNameForOldId(), -1);
        } else {
            rowNumber = readFields.get(getFieldNameForNewId(), -1l);
        }

        setField(getFieldNameForNewId(), rowNumber);
    }

    private void setField(String fieldName, Object value) {
        try {
            final Field field = getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(this, value);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to set field '" + fieldName + "' to value: " + value, e);
        }
    }
}
