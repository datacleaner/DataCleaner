/**
 *  This file is part of DataCleaner.
 *
 *  DataCleaner is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DataCleaner is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DataCleaner.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.eobjects.datacleaner.gui.setup;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.FileSystemResource;

import dk.eobjects.datacleaner.gui.DataCleanerGui;
import dk.eobjects.datacleaner.gui.GuiHelper;
import dk.eobjects.datacleaner.gui.model.DatabaseDriver;
import dk.eobjects.datacleaner.gui.model.NamedConnection;
import dk.eobjects.datacleaner.gui.panels.ConfigurationPanelManager;
import dk.eobjects.datacleaner.profiler.IProfileDescriptor;
import dk.eobjects.datacleaner.profiler.ProfilerManager;
import dk.eobjects.datacleaner.validator.IValidationRuleDescriptor;
import dk.eobjects.datacleaner.validator.ValidatorManager;

/**
 * The configuration represents the content of the configuration files, that are
 * readable and writable externally of DataCleaner. Thus the configuration is
 * non-modifiable during runtime, in contrast to the preferences which can be
 * modified and saved during runtime.
 */
public class GuiConfiguration {

	private static Log _log = LogFactory.getLog(GuiConfiguration.class);
	private static XmlBeanFactory _beanFactory;
	private static File _dataCleanerHome;

	/**
	 * Prevent instantiation
	 */
	private GuiConfiguration() {
	}

	/**
	 * Inject a DATACLEANER_HOME path (for testing purposes)
	 */
	public static void setDataCleanerHome(File dataCleanerHome) {
		_dataCleanerHome = dataCleanerHome;
	}

	public static File getDataCleanerHome() {
		if (_dataCleanerHome == null) {
			// Look for the DATACLEANER_HOME environment variable
			String dataCleanerHomePath = System.getenv("DATACLEANER_HOME");
			if (dataCleanerHomePath == null) {
				// If no DATACLEANER_HOME environment variable is set, use the
				// users home directory: ~/.datacleaner/${version}
				String userHomePath = System.getProperty("user.home");
				if (userHomePath == null) {
					_log.fatal("User home non existing: " + userHomePath);
					System.exit(DataCleanerGui.EXIT_CODE_COULD_NOT_OPEN_CONFIGURATION_FILE);
				}
				_dataCleanerHome = new File(userHomePath + File.separatorChar + ".datacleaner" + File.separatorChar
						+ DataCleanerGui.VERSION);
			} else {
				_dataCleanerHome = new File(dataCleanerHomePath);
			}

			// Fill in standard configuration, if no configuration exists
			if (!_dataCleanerHome.exists()) {
				_log.info("Creating new DATACLEANER_HOME: " + _dataCleanerHome.getAbsolutePath());

				if (!_dataCleanerHome.mkdirs()) {
					_log.warn("Could not create DATACLEANER_HOME: " + _dataCleanerHome.getAbsolutePath());
				}
				copyConfigurationFilesToDataCleanerHome();
			}
		}
		return _dataCleanerHome;
	}

	private static void copyConfigurationFilesToDataCleanerHome() {
		try {
			GuiHelper.copyDirectoryContentsFromClasspathToFileSystem("datacleaner-userhome", _dataCleanerHome);
		} catch (IOException e) {
			_log.fatal("Could not write configuration files to file system", e);
			System.exit(DataCleanerGui.EXIT_CODE_COULD_NOT_OPEN_CONFIGURATION_FILE);
		}
	}

	public static void initialize(File configurationFile) {
		_log.info("Reading configuration file: " + configurationFile);
		FileSystemResource fileSystemResource = new FileSystemResource(configurationFile);
		if (fileSystemResource.exists()) {
			_beanFactory = new XmlBeanFactory(fileSystemResource);
			Collection<IProfileDescriptor> pd = getBeansOfClass(IProfileDescriptor.class);
			ProfilerManager.setProfileDescriptors(new ArrayList<IProfileDescriptor>(pd));

			Collection<IValidationRuleDescriptor> vrd = getBeansOfClass(IValidationRuleDescriptor.class);
			ValidatorManager.setValidationRuleDescriptors(new ArrayList<IValidationRuleDescriptor>(vrd));
		} else {
			_log.fatal("Could not open configuration file: " + configurationFile);
			System.exit(DataCleanerGui.EXIT_CODE_COULD_NOT_OPEN_CONFIGURATION_FILE);
		}
	}

	public static void initialize() {
		File mainConfigurationFile = getDataCleanerFile("datacleaner-config.xml");
		if (!mainConfigurationFile.exists()) {
			copyConfigurationFilesToDataCleanerHome();
		}
		initialize(mainConfigurationFile);
	}

	public static File getDataCleanerFile(String filename) {
		File dataCleanerHome = getDataCleanerHome();
		String configurationFilePath = dataCleanerHome.getAbsolutePath() + File.separator + filename;
		return new File(configurationFilePath);
	}

	@SuppressWarnings("unchecked")
	protected static <E extends Object> Collection<E> getBeansOfClass(Class<E> clazz) {
		Map beansOfType = _beanFactory.getBeansOfType(clazz);
		return beansOfType.values();
	}

	public static Collection<DatabaseDriver> getDatabaseDrivers() {
		return getBeansOfClass(DatabaseDriver.class);
	}

	public static Collection<NamedConnection> getNamedConnections() {
		return getBeansOfClass(NamedConnection.class);
	}

	public static ConfigurationPanelManager getConfigurationPanelManager() {
		ConfigurationPanelManager manager = (ConfigurationPanelManager) _beanFactory
				.getBean("configurationPanelManager");
		if (manager == null) {
			_log.warn("No bean named 'configurationPanelManager' found in configuration. Creating default manager.");
			manager = new ConfigurationPanelManager();
		}
		return manager;
	}

	public static NamedConnection getNamedConnection(String namedConnectionName) {
		if (namedConnectionName != null) {
			Collection<NamedConnection> namedConnections = getNamedConnections();
			for (NamedConnection namedConnection : namedConnections) {
				if (namedConnectionName.equals(namedConnection.getName())) {
					return namedConnection;
				}
			}
		}
		return null;
	}
}