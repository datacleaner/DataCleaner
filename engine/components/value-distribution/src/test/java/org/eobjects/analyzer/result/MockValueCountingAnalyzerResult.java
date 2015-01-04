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
package org.eobjects.analyzer.result;

import java.util.Collection;

public class MockValueCountingAnalyzerResult extends AbstractValueCountingAnalyzerResult {

    private static final long serialVersionUID = 1L;
    
    private final Collection<ValueFrequency> _valueCounts;
    
    public MockValueCountingAnalyzerResult(Collection<ValueFrequency> valueCounts) {
        _valueCounts = valueCounts;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Collection<ValueFrequency> getValueCounts() {
        return _valueCounts;
    }

    @Override
    public int getNullCount() {
        return 0;
    }

    @Override
    public int getTotalCount() {
        return 0;
    }

    @Override
    public Integer getCount(String value) {
        return null;
    }

    @Override
    public Integer getDistinctCount() {
        return null;
    }

    @Override
    public Integer getUniqueCount() {
        return null;
    }

    @Override
    public Integer getUnexpectedValueCount() {
        return null;
    }

    @Override
    public boolean hasAnnotatedRows(String value) {
        return false;
    }

    @Override
    public AnnotatedRowsResult getAnnotatedRowsForValue(String value) {
        return null;
    }

    @Override
    public AnnotatedRowsResult getAnnotatedRowsForNull() {
        return null;
    }

    @Override
    public AnnotatedRowsResult getAnnotatedRowsForUnexpectedValues() {
        return null;
    }

    @Override
    public Collection<String> getUniqueValues() {
        return null;
    }

}
