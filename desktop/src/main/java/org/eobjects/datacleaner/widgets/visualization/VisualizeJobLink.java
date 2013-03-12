/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.widgets.visualization;

import java.util.List;

import org.eobjects.metamodel.util.BaseObject;

final class VisualizeJobLink extends BaseObject {

	private final Object _from;
	private final Object _to;

	public VisualizeJobLink(Object from, Object to) {
		_from = from;
		_to = to;
	}

	public Object getFrom() {
		return _from;
	}

	public Object getTo() {
		return _to;
	}

	@Override
	protected void decorateIdentity(List<Object> identifiers) {
		identifiers.add(_from);
		identifiers.add(_to);
	}

}
