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
package org.datacleaner.monitor.wizard.common;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.CollectionUtils;
import org.datacleaner.monitor.wizard.WizardPageController;

/**
 * A simple {@link WizardPageController} that asks the user to select the
 * {@link Column}s of interest.
 */
public abstract class SelectColumnsWizardPage extends AbstractFreemarkerWizardPage {

    private final Integer _pageIndex;
    private final Map<String, Column> _availableColumns;

    public SelectColumnsWizardPage(final Integer pageIndex, final Table table) {
        this(pageIndex, table.getColumns());
    }

    public SelectColumnsWizardPage(final Integer pageIndex, final Column[] availableColumns) {
        _pageIndex = pageIndex;
        _availableColumns = new LinkedHashMap<>();
        for (final Column column : availableColumns) {
            _availableColumns.put(column.getName(), column);
        }
    }

    @Override
    protected Class<?> getTemplateFriendlyClass() {
        return SelectColumnsWizardPage.class;
    }

    /**
     * Gets the "header" part of the page, shown before the table of column
     * selections. Typically this part will contain instructions to the user as
     * to which columns to select
     *
     * @return
     */
    protected String getHeaderHtml() {
        return "<p>Please select the source columns of the job:</p>";
    }

    /**
     * Gets a "footer" part of the page, shown after the table of column
     * selections.
     *
     * @return
     */
    protected String getFooterHtml() {
        return "";
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
        final Map<String, Object> map = new HashMap<>();
        map.put("headerHtml", getHeaderHtml());
        map.put("columns", _availableColumns.values());
        map.put("footerHtml", getFooterHtml());
        return map;
    }

    @Override
    public WizardPageController nextPageController(final Map<String, List<String>> formParameters) {
        final List<String> columnNames = formParameters.get("columns");

        final List<Column> selectedColumns = CollectionUtils.map(columnNames, _availableColumns::get);

        return nextPageController(selectedColumns);
    }

    protected abstract WizardPageController nextPageController(List<Column> selectedColumns);

}
