/**
 * AnalyzerBeans
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

import java.io.Serializable;

/**
 * Represents a symbol/element in a token pattern. If for example the pattern is
 * "aaa@aaa.aa", then there will be 5 symbols:
 * <ul>
 * <li>aaa</li>
 * <li>@</li>
 * <li>aaa</li>
 * <li>.</li>
 * <li>aa</li>
 * </ul>
 * 
 * The token pattern symbol is different from a pattern in the way that it is
 * more abstract. A symbol will not retain the concrete values of most tokens.
 * Thus the information stored in a symbol will often be limited to:
 * 
 * <ul>
 * <li>The TokenType</li>
 * <li>The length of the symbol</li>
 * <li>Metadata about the symbol such as: Is it a negativ number, is it
 * uppercase, does it contain decimals etc.</li>
 * </ul>
 * 
 * @see Token
 * 
 * 
 */
public interface TokenPatternSymbol extends Serializable {

	public String toSymbolicString();

	public TokenType getTokenType();

	public boolean isUpperCaseOnly();

	public boolean isLowerCaseOnly();

	public boolean isDecimal();

	public boolean isNegative();

	public boolean matches(Token token, TokenizerConfiguration configuration);

	public int length();

	public void expandLenght(int amount);

	public boolean isExpandable();
}
