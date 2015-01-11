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
package org.datacleaner.result;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.datacleaner.api.InputColumn;

/**
 * Result reducer for {@link CharacterSetDistributionResult}s
 */
public class CharacterSetDistributionResultReducer extends
        AbstractCrosstabResultReducer<CharacterSetDistributionResult> {

    @Override
    protected Serializable reduceValues(List<Object> slaveValues, String category1, String category2,
            Collection<? extends CharacterSetDistributionResult> results, Class<?> valueClass) {
        int sum = 0;
        for (Object slaveValue : slaveValues) {
            sum += ((Number) slaveValue).intValue();
        }
        return sum;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected CharacterSetDistributionResult buildResult(Crosstab<?> crosstab,
            Collection<? extends CharacterSetDistributionResult> results) {
        final CharacterSetDistributionResult firstResult = results.iterator().next();

        final InputColumn<String>[] columns = firstResult.getColumns();
        final String[] unicodeSetNames = firstResult.getUnicodeSetNames();

        return new CharacterSetDistributionResult(columns, unicodeSetNames, (Crosstab<Number>) crosstab);
    }

}
