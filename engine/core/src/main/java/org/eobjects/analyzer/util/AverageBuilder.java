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
/**
 *  This file is part of DataCleaner.
 *
 *  DataCleaner is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DataCleaner is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DataCleaner.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.eobjects.analyzer.util;

/**
 * Utility class for building an average from the values added using the
 * addValue(Number) method.
 * 
 * Note that this class is NOT thread-safe, so do not share an instance between
 * threads (or at least make sure to synchronize).
 * 
 * 
 */
public class AverageBuilder {

    private double _average;
    private int _numValues;

    public AverageBuilder() {
        _numValues = 0;
        _average = 0;
    }

    /**
     * Adds a value to the average that is being built.
     * 
     * @param number
     * @return
     */
    public AverageBuilder addValue(Number number) {
        double total = _average * _numValues + number.doubleValue();
        _numValues++;
        _average = total / _numValues;
        return this;
    }

    /**
     * Adds the same value [count] number of times to the average that is being
     * built.
     * 
     * @param number
     * @param count
     * @return
     */
    public AverageBuilder addValue(Number number, int count) {
        double total = _average * _numValues + number.doubleValue() * count;
        _numValues = _numValues + count;
        _average = total / _numValues;
        return this;
    }

    public double getAverage() {
        if (_numValues == 0) {
            return Double.NaN;
        }
        return _average;
    }

    public int getNumValues() {
        return _numValues;
    }

    @Override
    public String toString() {
        return "AverageBuilder[average=" + _average + ",numValues=" + _numValues + "]";
    }

    @Override
    public int hashCode() {
        return (int) (_average + _numValues);
    }
}
