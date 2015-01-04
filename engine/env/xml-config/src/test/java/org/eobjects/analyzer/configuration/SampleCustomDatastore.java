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
package org.eobjects.analyzer.configuration;

import java.io.File;

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreConnection;
import org.eobjects.analyzer.connection.DatastoreConnectionImpl;
import org.eobjects.analyzer.connection.PerformanceCharacteristics;
import org.eobjects.analyzer.connection.PerformanceCharacteristicsImpl;
import org.apache.metamodel.DataContext;
import org.apache.metamodel.DataContextFactory;
import org.junit.Ignore;

@Ignore
public class SampleCustomDatastore implements Datastore {

	private static final long serialVersionUID = 1L;

	@Configured
	String name;

	@Configured
	File xmlFile;

	@Configured
	String description;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public DatastoreConnection openConnection() {
		DataContext dc = DataContextFactory.createXmlDataContext(xmlFile, false);
		return new DatastoreConnectionImpl<DataContext>(dc, this);
	}

	@Override
	public PerformanceCharacteristics getPerformanceCharacteristics() {
		return new PerformanceCharacteristicsImpl(true, true);
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}
}
