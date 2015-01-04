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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class TokenPatternImpl implements TokenPattern {

	private static final long serialVersionUID = 1L;

	private final TokenizerConfiguration _configuration;
	private final List<TokenPatternSymbol> _symbols;
	private final String _sampleString;

	public TokenPatternImpl(String sampleString, List<Token> tokens, TokenizerConfiguration configuration) {
		if (tokens == null) {
			throw new IllegalArgumentException("tokens cannot be null");
		}
		_symbols = new ArrayList<TokenPatternSymbol>(tokens.size());
		for (Token token : tokens) {
			_symbols.add(new TokenPatternSymbolImpl(token, configuration));
		}
		_configuration = configuration;
		_sampleString = sampleString;
	}

	@Override
	public boolean match(List<Token> tokens) {
		if (_symbols.size() != tokens.size()) {
			return false;
		}

		Iterator<TokenPatternSymbol> it1 = _symbols.iterator();
		Iterator<Token> it2 = tokens.iterator();
		while (it1.hasNext()) {
			TokenPatternSymbol tokenSymbol = it1.next();
			Token token = it2.next();
			if (!tokenSymbol.matches(token, _configuration)) {
				return false;
			}
		}

		// it's a match. now expand sizes of tokens if needed
		it1 = _symbols.iterator();
		it2 = tokens.iterator();
		while (it1.hasNext()) {
			TokenPatternSymbol tokenSymbol = it1.next();
			Token token2 = it2.next();
			if (tokenSymbol.isExpandable()) {
				int length1 = tokenSymbol.length();
				int length2 = token2.length();
				if (length1 < length2) {
					int diff = length2 - length1;
					tokenSymbol.expandLenght(diff);
				}
			}
		}

		return true;
	}

	@Override
	public List<TokenPatternSymbol> getSymbols() {
		return Collections.unmodifiableList(_symbols);
	}

	@Override
	public String toSymbolicString() {
		StringBuilder sb = new StringBuilder();
		for (TokenPatternSymbol symbol : _symbols) {
			sb.append(symbol.toSymbolicString());
		}
		return sb.toString();
	}

	@Override
	public String getSampleString() {
		return _sampleString;
	}
}
