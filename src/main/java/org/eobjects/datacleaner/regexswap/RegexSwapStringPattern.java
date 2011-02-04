package org.eobjects.datacleaner.regexswap;

import org.eobjects.analyzer.reference.RegexStringPattern;
import org.eobjects.analyzer.reference.StringPattern;

/**
 * A specialized type of string pattern, based on a regex downloaded from the
 * regex swap
 * 
 * @author Kasper Sørensen
 * 
 */
public class RegexSwapStringPattern implements StringPattern {

	private static final long serialVersionUID = 1L;
	private final Regex _regex;
	private transient RegexStringPattern _delegate;

	public RegexSwapStringPattern(Regex regex) {
		_regex = regex;
	}

	@Override
	public String getName() {
		return _regex.getName();
	}

	@Override
	public boolean matches(String string) {
		if (_delegate == null) {
			_delegate = new RegexStringPattern(getName(), _regex.getExpression(), true);
		}
		return _delegate.matches(string);
	}

	public Regex getRegex() {
		return _regex;
	}
}
