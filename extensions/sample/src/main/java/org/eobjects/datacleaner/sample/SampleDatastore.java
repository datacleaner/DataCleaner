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
package org.eobjects.datacleaner.sample;

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.connection.DatastoreConnectionImpl;
import org.eobjects.analyzer.connection.PerformanceCharacteristics;
import org.eobjects.analyzer.connection.PerformanceCharacteristicsImpl;
import org.eobjects.analyzer.connection.UsageAwareDatastore;
import org.eobjects.analyzer.connection.UsageAwareDatastoreConnection;
import org.eobjects.metamodel.DataContext;

public class SampleDatastore extends UsageAwareDatastore<DataContext> {

    private static final long serialVersionUID = 1L;

    @Configured
    String name;

    public SampleDatastore() {
        super(null);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public PerformanceCharacteristics getPerformanceCharacteristics() {
        return new PerformanceCharacteristicsImpl(false);
    }

    @Override
    protected UsageAwareDatastoreConnection<DataContext> createDatastoreConnection() {
        DataContext dataContext = new SampleDataContext();
        return new DatastoreConnectionImpl<DataContext>(dataContext, this);
    }

}
