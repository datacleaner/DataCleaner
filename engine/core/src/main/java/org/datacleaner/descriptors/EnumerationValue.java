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

    private final String value;
    private final String name;
    private final String[] aliases;
    private final Enum<?> enumValue;

    public EnumerationValue(String value, String name) {
        this(value, name, null);
    }

    public EnumerationValue(String enumValue, String enumName, String[] enumAliases){
        this.value = enumValue;
        this.name = enumName == null ? "" : enumName;
        this.aliases = enumAliases == null ? new String[0]: enumAliases;
        this.enumValue = null;
    }

    public EnumerationValue(String value) {
        this(value, value);
    }

    public EnumerationValue(Enum<?> value) {
        this.enumValue = value;
        this.value = enumValue.name();
        if (enumValue instanceof HasName) {
            String nameCandidate = ((HasName) enumValue).getName();
            if (nameCandidate == null) {
                name = "";
            } else {
                name = nameCandidate;
            }
        } else {
            name = value.toString();
        }

        if (enumValue instanceof HasAliases) {
            aliases = ((HasAliases) enumValue).getAliases();
        } else {
            aliases = new String[0];
        }

    }

    /** Available only if this object represents a Java enum value */
    public Enum<?> asJavaEnum() {
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
        throw new IllegalArgumentException("Unsupported enumeration value array: "
                + (value == null ? null : value.getClass()));
    }

    public String[] getAliases() {
        return aliases;
    }

    public static EnumerationProvider providerFromEnumClass(Class<? extends Enum<?>> enumClass) {
        final EnumerationValue[] values = EnumerationValue.fromArray(enumClass.getEnumConstants());
        return new EnumerationProvider() {
            @Override
            public EnumerationValue[] values() {
                return values;
            }
            @Override
            public EnumerationValue forString(String value) {
                for(EnumerationValue candidate: values) {
                    if(value.equals(candidate.getValue()) || value.equals(candidate.getName())) {
                        return candidate;
                    }
                    for(String alias: candidate.getAliases()) {
                        if(value.equals(alias)) {
                            return candidate;
                        }
                    }
                }
                return null;
            }
        };
    }

    @Override
    public int compareTo(EnumerationValue o) {
        final Enum<?> javaEnum2 = o.asJavaEnum();
        if (enumValue != null && javaEnum2 != null) {
            try {
                @SuppressWarnings("rawtypes")
                final Enum javaEnum1 = (Enum<?>) asJavaEnum();
                @SuppressWarnings("unchecked")
                int result = javaEnum1.compareTo(javaEnum2);
                return result;
            } catch (Exception e) {
                // nothing to do
            }
        }
        return getName().compareTo(o.getName());
    }
}
