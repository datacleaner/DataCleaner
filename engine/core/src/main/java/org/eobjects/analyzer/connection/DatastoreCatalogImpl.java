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
package org.eobjects.analyzer.connection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.metamodel.util.CollectionUtils;
import org.apache.metamodel.util.HasNameMapper;

public class DatastoreCatalogImpl implements DatastoreCatalog {

	private static final long serialVersionUID = 1L;

	private final Collection<Datastore> _datastores;

	public DatastoreCatalogImpl(Collection<Datastore> datastores) {
		if (datastores == null) {
			throw new IllegalArgumentException("datastores cannot be null");
		}
		_datastores = datastores;
	}

	public DatastoreCatalogImpl(Datastore... datastores) {
		_datastores = new ArrayList<Datastore>();
		for (Datastore datastore : datastores) {
			_datastores.add(datastore);
		}
	}

	@Override
	public String[] getDatastoreNames() {
		List<String> names = CollectionUtils.map(_datastores, new HasNameMapper());
		Collections.sort(names);
		return names.toArray(new String[names.size()]);
	}

	@Override
	public Datastore getDatastore(String name) {
		if (name != null) {
			for (Datastore ds : _datastores) {
				if (name.equals(ds.getName())) {
					return ds;
				}
			}
		}
		return null;
	}
}
