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

public class SimpleToken implements Token {

	private TokenType _type;
	private StringBuilder _stringBuilder;

	public SimpleToken(TokenType type, StringBuilder stringBuilder) {
		_type = type;
		_stringBuilder = stringBuilder;
	}

	public SimpleToken(TokenType type, String string) {
		_type = type;
		_stringBuilder = new StringBuilder(string);
	}

	public SimpleToken(TokenType type, char c) {
		_type = type;
		_stringBuilder = new StringBuilder();
		_stringBuilder.append(c);
	}

	@Override
	public String getString() {
		return _stringBuilder.toString();
	}

	public void appendChar(char c) {
		_stringBuilder.append(c);
	}

	public void appendString(String s) {
		_stringBuilder.append(s);
	}

	public void prependChar(char c) {
		_stringBuilder.insert(0, c);
	}

	@Override
	public int length() {
		return _stringBuilder.length();
	}

	@Override
	public char charAt(int index) {
		return _stringBuilder.charAt(index);
	}

	@Override
	public TokenType getType() {
		return _type;
	}

	public void setType(TokenType type) {
		_type = type;
	}

	@Override
	public String toString() {
		return "Token['" + getString() + "' (" + _type + ")]";
	}
}
