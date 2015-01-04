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
package org.eobjects.analyzer.test.mock;

import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreConnection;
import org.eobjects.analyzer.connection.PerformanceCharacteristics;

public class MockDatastore implements Datastore, PerformanceCharacteristics {

	private static final long serialVersionUID = 1L;

	@Override
	public String getName() {
		return "MockDatastore";
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public void setDescription(String description) {
	}

	@Override
	public DatastoreConnection openConnection() {
		return new MockDatastoreConnection();
	}

	@Override
	public PerformanceCharacteristics getPerformanceCharacteristics() {
		return this;
	}

	@Override
	public boolean isQueryOptimizationPreferred() {
		return true;
	}

    @Override
    public boolean isNaturalRecordOrderConsistent() {
        return true;
    }
}
