/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.regexswap;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

import org.eobjects.analyzer.reference.AbstractReferenceData;
import org.eobjects.analyzer.reference.RegexStringPattern;
import org.eobjects.analyzer.reference.StringPattern;
import org.eobjects.analyzer.util.ReadObjectBuilder;

/**
 * A specialized type of string pattern, based on a regex downloaded from the
 * regex swap
 * 
 * @author Kasper Sørensen
 */
public final class RegexSwapStringPattern extends AbstractReferenceData implements StringPattern {

	private static final long serialVersionUID = 1L;
	private final Regex _regex;
	private transient RegexStringPattern _delegate;

	public RegexSwapStringPattern(Regex regex) {
		super(regex.getName());
		setDescription(regex.getDescription());
		_regex = regex;
	}

	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
		ReadObjectBuilder.create(this, RegexSwapStringPattern.class).readObject(stream);
	}

	@Override
	public String toString() {
		return "RegexSwapStringPattern[regex=" + _regex + "]";
	}

	@Override
	protected void decorateIdentity(List<Object> identifiers) {
		super.decorateIdentity(identifiers);
		identifiers.add(_regex);
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
