/**
 * AnalyzerBeans
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
package org.eobjects.analyzer.job.runner;

import java.io.ByteArrayInputStream;
import java.io.PrintStream;
import java.sql.SQLException;

import junit.framework.TestCase;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.log4j.xml.DOMConfigurator;
import org.apache.metamodel.MetaModelException;
import org.apache.metamodel.util.FileHelper;

public class ErrorAwareAnalysisListenerTest extends TestCase {

    private ByteArrayOutputStream baos;
    private PrintStream oldOut;
    private PrintStream newOut;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // the logging config of this project logs to system out. to get those
        // log messages, we replace System.out
        baos = new ByteArrayOutputStream();
        oldOut = System.out;
        newOut = new PrintStream(baos);
        System.setOut(newOut);

        DOMConfigurator.configure("src/test/resources/log4j.xml");
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        System.setOut(oldOut);
    }

    public void testHandleErrorWithSqlNextException() throws Exception {
        ErrorAwareAnalysisListener listener = new ErrorAwareAnalysisListener();
        SQLException sqlException = new SQLException("foo", new IllegalStateException("bar"));
        sqlException.setNextException(new SQLException("baz"));

        listener.handleError(null, new MetaModelException(sqlException));

        String string = FileHelper.readInputStreamAsString(new ByteArrayInputStream(baos.toByteArray()), "UTF8");
        assertTrue(string,
                string.indexOf("org.apache.metamodel.MetaModelException: java.sql.SQLException: foo") != -1);

        assertTrue(string,
                string.indexOf("WARN  ErrorAwareAnalysisListener - SQLException.getNextException() stack trace:") != -1);

        assertTrue(string, string.indexOf("java.sql.SQLException: baz") != -1);

    }
}
