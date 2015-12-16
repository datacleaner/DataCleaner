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
package org.datacleaner.regexswap;

import java.util.Objects;

import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.reference.AbstractReferenceData;
import org.datacleaner.reference.RegexStringPattern;
import org.datacleaner.reference.StringPattern;
import org.datacleaner.reference.StringPatternConnection;

/**
 * A specialized type of string pattern, based on a regex downloaded from the
 * regex swap
 */
public final class RegexSwapStringPattern extends AbstractReferenceData implements StringPattern {

    private static final long serialVersionUID = 1L;
    private final Regex _regex;
    private transient RegexStringPattern _delegate;

    public RegexSwapStringPattern(Regex regex) {
        super(regex.getName());
        setDescription(regex.getDescription());
        _regex = regex;
    }

    @Override
    public String toString() {
        return "RegexSwapStringPattern[regex=" + _regex + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            final RegexSwapStringPattern other = (RegexSwapStringPattern) obj;
            return Objects.equals(_regex, other._regex);
        }
        return false;
    }

    @Override
    public StringPatternConnection openConnection(DataCleanerConfiguration configuration) {
        if (_delegate == null) {
            _delegate = new RegexStringPattern(getName(), _regex.getExpression(), true);
        }
        return _delegate.openConnection(configuration);
    }

    public Regex getRegex() {
        return _regex;
    }
}
