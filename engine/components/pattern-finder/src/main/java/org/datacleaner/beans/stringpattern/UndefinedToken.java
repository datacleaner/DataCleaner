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
package org.datacleaner.beans.stringpattern;

public class UndefinedToken implements Token {

	private String _string;

	public UndefinedToken(String string) {
		_string = string;
	}

	public String getString() {
		return _string;
	}

	@Override
	public TokenType getType() {
		return TokenType.UNDEFINED;
	}

	@Override
	public String toString() {
		return "UndefinedToken['" + _string + "']";
	}

	@Override
	public int length() {
		return _string.length();
	}

	@Override
	public char charAt(int index) {
		return _string.charAt(index);
	}
}
