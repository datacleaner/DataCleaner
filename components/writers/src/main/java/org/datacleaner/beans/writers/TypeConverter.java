package org.datacleaner.beans.writers;

import java.util.Date;

import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.ColumnType;
import org.datacleaner.components.convert.ConvertToBooleanTransformer;
import org.datacleaner.components.convert.ConvertToNumberTransformer;

class TypeConverter {
    
    static Object convertType(final Object value, Column targetColumn) throws IllegalArgumentException {
        if (value == null) {
            return null;
        }
        Object result = value;
        ColumnType type = targetColumn.getType();
        if (type.isLiteral()) {
            // for strings, only convert some simple cases, since JDBC drivers
            // typically also do a decent job here (with eg. Clob types, char[]
            // types etc.)
            if (value instanceof Number || value instanceof Date) {
                result = value.toString();
            }
        } else if (type.isNumber()) {
            Number numberValue = ConvertToNumberTransformer.transformValue(value);
            if (numberValue == null && !"".equals(value)) {
                throw new IllegalArgumentException("Could not convert " + value + " to number");
            }
            result = numberValue;
        } else if (type == ColumnType.BOOLEAN) {
            Boolean booleanValue = ConvertToBooleanTransformer.transformValue(value);
            if (booleanValue == null && !"".equals(value)) {
                throw new IllegalArgumentException("Could not convert " + value + " to boolean");
            }
            result = booleanValue;
        }
        return result;
    }

}
