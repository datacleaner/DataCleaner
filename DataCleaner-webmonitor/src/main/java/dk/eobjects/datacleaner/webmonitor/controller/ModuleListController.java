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
package dk.eobjects.datacleaner.webmonitor.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.View;

import dk.eobjects.datacleaner.profiler.IProfileDescriptor;
import dk.eobjects.datacleaner.profiler.ProfilerManager;
import dk.eobjects.datacleaner.validator.IValidationRuleDescriptor;
import dk.eobjects.datacleaner.validator.ValidatorManager;
import dk.eobjects.datacleaner.webmonitor.view.RestfulNode;
import dk.eobjects.datacleaner.webmonitor.view.RestfulServiceView;

@Controller
public class ModuleListController {

	@RequestMapping(value = { "/site/modules" })
	public String siteModules(ModelMap model) throws Exception {
		IProfileDescriptor[] profileDescriptors = ProfilerManager
				.getProfileDescriptors();
		IValidationRuleDescriptor[] validationRuleDescriptors = ValidatorManager
				.getValidationRuleDescriptors();

		model.addAttribute("profileDescriptors", profileDescriptors);
		model.addAttribute("validationRuleDescriptors",
				validationRuleDescriptors);
		return "modules";
	}

	@RequestMapping(value = { "/rest/modules" })
	public View restModules() {
		IProfileDescriptor[] profileDescriptors = ProfilerManager
				.getProfileDescriptors();
		IValidationRuleDescriptor[] validationRuleDescriptors = ValidatorManager
				.getValidationRuleDescriptors();

		RestfulServiceView view = new RestfulServiceView();
		RestfulNode modules = RestfulNode.newModulesNode();
		for (int i = 0; i < profileDescriptors.length; i++) {
			RestfulNode descriptorNode = RestfulNode
					.newProfileDescriptorNode(profileDescriptors[i]);
			modules.addInnerNode(descriptorNode);
		}
		for (int i = 0; i < validationRuleDescriptors.length; i++) {
			RestfulNode descriptorNode = RestfulNode
					.newValidationRuleDescriptorNode(validationRuleDescriptors[i]);
			modules.addInnerNode(descriptorNode);
		}
		view.setRootNode(modules);
		return view;
	}
}