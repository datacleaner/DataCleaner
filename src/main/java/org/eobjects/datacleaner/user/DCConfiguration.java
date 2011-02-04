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
import org.eobjects.analyzer.connection.DatastoreCatalogImpl;
import org.eobjects.analyzer.descriptors.SimpleDescriptorProvider;
import org.eobjects.analyzer.job.concurrent.SingleThreadedTaskRunner;
import org.eobjects.analyzer.reference.ReferenceDataCatalogImpl;
import org.eobjects.analyzer.storage.InMemoryStorageProvider;
import org.eobjects.datacleaner.util.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Single point of reference for the AnalyzerBeans configuration used in
 * DataCleaner.
 * 
 * @author Kasper Sørensen
 */
public final class DCConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(DCConfiguration.class);

	static {
		// load the configuration file
		JaxbConfigurationReader configurationReader = new JaxbConfigurationReader();

		AnalyzerBeansConfiguration c;
		try {
			File file = new File(DataCleanerHome.get(), "conf.xml");
			c = configurationReader.create(file);
			logger.info("Succesfully read configuration from {}", file.getAbsolutePath());
		} catch (Exception ex1) {
			logger.warn("Unexpected error while reading conf.xml from DataCleanerHome!", ex1);
			try {
				c = configurationReader.create(ResourceManager.getInstance().getUrl("datacleaner-home/conf.xml")
						.openStream());
			} catch (Exception ex2) {
				logger.warn("Unexpected error while reading conf.xml from classpath!", ex2);
				logger.warn("Creating a bare-minimum configuration because of previous errors!");
				c = new AnalyzerBeansConfigurationImpl(new DatastoreCatalogImpl(), new ReferenceDataCatalogImpl(),
						new SimpleDescriptorProvider(), new SingleThreadedTaskRunner(), new InMemoryStorageProvider());
			}
		}

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
		logger.debug("get()");
		return configuration;
	}
}
