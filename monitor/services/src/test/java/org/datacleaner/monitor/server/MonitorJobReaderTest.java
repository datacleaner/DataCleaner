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
package org.datacleaner.monitor.server;

import java.util.Collection;
import java.util.Iterator;

import junit.framework.TestCase;

import org.apache.metamodel.schema.ColumnType;
import org.datacleaner.api.InputColumn;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.configuration.DataCleanerEnvironment;
import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.descriptors.ClasspathScanDescriptorProvider;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.repository.RepositoryFile;
import org.datacleaner.repository.file.FileRepository;

public class MonitorJobReaderTest extends TestCase {

    private final DataCleanerEnvironment environment = new DataCleanerEnvironmentImpl()
            .withDescriptorProvider(new ClasspathScanDescriptorProvider().scanPackage("org.datacleaner.beans", true));
    private final DataCleanerConfiguration configuration = new DataCleanerConfigurationImpl()
            .withEnvironment(environment);

    public void testMapPlaceholderColumnsQualified() throws Exception {
        RepositoryFile jobFile = new FileRepository("src/test/resources/example_employee_job")
                .getFile("employees.analysis.xml");

        MonitorJobReader reader = new MonitorJobReader(configuration, jobFile);

        AnalysisJob job = reader.readJob();
        Collection<InputColumn<?>> sourceColumns = job.getSourceColumns();
        assertEquals(
                "[MetaModelInputColumn[PUBLIC.EMPLOYEES.EMPLOYEENUMBER], MetaModelInputColumn[PUBLIC.EMPLOYEES.LASTNAME], "
                        + "MetaModelInputColumn[PUBLIC.EMPLOYEES.FIRSTNAME], MetaModelInputColumn[PUBLIC.EMPLOYEES.EXTENSION], "
                        + "MetaModelInputColumn[PUBLIC.EMPLOYEES.EMAIL], MetaModelInputColumn[PUBLIC.EMPLOYEES.OFFICECODE], "
                        + "MetaModelInputColumn[PUBLIC.EMPLOYEES.REPORTSTO], MetaModelInputColumn[PUBLIC.EMPLOYEES.JOBTITLE]]",
                sourceColumns.toString());

        Iterator<InputColumn<?>> it = sourceColumns.iterator();
        assertEquals(ColumnType.INTEGER, it.next().getPhysicalColumn().getType());
        assertEquals(ColumnType.VARCHAR, it.next().getPhysicalColumn().getType());
    }

    public void testMapPlaceholderColumnsNonQualified() throws Exception {
        RepositoryFile jobFile = new FileRepository("src/test/resources/example_employee_job")
                .getFile("alt_employees.analysis.xml");

        MonitorJobReader reader = new MonitorJobReader(configuration, jobFile);

        AnalysisJob job = reader.readJob();
        Collection<InputColumn<?>> sourceColumns = job.getSourceColumns();
        assertEquals(
                "[MetaModelInputColumn[schema.table.EMPLOYEENUMBER], MetaModelInputColumn[schema.table.LASTNAME], "
                        + "MetaModelInputColumn[schema.table.FIRSTNAME], MetaModelInputColumn[schema.table.EXTENSION], "
                        + "MetaModelInputColumn[schema.table.EMAIL], MetaModelInputColumn[schema.table.OFFICECODE], "
                        + "MetaModelInputColumn[schema.table.REPORTSTO], MetaModelInputColumn[schema.table.JOBTITLE]]",
                sourceColumns.toString());
    }
}
