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
package org.eobjects.analyzer.beans.stringpattern;

import java.util.Arrays;

import org.apache.metamodel.util.EqualsBuilder;

public class TokenPatternSymbolImpl implements TokenPatternSymbol {

    private static final long serialVersionUID = 1L;

    private final TokenType _tokenType;
    private int _length;
    private boolean _upperCaseOnly = false;
    private boolean _lowerCaseOnly = false;
    private boolean _negative = false;
    private boolean _decimal = false;
    private char _decimalSeparator;
    private char _minusSign;
    private String _symbolicString;

    public TokenPatternSymbolImpl(Token prototypeToken, TokenizerConfiguration configuration) {
        _tokenType = prototypeToken.getType();
        _length = prototypeToken.length();
        switch (_tokenType) {
        case TEXT:
            if (configuration.isDiscriminateTextCase()) {
                _upperCaseOnly = Character.isUpperCase(prototypeToken.charAt(0));
                _lowerCaseOnly = !_upperCaseOnly;
            }
            break;
        case NUMBER:
            if (configuration.isDiscriminateDecimalNumbers()) {
                Character decimalSeparator = configuration.getDecimalSeparator();
                if (decimalSeparator != null) {
                    _decimalSeparator = decimalSeparator.charValue();
                    _decimal = prototypeToken.getString().indexOf(_decimalSeparator) != -1;
                }
            }

            if (configuration.isDiscriminateNegativeNumbers()) {
                Character minusSign = configuration.getMinusSign();
                if (minusSign != null) {
                    _minusSign = minusSign.charValue();
                    _negative = (_minusSign == prototypeToken.charAt(0));
                }
            }

            break;
        case DELIM:
            _symbolicString = prototypeToken.getString();
            break;
        case WHITESPACE:
            if (configuration.isDiscriminateWhiteSpaces()) {
                _symbolicString = prototypeToken.getString();
            }
            break;
        case PREDEFINED:
            if (prototypeToken instanceof PredefinedToken) {
                PredefinedToken pt = (PredefinedToken) prototypeToken;
                _symbolicString = '[' + pt.getPredefinedTokenDefintion().getName() + ']';
            } else {
                _symbolicString = prototypeToken.getString();
            }
            break;
        case MIXED:
            break;
        default:
            throw new UnsupportedOperationException("Unsupported token type: " + _tokenType);
        }
    }

    @Override
    public String toSymbolicString() {
        if (_symbolicString != null) {
            return _symbolicString;
        }

        char c = getSymbolicChar();
        char[] result = new char[_length];
        Arrays.fill(result, c);

        if (isNegative()) {
            result[0] = _minusSign;
        }
        if (isDecimal()) {
            result[_length - 2] = _decimalSeparator;
        }

        return String.valueOf(result);
    }

    private char getSymbolicChar() {
        switch (_tokenType) {
        case TEXT:
            if (isUpperCaseOnly()) {
                return 'A';
            }
            return 'a';
        case NUMBER:
            return '#';
        case WHITESPACE:
            return ' ';
        case MIXED:
            return '?';
        default:
            throw new UnsupportedOperationException("No symbolic char for token type: " + _tokenType);
        }
    }

    @Override
    public TokenType getTokenType() {
        return _tokenType;
    }

    @Override
    public boolean isUpperCaseOnly() {
        return _upperCaseOnly;
    }

    @Override
    public boolean isLowerCaseOnly() {
        return _lowerCaseOnly;
    }

    @Override
    public boolean isDecimal() {
        return _decimal;
    }

    @Override
    public boolean isNegative() {
        return _negative;
    }

    @Override
    public boolean matches(Token token, TokenizerConfiguration configuration) {
        if (EqualsBuilder.equals(_tokenType, token.getType())) {
            if (configuration.isDistriminateTokenLength(_tokenType)) {
                if (toSymbolicString().length() != token.getString().length()) {
                    // not a match, based on length
                    return false;
                }
            }

            switch (_tokenType) {
            case TEXT:
                return matchesText(token, configuration);
            case NUMBER:
                return matchesNumber(token, configuration);
            case DELIM:
                return matchesDelim(token, configuration);
            case WHITESPACE:
                return matchesWhitespace(token, configuration);
            case MIXED:
                return matchesMixed(token, configuration);
            case PREDEFINED:
                return matchesPredefined(token);
            default:
                throw new UnsupportedOperationException("Unsupported token type for matching: " + _tokenType);
            }
        }
        return false;
    }

    private boolean matchesPredefined(Token token) {
        if (token instanceof PredefinedToken) {
            PredefinedToken pt = (PredefinedToken) token;
            String name = pt.getPredefinedTokenDefintion().getName();
            return _symbolicString.equals('[' + name + ']');
        } else {
            return _symbolicString.equals(token.getString());
        }
    }

    private boolean matchesText(Token token, TokenizerConfiguration configuration) {
        boolean discriminateTextCase = configuration.isDiscriminateTextCase();
        if (discriminateTextCase) {

            // if 'discriminateTextCase' is true then we can assume that all the
            // characters are either upper or lower case. Thus it is only
            // nescesary to check a single character from each string
            String str2 = token.getString();
            char char2 = str2.charAt(0);
            boolean upperCase = Character.isUpperCase(char2);

            boolean caseMatches = isUpperCaseOnly() == upperCase;
            if (!caseMatches) {
                return false;
            }

            if (upperCase && !configuration.isUpperCaseExpandable()) {
                // the token is not expandable, we need to verify same length
                return str2.length() == _length;
            }

            if (!upperCase && !configuration.isLowerCaseExpandable()) {
                // the token is not expandable, we need to verify same length
                return str2.length() == _length;
            }

            return true;
        }
        return true;
    }

    private boolean matchesNumber(Token token, TokenizerConfiguration configuration) {
        boolean discriminateNegativeNumbers = configuration.isDiscriminateNegativeNumbers();
        boolean discriminateDecimalNumbers = configuration.isDiscriminateDecimalNumbers();
        if (!discriminateDecimalNumbers && !discriminateNegativeNumbers) {
            return true;
        }

        String str2 = token.getString();

        Character minusSign = configuration.getMinusSign();
        if (discriminateNegativeNumbers && minusSign != null) {
            boolean negative1 = isNegative();
            boolean negative2 = EqualsBuilder.equals(minusSign, str2.charAt(0));
            if (negative1 != negative2) {
                return false;
            }
        }
        Character decimalSeparator = configuration.getDecimalSeparator();
        if (discriminateDecimalNumbers && decimalSeparator != null) {
            boolean decimal1 = isDecimal();
            boolean decimal2 = str2.indexOf(decimalSeparator.charValue()) != -1;
            if (decimal1 != decimal2) {
                return false;
            }
        }

        return true;
    }

    private boolean matchesDelim(Token token, TokenizerConfiguration configuration) {
        return toSymbolicString().equals(token.getString());
    }

    private boolean matchesWhitespace(Token token, TokenizerConfiguration configuration) {
        if (configuration.isDiscriminateWhiteSpaces()) {
            return toSymbolicString().equals(token.getString());
        }
        return true;
    }

    private boolean matchesMixed(Token token, TokenizerConfiguration configuration) {
        return true;
    }

    @Override
    public int length() {
        return _length;
    }

    @Override
    public boolean isExpandable() {
        return _symbolicString == null;
    }

    @Override
    public void expandLenght(int amount) {
        _length += amount;
    }
}
