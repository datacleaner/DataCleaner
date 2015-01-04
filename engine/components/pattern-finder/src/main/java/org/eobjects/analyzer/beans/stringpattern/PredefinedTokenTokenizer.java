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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PredefinedTokenTokenizer implements Tokenizer {

	private List<PredefinedTokenDefinition> _predefinedTokenDefitions;

	public PredefinedTokenTokenizer(PredefinedTokenDefinition... predefinedTokenDefinitions) {
		_predefinedTokenDefitions = new LinkedList<PredefinedTokenDefinition>();
		for (PredefinedTokenDefinition predefinedToken : predefinedTokenDefinitions) {
			_predefinedTokenDefitions.add(predefinedToken);
		}
	}

	public PredefinedTokenTokenizer(List<PredefinedTokenDefinition> predefinedTokenDefinitions) {
		_predefinedTokenDefitions = predefinedTokenDefinitions;
	}

	/**
	 * Will only return either tokens with type PREDEFINED or UNDEFINED
	 */
	@Override
	public List<Token> tokenize(String s) {
		List<Token> result = new ArrayList<Token>();
		result.add(new UndefinedToken(s));

		for (PredefinedTokenDefinition predefinedTokenDefinition : _predefinedTokenDefitions) {
			Set<Pattern> patterns = predefinedTokenDefinition.getTokenRegexPatterns();
			for (Pattern pattern : patterns) {
				for (ListIterator<Token> it = result.listIterator(); it.hasNext();) {
					Token token = it.next();
					if (token instanceof UndefinedToken) {
						List<Token> replacementTokens = tokenizeInternal(token.getString(), predefinedTokenDefinition,
								pattern);
						if (replacementTokens.size() > 1) {
							it.remove();
							for (Token newToken : replacementTokens) {
								it.add(newToken);
							}
						}
					}
				}
			}
		}

		return result;
	}

	protected static List<Token> tokenizeInternal(String string, PredefinedTokenDefinition predefinedTokenDefinition,
			Pattern pattern) {
		LinkedList<Token> result = new LinkedList<Token>();
		result.add(new UndefinedToken(string));

		for (Matcher matcher = pattern.matcher(string); matcher.find(); matcher = pattern.matcher(string)) {

			int start = matcher.start();
			int end = matcher.end();

			result.removeLast();

			if (start > 0) {
				result.add(new UndefinedToken(string.substring(0, start)));
			}
			result.add(new PredefinedToken(predefinedTokenDefinition, string.substring(start, end)));

			if (end == string.length()) {
				break;
			}

			string = string.substring(end);
			result.add(new UndefinedToken(string));
		}

		return result;
	}
}
