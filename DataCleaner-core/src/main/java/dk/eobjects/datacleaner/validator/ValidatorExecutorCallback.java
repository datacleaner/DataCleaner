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
package dk.eobjects.datacleaner.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dk.eobjects.datacleaner.execution.DataCleanerExecutor;
import dk.eobjects.datacleaner.execution.ExecutionConfiguration;
import dk.eobjects.datacleaner.execution.IExecutorCallback;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.schema.Column;

public class ValidatorExecutorCallback
		implements
		IExecutorCallback<ValidatorJobConfiguration, IValidationRuleResult, IValidationRule> {

	protected Log _log = LogFactory.getLog(getClass());

	public static DataCleanerExecutor<ValidatorJobConfiguration, IValidationRuleResult, IValidationRule> createExecutor() {
		return new DataCleanerExecutor<ValidatorJobConfiguration, IValidationRuleResult, IValidationRule>(
				new ValidatorExecutorCallback());
	}

	public List<IValidationRule> initProcessors(
			Map<ValidatorJobConfiguration, Column[]> jobConfigurations,
			ExecutionConfiguration executionConfiguration) {
		ArrayList<IValidationRule> result = new ArrayList<IValidationRule>();
		for (Entry<ValidatorJobConfiguration, Column[]> entry : jobConfigurations
				.entrySet()) {
			ValidatorJobConfiguration configuration = entry.getKey();
			Column[] columns = entry.getValue();
			IValidationRule vr = initValidationRule(configuration, columns);
			result.add(vr);
		}
		return result;
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

	public IValidationRuleResult getResult(IValidationRule processor) {
		IValidationRuleResult result = processor.getResult();
		return result;
	}

	public void processRow(Row row, long count, IValidationRule processor) {
		processor.process(row, count);
	}
}