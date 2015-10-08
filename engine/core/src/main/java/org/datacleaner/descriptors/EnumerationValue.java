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
package org.datacleaner.descriptors;

import java.io.IOException;
import java.io.Serializable;

import org.apache.metamodel.util.HasName;
import org.datacleaner.util.HasAliases;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;

/**
 * This class represents enumeration type for property of remote transformer, in
 * situation when the enumeration class is not on clients classpath.
 *
 * @Since 9/15/15
 */
public class EnumerationValue implements HasName, JsonSerializable, Serializable, Comparable<EnumerationValue> {

    private static final long serialVersionUID = 1L;

    private String value;
    private String name;
    private Enum<?> enumValue;

    public EnumerationValue(String value, String name) {
        this.value = value;
        this.name = name == null ? "" : name;
    }

    public EnumerationValue(String value) {
        this(value, value);
    }

    public EnumerationValue(Enum<?> value) {
        this.enumValue = value;
        this.value = enumValue.name();
        if (enumValue instanceof HasName) {
            name = ((HasName) enumValue).getName();
            if(name == null) { name = ""; }
        } else {
            name = value.toString();
        }
    }

    /** Available only if this object represents a Java enum value */
    public Enum asJavaEnum() {
        return enumValue;
    }

    /** The enum constant */
    public String getValue() {
        return value;
    }

    /** This should return the enum human-readable name */
    @Override
    public String getName() {
        return name;
    }

    @Override
    public void serialize(JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeString(value);
    }

    @Override
    public void serializeWithType(JsonGenerator jgen, SerializerProvider provider, TypeSerializer typeSer)
            throws IOException, JsonProcessingException {
        jgen.writeString(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        EnumerationValue that = (EnumerationValue) o;

        if (value != null ? !value.equals(that.value) : that.value != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }

    public static EnumerationValue[] fromArray(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof EnumerationValue[]) {
            return (EnumerationValue[]) value;
        }

        // Array of java enums will be converted
        if (value != null && value.getClass().isArray()) {
            if (value.getClass().getComponentType().isEnum()) {
                Enum<?>[] values = (Enum[]) value;
                EnumerationValue[] result = new EnumerationValue[values.length];
                for (int i = 0; i < result.length; i++) {
                    result[i] = new EnumerationValue(values[i]);
                }
                return result;
            }
        }
        throw new IllegalArgumentException(
                "Unsupported enumeration value array: " + (value == null ? null : value.getClass()));
    }

    public String[] getAliases() {
        if (enumValue instanceof HasAliases) {
            return ((HasAliases) enumValue).getAliases();
        }
        return new String[0];
    }

    public static EnumerationProvider providerFromEnumClass(Class<? extends Enum<?>> enumClass) {
        final EnumerationValue[] values = EnumerationValue.fromArray(enumClass.getEnumConstants());
        return new EnumerationProvider() {
            @Override
            public EnumerationValue[] values() {
                return values;
            }
        };
    }

    @Override
    public int compareTo(EnumerationValue o) {
        if(enumValue != null && o.asJavaEnum() != null) {
            try {
                return asJavaEnum().compareTo(o.asJavaEnum());
            } catch (Exception e) {
                // nothing to do
            }
        }
        return getName().compareTo(o.getName());
    }
}
