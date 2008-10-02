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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.FileSystemResource;

import dk.eobjects.datacleaner.gui.DataCleanerGui;
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

	public static final String CONFIGURATION_FILE = "datacleaner-config.xml";
	private static Log _log = LogFactory.getLog(GuiConfiguration.class);
	private static XmlBeanFactory _beanFactory;

	/**
	 * Prevent instantiation
	 */
	private GuiConfiguration() {
	}

	public static void initialize(String configurationFile) {
		_log.info("Reading configuration file: " + configurationFile);
		FileSystemResource fileSystemResource = new FileSystemResource(
				configurationFile);
		if (fileSystemResource.exists()) {
			_beanFactory = new XmlBeanFactory(fileSystemResource);
			Collection<IProfileDescriptor> pd = getBeansOfClass(IProfileDescriptor.class);
			ProfilerManager
					.setProfileDescriptors(new ArrayList<IProfileDescriptor>(pd));

			Collection<IValidationRuleDescriptor> vrd = getBeansOfClass(IValidationRuleDescriptor.class);
			ValidatorManager
					.setValidationRuleDescriptors(new ArrayList<IValidationRuleDescriptor>(
							vrd));
		} else {
			_log.fatal("Could not open configuration file: "
					+ CONFIGURATION_FILE);
			System
					.exit(DataCleanerGui.EXIT_CODE_COULD_NOT_OPEN_CONFIGURATION_FILE);
		}
	}

	public static void initialize() {
		initialize(CONFIGURATION_FILE);
	}

	@SuppressWarnings("unchecked")
	public static <E extends Object> Collection<E> getBeansOfClass(
			Class<E> clazz) {
		Map beansOfType = _beanFactory.getBeansOfType(clazz);
		return beansOfType.values();
	}

	public static ConfigurationPanelManager getConfigurationPanelManager() {
		ConfigurationPanelManager manager = (ConfigurationPanelManager) _beanFactory
				.getBean("configurationPanelManager");
		if (manager == null) {
			_log
					.warn("No bean named 'configurationPanelManager' found in configuration. Creating default manager.");
			manager = new ConfigurationPanelManager();
		}
		return manager;
	}
}