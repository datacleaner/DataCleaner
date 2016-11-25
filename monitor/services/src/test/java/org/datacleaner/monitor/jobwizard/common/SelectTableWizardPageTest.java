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
package org.datacleaner.monitor.jobwizard.common;

import java.util.ArrayList;
import java.util.List;

import org.apache.metamodel.pojo.ArrayTableDataProvider;
import org.apache.metamodel.pojo.TableDataProvider;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.SimpleTableDef;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.PojoDatastore;
import org.datacleaner.monitor.wizard.WizardPageController;
import org.datacleaner.monitor.wizard.common.SelectTableWizardPage;
import org.datacleaner.monitor.wizard.job.JobWizardContext;
import org.easymock.EasyMock;

import junit.framework.TestCase;

public class SelectTableWizardPageTest extends TestCase {

    public void testGetFormInnerHtml() throws Exception {
        final List<TableDataProvider<?>> tableDataProviders = new ArrayList<>();
        final SimpleTableDef tableDef1 = new SimpleTableDef("tab", new String[] { "col1", "col2", "col3" });
        final SimpleTableDef tableDef2 = new SimpleTableDef("le", new String[] { "col1", "col2", "col3" });
        tableDataProviders.add(new ArrayTableDataProvider(tableDef1, new ArrayList<>()));
        tableDataProviders.add(new ArrayTableDataProvider(tableDef2, new ArrayList<>()));
        final Datastore datastore = new PojoDatastore("my_pojo_ds", tableDataProviders);

        final JobWizardContext context = EasyMock.createMock(JobWizardContext.class);
        EasyMock.expect(context.getSourceDatastore()).andReturn(datastore);

        EasyMock.replay(context);

        final Integer pageIndex = 1;

        final SelectTableWizardPage page = new SelectTableWizardPage(context, pageIndex) {
            @Override
            protected WizardPageController nextPageController(final Table selectedTable) {
                throw new IllegalStateException("Should not happen in this test");
            }
        };
        final String formInnerHtml1 = page.getFormInnerHtml();
        assertEquals("<div>\n" + "    <p>Please select the source table of the job:</p>\n"
                + "    <select name=\"tableName\">\n" + "            <optgroup label=\"my_pojo_ds\">\n"
                + "                    <option value=\"my_pojo_ds.le\">le</option>\n"
                + "                    <option value=\"my_pojo_ds.tab\">tab</option>\n" + "            </optgroup>\n"
                + "    </select>\n" + "</div>", formInnerHtml1.replaceAll("\t", "    ").replaceAll("\r\n", "\n"));

        page.setSelectedTableName("my_pojo_ds.tab");

        final String formInnerHtml2 = page.getFormInnerHtml();
        assertEquals("<div>\n" + "    <p>Please select the source table of the job:</p>\n"
                        + "    <select name=\"tableName\">\n" + "            <optgroup label=\"my_pojo_ds\">\n"
                        + "                    <option value=\"my_pojo_ds.le\">le</option>\n"
                        + "                    <option value=\"my_pojo_ds.tab\" selected=\"selected\">tab</option>\n"
                        + "            </optgroup>\n" + "    </select>\n" + "</div>",
                formInnerHtml2.replaceAll("\t", "    ").replaceAll("\r\n", "\n"));

        EasyMock.verify(context);
    }
}
