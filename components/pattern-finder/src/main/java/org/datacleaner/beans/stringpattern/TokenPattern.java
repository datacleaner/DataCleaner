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
import java.util.List;

/**
 * Represents a pattern of token symbols that are matchable against a series of
 * concrete tokens.
 */
public interface TokenPattern extends Serializable {

	/**
	 * Attempts to match a list of tokens against this pattern. If it succeeds,
	 * true will be returned.
	 * 
	 * @param tokens
	 * @return true if the match was succesful, false otherwise.
	 */
	public boolean match(List<Token> tokens);

	public List<TokenPatternSymbol> getSymbols();

	public String toSymbolicString();
	
	public String getSampleString();
}
