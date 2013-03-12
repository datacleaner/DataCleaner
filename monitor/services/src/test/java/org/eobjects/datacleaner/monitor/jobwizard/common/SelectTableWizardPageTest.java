/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.PojoDatastore;
import org.eobjects.datacleaner.monitor.wizard.WizardPageController;
import org.eobjects.datacleaner.monitor.wizard.common.SelectTableWizardPage;
import org.eobjects.datacleaner.monitor.wizard.job.JobWizardContext;
import org.eobjects.metamodel.pojo.ArrayTableDataProvider;
import org.eobjects.metamodel.pojo.TableDataProvider;
import org.eobjects.metamodel.schema.Table;
import org.eobjects.metamodel.util.SimpleTableDef;

public class SelectTableWizardPageTest extends TestCase {

    public void testGetFormInnerHtml() throws Exception {
        final List<TableDataProvider<?>> tableDataProviders = new ArrayList<TableDataProvider<?>>();
        final SimpleTableDef tableDef = new SimpleTableDef("tab", new String[] { "col1", "col2", "col3" });
        tableDataProviders.add(new ArrayTableDataProvider(tableDef, new ArrayList<Object[]>()));
        final Datastore datastore = new PojoDatastore("my_pojo_ds", tableDataProviders);

        final JobWizardContext context = EasyMock.createMock(JobWizardContext.class);
        EasyMock.expect(context.getSourceDatastore()).andReturn(datastore);

        EasyMock.replay(context);

        final Integer pageIndex = 1;

        final SelectTableWizardPage page = new SelectTableWizardPage(context, pageIndex) {
            @Override
            protected WizardPageController nextPageController(Table selectedTable) {
                throw new IllegalStateException("Should not happen in this test");
            }
        };
        final String formInnerHtml = page.getFormInnerHtml();
        assertEquals("<div>\n" + 
        		"    <p>Please select the source table of the job:</p>\n" + 
        		"    <select name=\"tableName\">\n" + 
        		"            <optgroup label=\"information_schema\">\n" + 
        		"                    <option value=\"information_schema.tables\">tables</option>\n" + 
        		"                    <option value=\"information_schema.columns\">columns</option>\n" + 
        		"                    <option value=\"information_schema.relationships\">relationships</option>\n" + 
        		"            </optgroup>\n" + 
        		"            <optgroup label=\"my_pojo_ds\">\n" + 
        		"                    <option value=\"my_pojo_ds.tab\">tab</option>\n" + 
        		"            </optgroup>\n" + 
        		"    </select>\n" + 
        		"</div>", formInnerHtml.replaceAll("\t", "    ").replaceAll("\r\n", "\n"));

        EasyMock.verify(context);
    }
}
