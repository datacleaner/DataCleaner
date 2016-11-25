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
package org.datacleaner.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.SerializationUtils;
import org.apache.log4j.PropertyConfigurator;
import org.apache.metamodel.util.FileHelper;
import org.datacleaner.result.AnalysisResult;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.google.common.base.Splitter;

import junit.framework.TestCase;
import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.sax.HtmlParser;

public class MainTest extends TestCase {

    private StringWriter _stringWriter;
    private PrintStream _originalSysOut;

    @Override
    protected void setUp() throws Exception {
        _stringWriter = new StringWriter();
        _originalSysOut = System.out;
        useAsSystemOut(_stringWriter);

        PropertyConfigurator.configure("src/test/resources/log4j.xml");
    }

    private void useAsSystemOut(final StringWriter stringWriter) {
        final OutputStream out = new OutputStream() {
            @Override
            public void write(final int b) throws IOException {
                _stringWriter.write(b);
            }
        };
        System.setOut(new PrintStream(out));
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        System.setOut(_originalSysOut);
    }

    public void testUsage() throws Throwable {
        Main.main("-usage".split(" "));

        final String out1 = _stringWriter.toString();

        final String[] lines = out1.split("\n");

        assertEquals(13, lines.length);

        assertEquals("-conf (-configuration, --configuration-file) PATH          :"
                + " Path to an XML file describing the configuration of", lines[0].trim());
        assertEquals("DataCleaner", lines[1].trim());
        assertEquals("-ds (-datastore, --datastore-name) VAL                     :"
                + " Name of datastore when printing a list of schemas, tables", lines[2].trim());
        assertEquals("or columns. Overrides datastore used when used with -job", lines[3].trim());
        assertEquals("-job (--job-file) PATH                                     :"
                + " Path to an analysis job XML file to execute", lines[4].trim());
        assertEquals("-list [ANALYZERS | TRANSFORMERS | FILTERS | DATASTORES |   :"
                + " Used to print a list of various elements available in the", lines[5].trim());
        assertEquals("SCHEMAS | TABLES | COLUMNS]                                : configuration", lines[6].trim());
        assertEquals("-of (--output-file) PATH                                   :"
                + " Path to file in which to save the result of the job", lines[7].trim());
        assertEquals("-ot (--output-type) [TEXT | HTML | SERIALIZED]             :"
                + " How to represent the result of the job", lines[8].trim());
        assertEquals("-properties (--properties-file) PATH                       : Path to a custom properties file",
                lines[9].trim());

        assertEquals("-runtype (--runtype) [LOCAL | SPARK]                       : How/where to run the job",
                lines[10].trim());
        assertEquals("-s (-schema, --schema-name) VAL                            :"
                + " Name of schema when printing a list of tables or columns", lines[11].trim());
        assertEquals("-t (-table, --table-name) VAL                              :"
                + " Name of table when printing a list of columns", lines[12].trim());

        // again without the -usage flag
        _stringWriter = new StringWriter();
        useAsSystemOut(_stringWriter);
        Main.main(new String[0]);

        final String out2 = _stringWriter.toString();
        assertEquals(out1, out2);
    }

    public void testListDatastores() throws Throwable {
        Main.main("-conf src/test/resources/cli-examples/conf.xml -list DATASTORES".split(" "));

        final String out = _stringWriter.toString().replaceAll("\r\n", "\n");
        assertEquals("Datastores:\n-----------\nall_datastores\nemployees_csv\norderdb\n", out);
    }

    public void testListSchemas() throws Throwable {
        Main.main("-conf src/test/resources/cli-examples/conf.xml -ds orderdb -list SCHEMAS".split(" "));

        final String out = _stringWriter.toString().replaceAll("\r\n", "\n");
        assertEquals("Schemas:\n" + "--------\n" + "INFORMATION_SCHEMA\n" + "PUBLIC\n", out);
    }

    public void testListTables() throws Throwable {
        Main.main("-conf src/test/resources/cli-examples/conf.xml -ds orderdb -schema PUBLIC -list TABLES".split(" "));

        final String out = _stringWriter.toString().replaceAll("\r\n", "\n");
        assertEquals(
                "Tables:\n-------\nCUSTOMERS\nEMPLOYEES\nOFFICES\nORDERDETAILS\nORDERFACT\nORDERS\nPAYMENTS\nPRODUCTS\n",
                out);
    }

    public void testListColumns() throws Throwable {
        Main.main(
                "-conf src/test/resources/cli-examples/conf.xml -ds orderdb -schema PUBLIC -table EMPLOYEES -list COLUMNS"
                        .split(" "));

        final String out = _stringWriter.toString().replaceAll("\r\n", "\n");
        assertEquals(
                "Columns:\n--------\nEMPLOYEENUMBER\nLASTNAME\nFIRSTNAME\nEXTENSION\nEMAIL\nOFFICECODE\nREPORTSTO\nJOBTITLE\n",
                out);
    }

    public void testListTransformers() throws Throwable {
        Main.main("-conf src/test/resources/cli-examples/conf.xml -list TRANSFORMERS".split(" "));

        final String out = _stringWriter.toString().replaceAll("\r\n", "\n");
        final String[] lines = out.split("\n");

        assertEquals("Transformers:", lines[0]);

        assertTrue(out, out.indexOf("name: Email standardizer") != -1);
        assertTrue(out, out.indexOf(" - Consumes a single input column (type: String)") != -1);
    }

