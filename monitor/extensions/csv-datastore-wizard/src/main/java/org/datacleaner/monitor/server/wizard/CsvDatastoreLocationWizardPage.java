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
package org.datacleaner.monitor.server.wizard;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.FileResource;
import org.apache.metamodel.util.Resource;
import org.datacleaner.monitor.shared.model.DCUserInputException;
import org.datacleaner.monitor.wizard.WizardContext;
import org.datacleaner.monitor.wizard.WizardPageController;
import org.datacleaner.monitor.wizard.common.AbstractFreemarkerWizardPage;
import org.datacleaner.repository.RepositoryFolder;
import org.datacleaner.repository.file.FileRepositoryFolder;
import org.datacleaner.server.EnvironmentBasedHadoopClusterInformation;
import org.datacleaner.spark.utils.HadoopUtils;
import org.datacleaner.util.HadoopResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Page where user selects which folder the file should be located in
 */
public abstract class CsvDatastoreLocationWizardPage extends AbstractFreemarkerWizardPage {

    private static Logger logger = LoggerFactory.getLogger(CsvDatastoreLocationWizardPage.class);

    private final WizardContext _wizardContext;
    private final String _filename;
    private final boolean _newFile;

    public CsvDatastoreLocationWizardPage(WizardContext wizardContext, String filename, boolean newFile) {
        _wizardContext = wizardContext;
        _filename = filename;
        _newFile = newFile;
    }

    @Override
    public Integer getPageIndex() {
        return 1;
    }

    @Override
    protected String getTemplateFilename() {
        return "CsvDatastoreLocationWizardPage.html";
    }

    @Override
    protected Map<String, Object> getFormModel() {
        final String absolutePrefix = File.listRoots()[0].getAbsolutePath() + "data" + File.separatorChar;

        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("filename", _filename);
        map.put("absolutePrefix", absolutePrefix);

        if (_newFile) {
            map.put("introductionText", "What should be the server location of the CSV file:");
            map.put("repositoryText", "Copy it to a location in the repository:");
            map.put("absoluteText", "Copy it to an absolute location on the server:");
        } else {
            map.put("introductionText", "What is the server location of the CSV file:");
            map.put("repositoryText", "It's located in the repository:");
            map.put("absoluteText", "It's at an absolute location on the server:");
            map.put("relativePrefix", "/datacleaner/");
            map.put("relativeHadoopText", "It's a path on the Hadoop cluster:");
        }

        return map;
    }

    @Override
    public WizardPageController nextPageController(Map<String, List<String>> formParameters)
            throws DCUserInputException {
        final List<String> locations = formParameters.get("location");
        if (locations == null || locations.isEmpty()) {
            throw new DCUserInputException("Please select a location for the CSV file");
        }

        final String location = locations.get(0);
        final Resource resource;
        if ("repository".equals(location)) {
            final String filepath = formParameters.get("filepath_repository").get(0);
            final RepositoryFolder tenantFolder = _wizardContext.getTenantContext().getTenantRootFolder();
            if (!(tenantFolder instanceof FileRepositoryFolder)) {
                throw new DCUserInputException("Your repository type is not support for hosting raw data files");
            }

            final FileRepositoryFolder fileRepositoryFolder = (FileRepositoryFolder) tenantFolder;

            final File file = new File(fileRepositoryFolder.getFile(), filepath);
            resource = new FileResource(file);
        } else if ("absolute".equals(location)) {
            final String filepath = formParameters.get("filepath_absolute").get(0);
            final File file = new File(filepath);
            resource = new FileResource(file);
        } else if ("relativeHadoop".equals(location)) {
            final String path = formParameters.get("filepath_relative_hadoop").get(0);
            String uri;
            try {
                uri = HadoopUtils.getFileSystem().getUri().resolve(path).toString();
            } catch (IOException e) {
                throw new DCUserInputException("The Hadoop path does not exist");
            }
            final EnvironmentBasedHadoopClusterInformation environmentBasedHadoopClusterInformation = new EnvironmentBasedHadoopClusterInformation(
                    "default", HadoopResource.DEFAULT_CLUSTERREFERENCE);
            if (!EnvironmentBasedHadoopClusterInformation.isConfigurationDirectoriesSpecified()) {
                throw new DCUserInputException("HADOOP_CONF_DIR or/and SPARK_CONF_DIR are not defined");
            }

            logger.debug("Environment variable is", environmentBasedHadoopClusterInformation.getDescription());
            resource = new HadoopResource(uri, environmentBasedHadoopClusterInformation.getConfiguration(),
                    HadoopResource.DEFAULT_CLUSTERREFERENCE);
        } else {
            throw new IllegalArgumentException("Invalid location value: " + location);
        }

        logger.info("The resource path is  " + resource.getQualifiedPath());
        return nextPageController(resource);
    }

    /**
     * Invoked when the user has selected a file location on the server of the
     * CSV file.
     * 
     * @param filepath
     * @param resource
     * @return
     */
    protected abstract WizardPageController nextPageController(Resource resource);

}
