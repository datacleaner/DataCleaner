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
package org.eobjects.analyzer.reference;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

import org.eobjects.analyzer.util.ReadObjectBuilder;
import org.eobjects.analyzer.util.ReadObjectBuilder.Moved;
import org.apache.metamodel.util.BaseObject;

public abstract class AbstractReferenceData extends BaseObject implements ReferenceData {

	private static final long serialVersionUID = 1L;

	@Moved
	private final String _name;

	private String _description;

	public AbstractReferenceData(String name) {
		_name = name;
	}

	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
		ReadObjectBuilder.create(this, AbstractReferenceData.class).readObject(stream);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String getDescription() {
		return _description;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String getName() {
		return _name;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void setDescription(String description) {
		_description = description;
	}

	@Override
	protected void decorateIdentity(List<Object> identifiers) {
		identifiers.add(getName());
		identifiers.add(getDescription());
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[name=" + getName() + "]";
	}
}
