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
package org.datacleaner.monitor.configuration;

import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBException;

import org.apache.metamodel.util.Action;
import org.apache.metamodel.util.Func;
import org.datacleaner.configuration.jaxb.Configuration;
import org.datacleaner.monitor.server.jaxb.AbstractJaxbAdaptor;
import org.datacleaner.repository.RepositoryFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Writes an updated conf.xml file to the repository. This is used by taking the
 * <datastore-catalog> and <reference-data-catalog> elements of the updated
 * conf.xml file, and replacing them in the existing conf.xml file.
 */
public class WriteUpdatedConfigurationFileAction extends AbstractJaxbAdaptor<Configuration>
        implements Action<OutputStream> {

    private static final Logger logger = LoggerFactory.getLogger(WriteUpdatedConfigurationFileAction.class);

    private final InputStream _updatedConfigurationInputStream;
    private final Configuration _existingConfiguration;

    public WriteUpdatedConfigurationFileAction(InputStream updatedConfigurationInputStream,
            RepositoryFile existingConfigurationFile) throws JAXBException {
        super(Configuration.class);
        _updatedConfigurationInputStream = updatedConfigurationInputStream;

        Configuration existingConfiguration;
        try {
            if (existingConfigurationFile == null) {
                existingConfiguration = null;
            } else {
                existingConfiguration = existingConfigurationFile.readFile(new Func<InputStream, Configuration>() {
                    @Override
                    public Configuration eval(InputStream in) {
                        return unmarshal(in);
                    }
                });
            }
        } catch (Exception e) {
            logger.warn("Failed to parse configuration file - treating it as invalid and will override.", e);
            existingConfiguration = null;
        }
        _existingConfiguration = existingConfiguration;
    }

    @Override
    public void run(OutputStream out) throws Exception {
        final Configuration updatedConfiguration = unmarshal(_updatedConfigurationInputStream);

        if (_existingConfiguration == null) {
            marshal(updatedConfiguration, out);
        } else {
            _existingConfiguration.setDatastoreCatalog(updatedConfiguration.getDatastoreCatalog());
            _existingConfiguration.setReferenceDataCatalog(_existingConfiguration.getReferenceDataCatalog());
            marshal(_existingConfiguration, out);
        }
    }
}
