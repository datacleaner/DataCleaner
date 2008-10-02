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
package dk.eobjects.datacleaner.profiler.pattern;

import java.util.regex.Pattern;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class Token {

	public static final short TYPE_WORD = 0;
	public static final short TYPE_NUMBER = 1;
	public static final short TYPE_DELIM = 2;
	public static final short TYPE_MIXED = 3;

	private char[] _chars;
	private short _type = -1;
	private Integer _length = null;

	public Token(char[] chars) {
		_chars = chars;
	}

	public Token(short type, int length) {
		if (type == TYPE_DELIM) {
			throw new IllegalArgumentException(
					"Cannot create arbitrary DELIM tokens.");
		}
		_type = type;
		_length = length;
	}

	public short getType() {
		if (_type == -1) {
			boolean foundLetter = false;
			boolean foundDigit = false;
			boolean foundDelim = false;
			for (int i = 0; i < _chars.length; i++) {
				if (Character.isLetter(_chars[i])) {
					foundLetter = true;
				} else if (Character.isDigit(_chars[i])) {
					foundDigit = true;
				} else {
					foundDelim = true;
				}
			}
			if (foundLetter && foundDigit && !foundDelim) {
				_type = TYPE_MIXED;
			} else if (foundLetter && !foundDigit && !foundDelim) {
				_type = TYPE_WORD;
			} else if (!foundLetter && foundDigit && !foundDelim) {
				_type = TYPE_NUMBER;
			} else if (!foundLetter && !foundDigit && foundDelim) {
				_type = TYPE_DELIM;
			} else {
				throw new IllegalStateException(
						"Chars in Token contained both delim chars and letters/digits.");
			}
		}
		return _type;
	}

	public int getLength() {
		if (_length == null) {
			_length = _chars.length;
		}
		return _length;
	}

	public boolean isWord() {
		return getType() == TYPE_WORD;
	}

	public boolean isDelimitor() {
		return getType() == TYPE_DELIM;
	}

	public boolean isNumber() {
		return getType() == TYPE_NUMBER;
	}

	public boolean isMixed() {
		return getType() == TYPE_MIXED;
	}

	public String toRegex() {
		short type = getType();
		StringBuilder sb = new StringBuilder();
		if (type == TYPE_DELIM) {
			sb.append(Pattern.quote(new String(_chars)));
		} else {
			if (type == TYPE_MIXED) {
				sb.append("[a-zA-Z0-9]");
			} else if (type == TYPE_WORD) {
				sb.append("[a-zA-Z]");
			} else if (type == TYPE_NUMBER) {
				sb.append("[0-9]");
			}
			sb.append("{1," + getLength() + '}');
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		return new String(_chars);
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(_chars).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Token) {
			Token that = (Token) obj;
			return new EqualsBuilder().append(this.getType(), that.getType())
					.append(this.getLength(), that.getLength()).isEquals();
		}
		return false;
	}
}