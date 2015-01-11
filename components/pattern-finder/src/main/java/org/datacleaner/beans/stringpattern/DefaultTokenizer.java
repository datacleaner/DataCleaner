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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.datacleaner.util.CharIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultTokenizer implements Serializable, Tokenizer {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(DefaultTokenizer.class);

	private final TokenizerConfiguration _configuration;
	private final boolean _predefinedTokens;

	public DefaultTokenizer() {
		this(new TokenizerConfiguration());
	}

	public DefaultTokenizer(TokenizerConfiguration configuration) {
		if (configuration == null) {
			throw new NullPointerException("configuration argument cannot be null");
		}
		_configuration = configuration;

		final List<PredefinedTokenDefinition> predefinedTokens = _configuration.getPredefinedTokens();
		_predefinedTokens = !predefinedTokens.isEmpty() && _configuration.isTokenTypeEnabled(TokenType.PREDEFINED);

		if (_predefinedTokens) {
			logger.debug("Predefined tokens are turned ON, using PredefinedTokenTokenizer");
		} else {
			logger.debug("Predefined tokens are turned OFF, using tokenizeInternal");
		}
	}

	public List<Token> tokenize(String string) {
		if (string == null) {
			return Arrays.asList(NullToken.INSTANCE);
		}
		
		if ("".equals(string)) {
		    return Arrays.asList(BlankToken.INSTANCE);
		}

		List<Token> tokens;

		if (_predefinedTokens) {
			final List<PredefinedTokenDefinition> predefinedTokens = _configuration.getPredefinedTokens();
			PredefinedTokenTokenizer tokenizer = new PredefinedTokenTokenizer(predefinedTokens);
			tokens = tokenizer.tokenize(string);
			for (ListIterator<Token> it = tokens.listIterator(); it.hasNext();) {
				Token token = it.next();
				TokenType tokenType = token.getType();
				logger.debug("Next token type is: {}", tokenType);
				if (tokenType == TokenType.UNDEFINED) {
					List<SimpleToken> replacementTokens = tokenizeInternal(token.getString());
					boolean replace = true;
					if (replacementTokens.size() == 1) {
						if (token.equals(replacementTokens.get(0))) {
							replace = false;
						}
					}

					if (replace) {
						it.remove();
						for (SimpleToken replacementToken : replacementTokens) {
							it.add(replacementToken);
						}
					}
				}
			}
		} else {
			tokens = new ArrayList<Token>();
			tokens.addAll(tokenizeInternal(string));
		}

		return tokens;
	}

	private List<SimpleToken> tokenizeInternal(String string) {
		List<SimpleToken> tokens = preliminaryTokenize(string, _configuration);

		if (_configuration.isTokenTypeEnabled(TokenType.MIXED)) {
			tokens = flattenMixedTokens(tokens);
		}

		return tokens;
	}

	protected static List<SimpleToken> preliminaryTokenize(final String string, final TokenizerConfiguration configuration) {
		LinkedList<SimpleToken> result = new LinkedList<SimpleToken>();
		SimpleToken lastToken = null;

		CharIterator ci = new CharIterator(string);
		while (ci.hasNext()) {
			char c = ci.next();

			if (ci.is(configuration.getThousandsSeparator()) || ci.is(configuration.getDecimalSeparator())) {
				boolean treatAsSeparator = false;
				if (lastToken != null && lastToken.getType() == TokenType.NUMBER) {
					// there's a previous NUMBER token

					if (ci.hasNext()) {
						char next = ci.next();
						if (ci.isDigit()) {
							// the next token is also a NUMBER

							// now we're ready to assume that this is a
							// separator
							treatAsSeparator = true;
							lastToken = registerChar(result, lastToken, c, TokenType.NUMBER);
							lastToken = registerChar(result, lastToken, next, TokenType.NUMBER);
						} else {
							ci.previous();
						}
					}
				}

				if (!treatAsSeparator) {
					// the thousand separator is treated as a delim
					lastToken = registerChar(result, lastToken, c, TokenType.DELIM);
				}
			} else if (ci.is(configuration.getMinusSign())) {
				// the meaning of minus sign is dependent on the next token
				// (maybe it's the negative number operator)
				boolean treatAsMinus = false;

				if (lastToken == null || lastToken.getType() != TokenType.NUMBER) {
					if (ci.hasNext()) {
						char next = ci.next();
						if (ci.isDigit()) {
							// the minus sign was the number operator
							treatAsMinus = true;
							lastToken = registerChar(result, null, c, TokenType.NUMBER);
							lastToken = registerChar(result, lastToken, next, TokenType.NUMBER);
						} else {
							ci.previous();
						}
					}
				}

				if (!treatAsMinus) {
					// the minus sign is treated as a delim
					lastToken = registerChar(result, lastToken, c, TokenType.DELIM);
				}
			} else if (ci.isDigit()) {
				lastToken = registerChar(result, lastToken, c, TokenType.NUMBER);
			} else if (ci.isLetter()) {
				if (configuration.isDiscriminateTextCase()) {
					if (lastToken != null && lastToken.getType() == TokenType.TEXT) {
						// if we need to discriminate on case then we should
						// check the previous token and make sure that we only
						// append to that if they share the same case.
						char charFromPreviousToken = lastToken.getString().charAt(0);
						if (Character.isUpperCase(charFromPreviousToken) != Character.isUpperCase(c)) {
							lastToken = null;
						}
					}
				}
				lastToken = registerChar(result, lastToken, c, TokenType.TEXT);
			} else if (ci.isWhitespace()) {
				lastToken = registerChar(result, lastToken, c, TokenType.WHITESPACE);
			} else {
				lastToken = registerChar(result, lastToken, c, TokenType.DELIM);
			}
		}

		return result;
	}

	private static SimpleToken registerChar(List<SimpleToken> result, SimpleToken lastToken, char c, TokenType tokenType) {
		if (lastToken == null) {
			logger.debug("Creating new {} token", tokenType);
			lastToken = new SimpleToken(tokenType, c);
			result.add(lastToken);
		} else if (lastToken.getType() == tokenType) {
			logger.debug("Appending to previous token", tokenType);
			lastToken.appendChar(c);
		} else {
			logger.debug("Creating new {} token", tokenType);
			lastToken = new SimpleToken(tokenType, c);
			result.add(lastToken);
		}
		logger.debug("{} registered as {}", c, tokenType);
		return lastToken;
	}

	public static List<SimpleToken> flattenMixedTokens(List<SimpleToken> tokens) {
		SimpleToken previousToken = null;
		for (ListIterator<SimpleToken> it = tokens.listIterator(); it.hasNext();) {
			SimpleToken token = it.next();
			if (previousToken == null) {
				previousToken = token;
			} else {
				boolean mix = false;

				TokenType previousType = previousToken.getType();
				TokenType currentType = token.getType();
				if (previousType != currentType) {
					if (isMixedCandidate(previousType) && isMixedCandidate(currentType)) {
						mix = true;
						previousToken.appendString(token.getString());
						previousToken.setType(TokenType.MIXED);
						it.remove();
					}
				}

				if (!mix) {
					previousToken = token;
				}
			}
		}
		return tokens;
	}

	private static boolean isMixedCandidate(TokenType type) {
		return type == TokenType.MIXED || type == TokenType.NUMBER || type == TokenType.TEXT;
	}
}
