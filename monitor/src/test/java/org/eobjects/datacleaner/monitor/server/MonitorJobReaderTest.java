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
package org.eobjects.datacleaner.monitor.server;

import java.util.Collection;
import java.util.Iterator;

import junit.framework.TestCase;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.descriptors.ClasspathScanDescriptorProvider;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.eobjects.datacleaner.repository.file.FileRepository;
import org.eobjects.metamodel.schema.ColumnType;

public class MonitorJobReaderTest extends TestCase {

    public void testMapPlaceholderColumnsQualified() throws Exception {
        AnalyzerBeansConfiguration configuration = new AnalyzerBeansConfigurationImpl()
                .replace(new ClasspathScanDescriptorProvider().scanPackage("org.eobjects.analyzer.beans", true));

        RepositoryFile jobFile = new FileRepository("src/test/resources/example_employee_job").getFile("employees.analysis.xml");

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
        AnalyzerBeansConfiguration configuration = new AnalyzerBeansConfigurationImpl()
                .replace(new ClasspathScanDescriptorProvider().scanPackage("org.eobjects.analyzer.beans", true));

        RepositoryFile jobFile = new FileRepository("src/test/resources/example_employee_job").getFile("alt_employees.analysis.xml");

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
