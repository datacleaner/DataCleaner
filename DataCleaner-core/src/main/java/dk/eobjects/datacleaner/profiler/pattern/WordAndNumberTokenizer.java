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

import java.util.ArrayList;
import java.util.List;

public class WordAndNumberTokenizer implements ITokenizer {

	public Token[] tokenize(String string) {
		char[] chars = string.toCharArray();

		List<Token> result = new ArrayList<Token>();
		List<Character> currentChars = new ArrayList<Character>();

		// Indicates if the current token is a delimitor token.
		boolean isTokenDelim = false;

		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			boolean isCharLetterOrDigit = Character.isLetterOrDigit(c);
			if (i == 0) {
				isTokenDelim = !isCharLetterOrDigit;
				currentChars.add(c);
			} else {

				if (isCharLetterOrDigit) {
					if (!isTokenDelim) {
						currentChars.add(c);
					} else {
						result.add(createToken(currentChars));
						currentChars = new ArrayList<Character>();
						currentChars.add(c);
						isTokenDelim = !isTokenDelim;
					}
				} else {
					if (isTokenDelim) {
						currentChars.add(c);
					} else {
						result.add(createToken(currentChars));
						currentChars = new ArrayList<Character>();
						currentChars.add(c);
						isTokenDelim = !isTokenDelim;
					}
				}
			}
			if (i == chars.length - 1) {
				result.add(createToken(currentChars));
				currentChars = null;
			}
		}

		return result.toArray(new Token[result.size()]);
	}

	private Token createToken(List<Character> chars) {
		char[] charArray = new char[chars.size()];
		for (int i = 0; i < chars.size(); i++) {
			charArray[i] = chars.get(i);
		}
		return new Token(charArray);
	}

}