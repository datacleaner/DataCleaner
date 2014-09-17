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
package org.eobjects.datacleaner.monitor.jobwizard.quickanalysis;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eobjects.datacleaner.monitor.shared.model.DCUserInputException;
import org.eobjects.datacleaner.monitor.wizard.WizardPageController;
import org.eobjects.datacleaner.monitor.wizard.common.AbstractFreemarkerWizardPage;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.CollectionUtils;
import org.apache.metamodel.util.Func;

/**
 * Page where user gets to select value distribution columns
 */
public abstract class SelectValueDistributionColumnsPage extends
		AbstractFreemarkerWizardPage {

	private final int _pageIndex;
	private final Map<String, Column> _availableColumns;

	public SelectValueDistributionColumnsPage(int pageIndex, Table table) {
		_pageIndex = pageIndex;
		_availableColumns = new LinkedHashMap<String, Column>();
		for (Column column : table.getColumns()) {
		    ColumnType type = column.getType();
            if (type == null || type.isLiteral() || type.isBoolean() || type.isNumber()) {
		        _availableColumns.put(column.getName(), column);
		    }
		}
	}

	@Override
	public Integer getPageIndex() {
		return _pageIndex;
	}

	@Override
	public WizardPageController nextPageController(
			Map<String, List<String>> formParameters)
			throws DCUserInputException {
		List<String> columnNames = formParameters.get("columns");
		if (columnNames == null) {
			columnNames = Collections.emptyList();
		}

		final List<Column> selectedColumns = CollectionUtils.map(columnNames,
				new Func<String, Column>() {
					@Override
					public Column eval(String columnName) {
						return _availableColumns.get(columnName);
					}
				});

		return nextPageController(selectedColumns);
	}

	protected abstract WizardPageController nextPageController(
			List<Column> selectedColumns);

	@Override
	protected String getTemplateFilename() {
		return "SelectValueDistributionColumnsPage.html";
	}

	@Override
	protected Map<String, Object> getFormModel() {
		final Map<String, Object> map = new HashMap<String, Object>();
		map.put("columns", _availableColumns.values());
		return map;
	}

}
