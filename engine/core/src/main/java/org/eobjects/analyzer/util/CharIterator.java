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
package org.eobjects.analyzer.util;

import java.util.ListIterator;

/**
 * An iterator (with additional helper methods) for characters. The iterator
 * does not support the add(...) method.
 * 
 * 
 */
public class CharIterator implements ListIterator<Character> {

	private char[] _chars;
	private int _index = -1;
	private int _length;

	public CharIterator(CharSequence charSequence) {
		if (charSequence == null) {
			_chars = new char[0];
		} else {
			_chars = charSequence.toString().toCharArray();
		}
		_length = _chars.length;
	}

	public CharIterator(char[] chars) {
		if (chars == null) {
			_chars = new char[0];
		} else {
			_chars = chars;
		}
		_length = _chars.length;
	}

	public void reset() {
		_index = -1;
	}

	public Character first() {
		_index = 0;
		return current();
	}

	public Character last() {
		_index = _length - 1;
		return current();
	}

	public CharIterator subIterator(int fromIndex, int toIndex) {
		int length = toIndex - fromIndex;

		assert length > 0;

		char[] chars = new char[length];
		System.arraycopy(_chars, fromIndex, chars, 0, length);
		return new CharIterator(chars);
	}

	public boolean is(Character c) {
		if (c == null) {
			return false;
		}
		return c.charValue() == current();
	}

	public boolean isLetter() {
		return Character.isLetter(current());
	}

	public boolean isDigit() {
		return Character.isDigit(current());
	}

	public boolean isWhitespace() {
		return Character.isWhitespace(current());
	}

	public boolean isUpperCase() {
		return Character.isUpperCase(current());
	}

	public boolean isLowerCase() {
		return Character.isLowerCase(current());
	}

	public boolean isDiacritic() {
		return StringUtils.isDiacritic(current());
	}

	@Override
	public boolean hasNext() {
		return _index + 1 < _length;
	}

	@Override
	public Character next() {
		_index++;
		return current();
	}

	public int currentIndex() {
		return _index;
	}

	public char current() {
		return _chars[_index];
	}

	@Override
	public boolean hasPrevious() {
		return _index > 0;
	}

	@Override
	public Character previous() {
		_index--;
		return current();
	}

	@Override
	public int nextIndex() {
		return _index + 1;
	}

	@Override
	public int previousIndex() {
		return _index - 1;
	}

	@Override
	public void remove() {
		// algorythm adapted from ArrayList.remove(int)
		int numMoved = length() - _index - 1;
		if (numMoved > 0) {
			System.arraycopy(_chars, _index + 1, _chars, _index, numMoved);
		}
		_index--;
		_length--;
	}

	@Override
	public void set(Character e) {
		_chars[_index] = e;
	}

	@Override
	public void add(Character e) {
		throw new UnsupportedOperationException("CharIterator does not support add(...)");
	}

	@Override
	public String toString() {
		int length = length();
		if (length != _chars.length) {
			char[] tmp = _chars;
			_chars = new char[length];
			System.arraycopy(tmp, 0, _chars, 0, length);
		}
		return new String(_chars);
	}

	public int length() {
		return _length;
	}
}
