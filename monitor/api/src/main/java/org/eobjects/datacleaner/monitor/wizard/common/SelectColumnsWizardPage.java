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
package org.eobjects.datacleaner.monitor.wizard.common;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eobjects.datacleaner.monitor.wizard.WizardPageController;
import org.eobjects.metamodel.schema.Column;
import org.eobjects.metamodel.schema.Table;
import org.eobjects.metamodel.util.CollectionUtils;
import org.eobjects.metamodel.util.Func;

/**
 * A simple {@link WizardPageController} that asks the user to select the
 * {@link Column}s of interest.
 */
public abstract class SelectColumnsWizardPage extends AbstractFreemarkerWizardPage {

    private final Integer _pageIndex;
    private final Map<String, Column> _availableColumns;

    public SelectColumnsWizardPage(Integer pageIndex, Table table) {
        this(pageIndex, table.getColumns());
    }

    public SelectColumnsWizardPage(Integer pageIndex, Column[] availableColumns) {
        _pageIndex = pageIndex;
        _availableColumns = new LinkedHashMap<String, Column>();
        for (Column column : availableColumns) {
            _availableColumns.put(column.getName(), column);
        }
    }

    @Override
    protected Class<?> getTemplateFriendlyClass() {
        return SelectColumnsWizardPage.class;
    }

    @Override
    protected String getTemplateFilename() {
        return "SelectColumnsWizardPage.html";
    }

    @Override
    public Integer getPageIndex() {
        return _pageIndex;
    }

    @Override
    protected Map<String, Object> getFormModel() {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("columns", _availableColumns.values());
        return map;
    }

    @Override
    public WizardPageController nextPageController(Map<String, List<String>> formParameters) {
        final List<String> columnNames = formParameters.get("columns");

        final List<Column> selectedColumns = CollectionUtils.map(columnNames, new Func<String, Column>() {
            @Override
            public Column eval(String columnName) {
                return _availableColumns.get(columnName);
            }
        });

        return nextPageController(selectedColumns);
    }

    protected abstract WizardPageController nextPageController(List<Column> selectedColumns);

}
