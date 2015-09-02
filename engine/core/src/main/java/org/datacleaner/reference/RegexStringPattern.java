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
package org.datacleaner.reference;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.util.ReadObjectBuilder;
import org.elasticsearch.common.base.Objects;

/**
 * Represents a string pattern which is based on a regular expression (regex).
 * There are two basic modes of matching when using a regex string pattern:
 * Using entire string matching or subsequence matching. This mode is determined
 * using the <code>matchEntireString</code> property.
 * 
 * 
 */
public final class RegexStringPattern extends AbstractReferenceData implements StringPattern {

    private static final long serialVersionUID = 1L;

    private final String _expression;
    private final boolean _matchEntireString;

    public RegexStringPattern(String name, String expression, boolean matchEntireString) {
        super(name);
        _expression = expression;
        _matchEntireString = matchEntireString;
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            final RegexStringPattern other = (RegexStringPattern) obj;
            return Objects.equal(_expression, other._expression)
                    && Objects.equal(_matchEntireString, other._matchEntireString);
        }
        return false;
    }

    public boolean isMatchEntireString() {
        return _matchEntireString;
    }

    public String getExpression() {
        return _expression;
    }
    
    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        ReadObjectBuilder.create(this, RegexStringPattern.class).readObject(stream);
    }

    @Override
    public String toString() {
        return "RegexStringPattern[name=" + getName() + ", expression=" + _expression + ", matchEntireString="
                + _matchEntireString + "]";
    }

    @Override
    public StringPatternConnection openConnection(DataCleanerConfiguration configuration) {
        return new StringPatternConnection() {

            private final Pattern _pattern = Pattern.compile(_expression);

            @Override
            public boolean matches(String string) {
                if (string == null) {
                    return false;
                }
                Matcher matcher = _pattern.matcher(string);

                if (matcher.find()) {
                    if (_matchEntireString) {
                        int s = matcher.start();
                        if (s == 0) {
                            int e = matcher.end();
                            if (e == string.length()) {
                                return true;
                            }
                        }
                        return false;
                    }
                    return true;
                }
                return false;
            }

            @Override
            public void close() {
            }
        };
    }

}
