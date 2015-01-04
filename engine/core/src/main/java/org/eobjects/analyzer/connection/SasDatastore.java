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

import java.io.File;

import org.apache.metamodel.DataContext;
import org.eobjects.metamodel.sas.metamodel.SasDataContext;

/**
 * Datastore implementation for directories with SAS datasets.
 */
public class SasDatastore extends UsageAwareDatastore<DataContext> implements FileDatastore {

	private static final long serialVersionUID = 1L;
	private final File _directory;

	public SasDatastore(String name, File directory) {
		super(name);
		_directory = directory;
	}

	@Override
	public PerformanceCharacteristics getPerformanceCharacteristics() {
		return new PerformanceCharacteristicsImpl(false, true);
	}

	@Override
	protected UsageAwareDatastoreConnection<DataContext> createDatastoreConnection() {
		DataContext dataContext = new SasDataContext(_directory);
		return new DatastoreConnectionImpl<DataContext>(dataContext, this);
	}

	@Override
	public String getFilename() {
		return _directory.getAbsolutePath();
	}

}
