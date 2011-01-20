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
package org.eobjects.datacleaner.user;

import java.io.File;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl;
import org.eobjects.analyzer.configuration.JaxbConfigurationReader;

/**
 * Single point of reference for the AnalyzerBeans configuration used in
 * DataCleaner.
 * 
 * @author Kasper Sørensen
 */
public final class DCConfiguration {

	static {
		// load the configuration file
		JaxbConfigurationReader configurationReader = new JaxbConfigurationReader();
		AnalyzerBeansConfiguration c = configurationReader.create(new File(DataCleanerHome.get(), "conf.xml"));

		// make the configuration mutable
		MutableDatastoreCatalog datastoreCatalog = new MutableDatastoreCatalog(c.getDatastoreCatalog());
		MutableReferenceDataCatalog referenceDataCatalog = new MutableReferenceDataCatalog(c.getReferenceDataCatalog(),
				datastoreCatalog);
		configuration = new AnalyzerBeansConfigurationImpl(datastoreCatalog, referenceDataCatalog,
				c.getDescriptorProvider(), c.getTaskRunner(), c.getStorageProvider());
	}

	private static final AnalyzerBeansConfiguration configuration;

	private DCConfiguration() {
		// prevent instantiation
	}

	public static AnalyzerBeansConfiguration get() {
		return configuration;
	}
}