    public void testListFilters() throws Throwable {
        Main.main("-conf src/test/resources/cli-examples/conf.xml -list FILTERS".split(" "));

        final String out = _stringWriter.toString().replaceAll("\r\n", "\n");
        final String[] lines = out.split("\n");

        assertEquals("Filters:", lines[0]);

        assertTrue(out.indexOf("name: Null check") != -1);
        assertTrue(out.indexOf("- Outcome: NOT_NULL") != -1);
        assertTrue(out.indexOf("- Outcome: NULL") != -1);
    }

    public void testListAnalyzers() throws Throwable {
        Main.main("-conf src/test/resources/cli-examples/conf.xml -list ANALYZERS".split(" "));

        final String out = _stringWriter.toString().replaceAll("\r\n", "\n");
        final String[] lines = out.split("\n");

        assertEquals("Analyzers:", lines[0]);

        assertTrue(out.indexOf("name: Pattern finder") != -1);
        assertTrue(out.indexOf("name: String analyzer") != -1);
    }

    public void testExampleEmployeesJob() throws Throwable {
        Main.main(
                "-conf src/test/resources/cli-examples/conf.xml -job src/test/resources/cli-examples/employees_job.xml"
                        .split(" "));

        final String out = _stringWriter.toString().replaceAll("\r\n", "\n");
        final List<String> lines = Splitter.on('\n').splitToList(out);

        assertTrue(out, out.indexOf("- Value count (company.com): 4") != -1);
        assertTrue(out, out.indexOf("- Value count (eobjects.org): 2") != -1);

        assertTrue("lines length was: " + lines.size(), lines.size() > 60);
        assertTrue("lines length was: " + lines.size(), lines.size() < 90);

        assertTrue(lines.contains("SUCCESS!"));
    }

    public void testWriteToFile() throws Throwable {
        final String filename = "target/test_write_to_file.txt";
        Main.main(
                ("-conf src/test/resources/cli-examples/conf.xml -job src/test/resources/cli-examples/employees_job.xml -of "
                        + filename).split(" "));

        final File file = new File(filename);
        assertTrue(file.exists());
        final String result = FileHelper.readFileAsString(file);
        assertEquals("SUCCESS!", result.split("\n")[0].trim());
    }

    public void testWriteHtmlToFile() throws Throwable {
        final String filename = "target/test_write_html_to_file.html";
        Main.main(
                ("-conf src/test/resources/cli-examples/conf.xml -job src/test/resources/cli-examples/employees_job.xml -of "
                        + filename + " -ot HTML").split(" "));

        final File file = new File(filename);
        assertTrue(file.exists());

        {
            final String result = FileHelper.readFileAsString(file);
            final String[] lines = result.split("\n");

            assertEquals("<html>", lines[1]);
        }

        try (InputStream in = FileHelper.getInputStream(file)) {
            // parse it with validator.nu for HTML correctness
            final HtmlParser htmlParser = new HtmlParser(XmlViolationPolicy.FATAL);
            final AtomicInteger elementCounter = new AtomicInteger();
            htmlParser.setContentHandler(new DefaultHandler() {
                @Override
                public void startElement(final String uri, final String localName, final String qName,
                        final Attributes attributes) throws SAXException {
                    elementCounter.incrementAndGet();
                }
            });
            final List<Exception> warningsAndErrors = new ArrayList<>();
            htmlParser.setErrorHandler(new ErrorHandler() {
                @Override
                public void warning(final SAXParseException exception) throws SAXException {
                    System.err.println("Warning: " + exception.getMessage());
                    warningsAndErrors.add(exception);
                }

                @Override
                public void fatalError(final SAXParseException exception) throws SAXException {
                    System.out.println("Fatal error: " + exception.getMessage());
                    throw exception;
                }

                @Override
                public void error(final SAXParseException exception) throws SAXException {
                    System.err.println("Error: " + exception.getMessage());
                    warningsAndErrors.add(exception);
                }
            });

            htmlParser.parse(new InputSource(in));

            // the output has approx 3600 XML elements
            final int elementCount = elementCounter.get();
            assertTrue("Element count: " + elementCount, elementCount > 3000);
            assertTrue("Element count: " + elementCount, elementCount < 5000);

            if (!warningsAndErrors.isEmpty()) {
                for (final Exception error : warningsAndErrors) {
                    final String message = error.getMessage();
                    if (message.startsWith("No explicit character encoding declaration has been seen yet") || message
                            .startsWith("The character encoding of the document was not declared.")) {
                        // ignore/accept this one
                        continue;
                    }
                    error.printStackTrace();
                    fail("Got " + warningsAndErrors.size() + " warnings and errors, see log for details");
                }
            }
        }
    }

    public void testWriteSerializedToFile() throws Throwable {
        final String filename = "target/test_write_serialized_to_file.analysis.result.dat";
        Main.main(
                ("-conf src/test/resources/cli-examples/conf.xml -job src/test/resources/cli-examples/employees_job.xml -of "
                        + filename + " -ot SERIALIZED").split(" "));

        final File file = new File(filename);
        assertTrue(file.exists());

        final AnalysisResult result = (AnalysisResult) SerializationUtils.deserialize(new FileInputStream(file));
        assertNotNull(result);
        assertEquals(6, result.getResults().size());
    }
}
