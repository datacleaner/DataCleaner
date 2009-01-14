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
package dk.eobjects.datacleaner.execution;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import dk.eobjects.datacleaner.validator.IValidationRule;
import dk.eobjects.datacleaner.validator.IValidationRuleResult;
import dk.eobjects.datacleaner.validator.ValidatorJobConfiguration;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.schema.Column;

public class ValidationRuleRunner
		extends
		AbstractRunner<ValidatorJobConfiguration, IValidationRuleResult, IValidationRule> {

	@Override
	protected IValidationRule[] initConfigurations(
			Map<ValidatorJobConfiguration, Column[]> configurations) {
		ArrayList<IValidationRule> result = new ArrayList<IValidationRule>();
		for (Entry<ValidatorJobConfiguration, Column[]> entry : configurations
				.entrySet()) {
			ValidatorJobConfiguration configuration = entry.getKey();
			Column[] columns = entry.getValue();
			IValidationRule vr = initValidationRule(configuration, columns);
			result.add(vr);
		}
		return result.toArray(new IValidationRule[result.size()]);
	}

	private IValidationRule initValidationRule(
			ValidatorJobConfiguration configuration, Column[] columns) {
		Class<? extends IValidationRule> validationRuleClass = configuration
				.getValidationRuleDescriptor().getValidationRuleClass();
		try {
			IValidationRule vr = validationRuleClass.newInstance();
			vr.setProperties(configuration.getValidationRuleProperties());
			vr.initialize(columns);
			return vr;
		} catch (InstantiationException e) {
			_log.fatal(e);
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			_log.fatal(e);
			throw new RuntimeException(e);
		}
	}

	@Override
	protected IValidationRuleResult getResult(IValidationRule processor) {
		IValidationRuleResult result = processor.getResult();
		return result;
	}

	@Override
	protected void processRow(Row row, long count, IValidationRule processor) {
		processor.process(row, count);
	}
}