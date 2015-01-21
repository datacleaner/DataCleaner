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
import java.util.Collection;
import java.util.List;

import org.datacleaner.util.ReadObjectBuilder;

/**
 * The simplest possible Dictionary implementation. Based on an in-memory set of
 * values.
 * 
 * 
 */
public final class SimpleDictionary extends AbstractReferenceData implements Dictionary {

	private static final long serialVersionUID = 1L;

	private final SimpleReferenceValues<String> _values;

	public SimpleDictionary(String name, String... values) {
		super(name);
		_values = new SimpleReferenceValues<String>(values);
	}

	public SimpleDictionary(String name, Collection<String> values) {
		super(name);
		_values = new SimpleReferenceValues<String>(values.toArray(new String[values.size()]));
	}

	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
		ReadObjectBuilder.create(this, SimpleDictionary.class).readObject(stream);
	}

	@Override
	protected void decorateIdentity(List<Object> identifiers) {
		super.decorateIdentity(identifiers);
		identifiers.add(_values);
	}

	@Override
	public ReferenceValues<String> getValues() {
		return _values;
	}

	@Override
	public boolean containsValue(String value) {
		return _values.containsValue(value);
	}

}
