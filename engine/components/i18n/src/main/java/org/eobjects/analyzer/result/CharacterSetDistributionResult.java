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

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import org.eobjects.analyzer.beans.CharacterSetDistributionAnalyzer;
import org.eobjects.analyzer.beans.api.Distributed;
import org.eobjects.analyzer.data.InputColumn;

/**
 * Represents the result of a {@link CharacterSetDistributionAnalyzer} analyzer
 * 
 * 
 * 
 */
@Distributed(reducer = CharacterSetDistributionResultReducer.class)
public class CharacterSetDistributionResult extends CrosstabResult {

    private static final long serialVersionUID = 1L;

    private final InputColumn<String>[] _columns;
    private final Set<String> _unicodeSetNames;

    public CharacterSetDistributionResult(InputColumn<String>[] columns, String[] unicodeSetNames,
            Crosstab<Number> crosstab) {
        super(crosstab);
        _columns = columns;
        _unicodeSetNames = new TreeSet<String>();
        for (String unicodeSetName : unicodeSetNames) {
            _unicodeSetNames.add(unicodeSetName);
        }
    }

    public CharacterSetDistributionResult(InputColumn<String>[] columns, Collection<String> unicodeSetNames,
            Crosstab<Number> crosstab) {
        super(crosstab);
        _columns = columns;
        _unicodeSetNames = new TreeSet<String>(unicodeSetNames);
    }

    /**
     * Gets the columns that where analyzed
     * 
     * @return an array of {@link InputColumn}s.
     */
    public InputColumn<String>[] getColumns() {
        return Arrays.copyOf(_columns, _columns.length);
    }

    /**
     * Gets the names of the character sets which are available in the
     * distribution
     * 
     * @return an array of string names.
     */
    public String[] getUnicodeSetNames() {
        return _unicodeSetNames.toArray(new String[_unicodeSetNames.size()]);
    }

}
