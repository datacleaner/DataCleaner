/**
 * AnalyzerBeans
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
package org.eobjects.analyzer.beans.coalesce;

import org.eobjects.analyzer.beans.api.Converter;
import org.eobjects.analyzer.util.convert.ArrayConverter;
import org.eobjects.analyzer.util.convert.StandardTypeConverter;

/**
 * A {@link Converter} for {@link CoalesceUnit}
 */
public class CoalesceUnitConverter implements Converter<CoalesceUnit> {

    private final ArrayConverter delegate = new ArrayConverter(new StandardTypeConverter());

    @Override
    public CoalesceUnit fromString(Class<?> type, String serializedForm) {
        String[] columnNames = (String[]) delegate.fromString(String[].class, serializedForm);
        return new CoalesceUnit(columnNames);
    }

    @Override
    public String toString(CoalesceUnit instance) {
        String[] inputColumnNames = instance.getInputColumnNames();
        return delegate.toString(inputColumnNames);
    }

    @Override
    public boolean isConvertable(Class<?> type) {
        return type == CoalesceUnit.class;
    }

}
