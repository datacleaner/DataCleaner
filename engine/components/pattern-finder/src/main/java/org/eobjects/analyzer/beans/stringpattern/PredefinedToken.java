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
package org.eobjects.analyzer.beans.stringpattern;

/**
 * Defines a "predefined" token which is a token type that have been found based
 * on the users entered patterns, before any other pattern finding processing.
 * using a set of regular expressions and given a name.
 * 
 * 
 * 
 */
public class PredefinedToken implements Token {

	private PredefinedTokenDefinition _predefinedTokenDefintion;
	private String _string;

	/**
	 * Constructs a predefined token.
	 * 
	 * @param tokenDefinition
	 *            the definition of the token type
	 * @param string
	 *            the string part that matches the token definition.
	 */
	public PredefinedToken(PredefinedTokenDefinition tokenDefinition, String string) {
		_predefinedTokenDefintion = tokenDefinition;
		_string = string;
	}

	public PredefinedTokenDefinition getPredefinedTokenDefintion() {
		return _predefinedTokenDefintion;
	}

	@Override
	public int length() {
		return _string.length();
	}

	@Override
	public String getString() {
		return _string;
	}

	@Override
	public TokenType getType() {
		return TokenType.PREDEFINED;
	}

	@Override
	public String toString() {
		return "Token['" + _string + "' (PREDEFINED " + _predefinedTokenDefintion.getName() + ")]";
	}

	@Override
	public char charAt(int index) {
		return _string.charAt(index);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_predefinedTokenDefintion == null) ? 0 : _predefinedTokenDefintion.hashCode());
		result = prime * result + ((_string == null) ? 0 : _string.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PredefinedToken other = (PredefinedToken) obj;
		if (_predefinedTokenDefintion == null) {
			if (other._predefinedTokenDefintion != null)
				return false;
		} else if (!_predefinedTokenDefintion.equals(other._predefinedTokenDefintion))
			return false;
		if (_string == null) {
			if (other._string != null)
				return false;
		} else if (!_string.equals(other._string))
			return false;
		return true;
	}
}
