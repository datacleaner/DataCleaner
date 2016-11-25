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
package org.datacleaner.monitor.server.wizard;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.metamodel.csv.CsvConfiguration;
import org.apache.metamodel.util.Resource;
import org.datacleaner.monitor.wizard.WizardPageController;
import org.easymock.EasyMock;

import junit.framework.TestCase;

public class CsvConfigurationWizardPageTest extends TestCase {

    public void testBuildConfigurationFromParameters() throws Exception {
        final WizardPageController pageMock = EasyMock.createMock(WizardPageController.class);

        final Resource file = null;
        final CsvConfigurationWizardPage page = new CsvConfigurationWizardPage(file) {
            @Override
            protected WizardPageController nextPageController(final CsvConfiguration configuration) {
                assertEquals("UTF8", configuration.getEncoding());
                assertEquals(1, configuration.getColumnNameLineNumber());
                assertEquals(CsvConfiguration.NOT_A_CHAR, configuration.getEscapeChar());
                assertEquals('"', configuration.getQuoteChar());
                assertEquals(';', configuration.getSeparatorChar());
                return pageMock;
            }
        };

        final Map<String, List<String>> parameters = new HashMap<>();
        parameters.put("separator", Arrays.asList(";|;"));
        parameters.put("escape", Arrays.asList(""));
        parameters.put("quote", Arrays.asList("\""));
        parameters.put("headerLineNumber", Arrays.asList("1"));
        parameters.put("encoding", Arrays.asList("UTF8"));

        final WizardPageController result = page.nextPageController(parameters);

        assertSame(result, pageMock);
    }
}
