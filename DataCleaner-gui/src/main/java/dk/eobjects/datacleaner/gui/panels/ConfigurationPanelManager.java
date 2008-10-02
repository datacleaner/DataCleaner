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
package dk.eobjects.datacleaner.gui.panels;

import java.util.Map;

import dk.eobjects.datacleaner.profiler.IProfile;
import dk.eobjects.datacleaner.validator.IValidationRule;

/**
 * Class that manages various configuration panels for profiles and validation
 * rules. This class manages which panels should be used for which profiles and
 * validation rules and enables easy instantiation of these panels based on
 * instances or class names.
 */
public class ConfigurationPanelManager {

	private Class<? extends IConfigurationPanel> _defaultProfilerPanel = DefaultProfilerConfigurationPanel.class;
	private Class<? extends IConfigurationPanel> _defaultValidatorPanel = DefaultValidatorConfigurationPanel.class;
	private Map<Class<? extends IProfile>, Class<? extends IConfigurationPanel>> _profilerPanels;
	private Map<Class<? extends IValidationRule>, Class<? extends IConfigurationPanel>> _validatorPanels;

	public IConfigurationPanel getPanelForProfile(
			Class<? extends IProfile> profileClass) {
		Class<? extends IConfigurationPanel> panelClass = _defaultProfilerPanel;
		if (_profilerPanels != null
				&& _profilerPanels.containsKey(profileClass)) {
			panelClass = _profilerPanels.get(profileClass);
		}
		try {
			IConfigurationPanel configurationPanel = panelClass.newInstance();
			return configurationPanel;
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public IConfigurationPanel getPanelForValidationRule(
			Class<? extends IValidationRule> validationRuleClass) {
		Class<? extends IConfigurationPanel> panelClass = _defaultValidatorPanel;
		if (_validatorPanels != null
				&& _validatorPanels.containsKey(validationRuleClass)) {
			panelClass = _validatorPanels.get(validationRuleClass);
		}
		try {
			IConfigurationPanel configurationPanel = panelClass.newInstance();
			return configurationPanel;
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public void setDefaultProfilerPanel(
			Class<? extends IConfigurationPanel> defaultProfilerPanel) {
		_defaultProfilerPanel = defaultProfilerPanel;
	}

	public void setDefaultValidatorPanel(
			Class<? extends IConfigurationPanel> defaultValidatorPanel) {
		_defaultValidatorPanel = defaultValidatorPanel;
	}

	public void setProfilerPanels(
			Map<Class<? extends IProfile>, Class<? extends IConfigurationPanel>> profilerPanels) {
		_profilerPanels = profilerPanels;
	}

	public void setValidatorPanels(
			Map<Class<? extends IValidationRule>, Class<? extends IConfigurationPanel>> validatorPanels) {
		_validatorPanels = validatorPanels;
	}
}