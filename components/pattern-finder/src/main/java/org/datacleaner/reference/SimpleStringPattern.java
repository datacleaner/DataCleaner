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
package org.datacleaner.reference;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

import org.datacleaner.beans.stringpattern.DefaultTokenizer;
import org.datacleaner.beans.stringpattern.Token;
import org.datacleaner.beans.stringpattern.TokenPattern;
import org.datacleaner.beans.stringpattern.TokenPatternImpl;
import org.datacleaner.beans.stringpattern.Tokenizer;
import org.datacleaner.beans.stringpattern.TokenizerConfiguration;
import org.datacleaner.util.LabelUtils;
import org.datacleaner.util.ReadObjectBuilder;

/**
 * Represents a string pattern that is based on a sequence of token types. The
 * pattern format is similar to the one used by the Pattern finder analyzer,
 * which makes it ideal for reusing discovered patterns.
 * 
 * @see TokenPattern
 * 
 * 
 */
public final class SimpleStringPattern extends AbstractReferenceData implements StringPattern {

	private static final long serialVersionUID = 1L;
	private final String _expression;
	private transient TokenPattern _tokenPattern;
	private transient DefaultTokenizer _tokenizer;
	private transient TokenizerConfiguration _configuration;
	
	public SimpleStringPattern(String name, String expression) {
	    this(name, expression, new TokenizerConfiguration());
	}

	public SimpleStringPattern(String name, String expression, TokenizerConfiguration configuration) {
		super(name);
		_expression = expression;
		_configuration = configuration;
	}

	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
		ReadObjectBuilder.create(this, SimpleStringPattern.class).readObject(stream);
	}

	@Override
	protected void decorateIdentity(List<Object> identifiers) {
		super.decorateIdentity(identifiers);
		identifiers.add(_expression);
	}

	private Tokenizer getTokenizer() {
		if (_tokenizer == null) {
			_tokenizer = new DefaultTokenizer(getConfiguration());
		}
		return _tokenizer;
	}

	private TokenizerConfiguration getConfiguration() {
		if (_configuration == null) {
			// TODO: Ideally we should provide all the configuration options in
			// the constructor
			_configuration = new TokenizerConfiguration();
		}
		return _configuration;
	}

	private TokenPattern getTokenPattern() {
		if (_tokenPattern == null) {
		    final String expression;
		    if (LabelUtils.NULL_LABEL.equals(_expression)) {
		        expression = null;
		    } else if (LabelUtils.BLANK_LABEL.equals(_expression)) {
                expression = "";
		    } else {
                expression = _expression;
		    }
		    
            List<Token> tokens = getTokenizer().tokenize(expression);
			_tokenPattern = new TokenPatternImpl(expression, tokens, getConfiguration());
		}
		return _tokenPattern;
	}

	public String getExpression() {
		return _expression;
	}

	@Override
	public boolean matches(String string) {
		List<Token> tokens = getTokenizer().tokenize(string);
		return getTokenPattern().match(tokens);
	}

	@Override
	public String toString() {
		return "SimpleStringPattern[name=" + getName() + ", expression=" + _expression + "]";
	}
}
