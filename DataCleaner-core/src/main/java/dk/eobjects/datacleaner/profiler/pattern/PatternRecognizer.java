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

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * The PatternRecognizer is a responsible for collecting incoming values,
 * tokenize them and aggregate and distribute them in humanly readable
 * string-patterns.
 * 
 * The patterns used serve the same purpose as regular expressions but are a lot
 * easier to read but also quite simplified compared to regexes.
 * 
 * @see WordAndNumberTokenizer
 * @see PatternFinderProfile
 */
public class PatternRecognizer {

	private ITokenizer _tokenizer = new WordAndNumberTokenizer();
	private Map<String, PatternDefinition> _patternMap = new TreeMap<String, PatternDefinition>();

	/**
	 * Collect/aggregate a value
	 * 
	 * @param string
	 *            the value
	 * @param count
	 *            the number of occurrances (increment-value) of this value.
	 */
	public void addInstance(String string, long count) {
		Token[] tokens = _tokenizer.tokenize(string);
		String patternMapKey = toPatternKey(tokens);

		PatternDefinition patternDefinition = _patternMap.get(patternMapKey);
		if (patternDefinition == null) {
			patternDefinition = new PatternDefinition();
			_patternMap.put(patternMapKey, patternDefinition);
		}
		patternDefinition.addInstanceData(tokens, count);
	}

	/**
	 * Creates a unique string representing this pattern's token-composition,
	 * but not the tokens themselves, ie. WORD, NUMBER and MIXED tokens are only
	 * given a single character to support varying sizes. This means that "John
	 * 123", "Kasper 123" and "Frederick 123" are all converted to the same
	 * pattern key: "a 9".
	 */
	private static String toPatternKey(Token[] tokens) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < tokens.length; i++) {
			Token token = tokens[i];
			short type = token.getType();
			if (type == Token.TYPE_DELIM) {
				/**
				 * TODO: Ticket #202: have an option to ignore repeating spaces
				 * in delimitor tokens
				 * 
				 * @see http://eobjects.org/trac/ticket/202
				 */
				sb.append(token.toString());
			} else {
				sb.append(type);
			}
		}
		return sb.toString();
	}

	/**
	 * Retrieves a map of identified patterns and their occurences counts.
	 */
	public Map<String, Long> identifyPatterns() {
		Map<String, Long> result = new LinkedHashMap<String, Long>();

		Collection<PatternDefinition> values = _patternMap.values();
		for (PatternDefinition patternDefinition : values) {
			result.put(patternDefinition.toString(), patternDefinition
					.getCount());
		}
		return result;
	}

	/**
	 * @param patternName
	 * @param value
	 * @return true if the value is among the instances that produced the
	 *         patternName
	 */
	public boolean patternEquals(String patternName, String value) {
		Token[] tokens = _tokenizer.tokenize(value);
		String patternMapKey = toPatternKey(tokens);
		PatternDefinition patternDefinition = _patternMap.get(patternMapKey);
		if (patternDefinition != null) {
			return patternDefinition.toString().equals(patternName);
		}
		return false;
	}

	public Pattern getRegex(String patternName) {
		if (patternName != null) {
			Collection<PatternDefinition> values = _patternMap.values();
			for (PatternDefinition patternDefinition : values) {
				if (patternName.equals(patternDefinition.toString())) {
					String regex = patternDefinition.toRegex();
					return Pattern.compile(regex);
				}
			}
		}
		return null;
	}

	/**
	 * Convience class that holds the a pattern where each token has been
	 * converted/serialized as a String. These strings can grow in size so that
	 * the same pattern can hold tokens of varying sizes, eg. "John", "Kasper",
	 * "Frederick" etc.
	 * 
	 * Furthermore the pattern definition holds a simple counter to increment on
	 * each added observation
	 */
	private static class PatternDefinition {

		private long _count = 0l;
		private short[] _tokenTypes = null;
		private int[] _tokenLengths = null;
		private String[] _delimTokens = null;

		public void addInstanceData(Token[] tokens, long count) {
			if (_tokenTypes == null && _tokenLengths == null) {
				_tokenTypes = new short[tokens.length];
				_tokenLengths = new int[tokens.length];
				_delimTokens = new String[tokens.length];
				for (int i = 0; i < tokens.length; i++) {
					short type = tokens[i].getType();
					_tokenTypes[i] = type;
					if (type == Token.TYPE_DELIM) {
						_delimTokens[i] = tokens[i].toString();
					}
				}
			}

			_count += count;
			for (int i = 0; i < tokens.length; i++) {
				int length = tokens[i].getLength();
				if (length > _tokenLengths[i]) {
					_tokenLengths[i] = length;
				}
			}
		}

		private String createSymbol(short tokenType, int length) {
			switch (tokenType) {
			case Token.TYPE_MIXED:
				return createSymbol('?', length);
			case Token.TYPE_NUMBER:
				return createSymbol('9', length);
			case Token.TYPE_WORD:
				return createSymbol('a', length);
			default:
				throw new IllegalArgumentException(
						"Token had unsupported type.");
			}
		}

		private String createSymbol(char repeatedChar, int length) {
			char[] result = new char[length];
			Arrays.fill(result, repeatedChar);
			return new String(result);
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < _tokenTypes.length; i++) {
				short type = _tokenTypes[i];
				if (type == Token.TYPE_DELIM) {
					sb.append(_delimTokens[i]);
				} else {
					sb.append(createSymbol(_tokenTypes[i], _tokenLengths[i]));
				}
			}
			return sb.toString();
		}

		public String toRegex() {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < _tokenTypes.length; i++) {
				short type = _tokenTypes[i];
				if (type == Token.TYPE_DELIM) {
					sb.append(new Token(_delimTokens[i].toCharArray())
							.toRegex());
				} else {
					sb.append(new Token(type, _tokenLengths[i]).toRegex());
				}
			}
			return sb.toString();
		}

		public long getCount() {
			return _count;
		}
	}
}