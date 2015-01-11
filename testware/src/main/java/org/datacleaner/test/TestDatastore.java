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
package org.datacleaner.test;

import javax.sql.DataSource;

import org.datacleaner.connection.PerformanceCharacteristics;
import org.datacleaner.connection.UpdateableDatastore;
import org.datacleaner.connection.UpdateableDatastoreConnection;

final class TestDatastore implements UpdateableDatastore, PerformanceCharacteristics {

    private static final long serialVersionUID = 1L;
    private final String _name;
    private final DataSource _dataSource;
    private String _description;

    public TestDatastore(String name, DataSource dataSource) {
        _name = name;
        _dataSource = dataSource;
    }

    @Override
    public String getName() {
        return _name;
    }

    public DataSource getDataSource() {
        return _dataSource;
    }

    @Override
    public String getDescription() {
        return _description;
    }

    @Override
    public void setDescription(String description) {
        _description = description;
    }

    @Override
    public UpdateableDatastoreConnection openConnection() {
        try {
            return new TestDatastoreConnection(this);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
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
        return false;
    }
}
