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
package org.eobjects.datacleaner.monitor.jobwizard.common;

import java.util.List;

import junit.framework.TestCase;

import org.eobjects.datacleaner.monitor.jobwizard.api.JobWizardPageController;
import org.eobjects.metamodel.schema.Column;
import org.eobjects.metamodel.schema.ColumnType;
import org.eobjects.metamodel.schema.MutableColumn;
import org.eobjects.metamodel.schema.MutableTable;
import org.eobjects.metamodel.schema.Table;

public class SelectColumnsWizardPageTest extends TestCase {

    public void testGetFormInnerHtml() throws Exception {
        final Integer pageIndex = 1;

        final Table table = new MutableTable().addColumn(new MutableColumn("foo", ColumnType.INTEGER))
                .addColumn(new MutableColumn("bar", ColumnType.VARCHAR))
                .addColumn(new MutableColumn("baz"));

        final SelectColumnsWizardPage page = new SelectColumnsWizardPage(pageIndex, table) {
            @Override
            protected JobWizardPageController nextPageController(List<Column> selectedColumns) {
                throw new IllegalStateException("Should not happen in this test");
            }
        };
        final String formInnerHtml = page.getFormInnerHtml();
        assertEquals(
                "<div>\n"
                        + "    <p>Please select the source columns of the job:</p>\n"
                        + "    \n"
                        + "    <table>\n"
                        + "            <tr>\n"
                        + "            <td>\n"
                        + "                <input type=\"checkbox\" name=\"columns\" id=\"column_checkbox_0\" value=\"foo\" title=\"foo\" />\n"
                        + "            </td>\n"
                        + "            <td>\n"
                        + "                <label for=\"column_checkbox_0\">foo</label>\n"
                        + "            </td>\n"
                        + "            <td>\n"
                        + "                INTEGER\n"
                        + "            </td>\n"
                        + "        </tr>\n"
                        + "        <tr>\n"
                        + "            <td>\n"
                        + "                <input type=\"checkbox\" name=\"columns\" id=\"column_checkbox_1\" value=\"bar\" title=\"bar\" />\n"
                        + "            </td>\n"
                        + "            <td>\n"
                        + "                <label for=\"column_checkbox_1\">bar</label>\n"
                        + "            </td>\n"
                        + "            <td>\n"
                        + "                VARCHAR\n"
                        + "            </td>\n"
                        + "        </tr>\n"
                        + "        <tr>\n"
                        + "            <td>\n"
                        + "                <input type=\"checkbox\" name=\"columns\" id=\"column_checkbox_2\" value=\"baz\" title=\"baz\" />\n"
                        + "            </td>\n" + "            <td>\n"
                        + "                <label for=\"column_checkbox_2\">baz</label>\n" + "            </td>\n"
                        + "            <td>\n" + "                n/a\n" + "            </td>\n"
                        + "        </tr>\n" + "    </table>\n" + "</div>", formInnerHtml.replaceAll("\t", "    ")
                        .replaceAll("\r\n", "\n"));

    }
}
