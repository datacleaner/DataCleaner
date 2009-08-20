/**
 *  This file is part of DataCleaner.
 *
 *  DataCleaner is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DataCleaner is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DataCleaner.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.eobjects.datacleaner.export;

import java.io.File;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import dk.eobjects.datacleaner.export.XmlResultExporter.XmlType;
import dk.eobjects.datacleaner.profiler.MatrixBuilder;
import dk.eobjects.datacleaner.profiler.ProfileManagerTest;
import dk.eobjects.datacleaner.profiler.ProfileResult;
import dk.eobjects.datacleaner.validator.SimpleValidationRuleResult;
import dk.eobjects.datacleaner.validator.ValidationRuleManagerTest;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.query.SelectItem;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Table;

public class XmlResultExporterTest extends TestCase {

	public void testSafePrint() throws Exception {
		assertEquals(
				"Howdy &lt;there&gt; this is &quot;dangerous&quot; markup to &lt;be&gt; secured &amp; replaced!",
				XmlResultExporter
						.safePrint("Howdy <there> this is \"dangerous\" markup to <be> secured & replaced!"));
	}

	public void testWriteProfileResult() throws Exception {
		Column[] columns = new Column[] { new Column("col1"),
				new Column("col2") };

		HashMap<String, String> props = new HashMap<String, String>();
		props.put("foo", "bar");
		props.put("eobj", "ects");
		ProfileResult profileResult = new ProfileResult(
				ProfileManagerTest.DESCRIPTOR_STRING_ANALYSIS, props, columns);

		MatrixBuilder mb = new MatrixBuilder();
		mb.addColumn("col1");
		mb.addColumn("col2");
		mb.addRow("foO", 1, 2);
		mb.addRow("b4r", 3, 4);
		profileResult.addMatrix(mb.getMatrix());

		StringWriter sw = new StringWriter();
		PrintWriter printWriter = new PrintWriter(sw);

		XmlResultExporter exporter = new XmlResultExporter();
		exporter.writeProfileResultHeader(printWriter);
		exporter.writeProfileResult(new Table("hehe"), profileResult,
				printWriter);
		exporter.writeProfileResultFooter(printWriter);

		final String xmlOutput = sw.toString();
		String expectedResult = "<?xml version=_1.0_ encoding=_UTF-8_?>n<datacleanerResults xmlns=_http://datacleaner.eobjects.org/1.5/results_ xmlns:xsi=_http://www.w3.org/2001/XMLSchema-instance_>n<profileResult>n <jobConfiguration>n  <profile displayName=_String analysis_ className=_class dk.eobjects.datacleaner.profiler.trivial.StringAnalysisProfile_ />n  <table name=_hehe_ />n  <profiledColumns>n   <column name=_col1_ />n   <column name=_col2_ />n  </profiledColumns>n  <properties>n   <property name=_eobj_ value=_ects_ />n   <property name=_foo_ value=_bar_ />n  </properties>n </jobConfiguration>n <result>n  <matrix>n   <measure columnName=_col1_ columnIndex=_0_ rowName=_foO_ rowIndex=_0_ value=_1_ />n   <measure columnName=_col1_ columnIndex=_0_ rowName=_b4r_ rowIndex=_1_ value=_3_ />n   <measure columnName=_col2_ columnIndex=_1_ rowName=_foO_ rowIndex=_0_ value=_2_ />n   <measure columnName=_col2_ columnIndex=_1_ rowName=_b4r_ rowIndex=_1_ value=_4_ />n  </matrix>n </result>n</profileResult>n</datacleanerResults>";
		assertEquals(expectedResult, xmlOutput.replace('\"', '_').replace('\n',
				'n'));

		// Make sure that the result is parseable

		getDocumentBuilder().parse(new InputSource() {

			@Override
			public Reader getCharacterStream() {
				return new StringReader(xmlOutput);
			}
		});
	}

	public void testWriteValidationRuleResult() throws Exception {
		Column[] columns = new Column[] { new Column("col1"),
				new Column("col2") };

		HashMap<String, String> props = new HashMap<String, String>();
		props.put("foo", "bar");
		props.put("eobj", "ects");
		SimpleValidationRuleResult validationRuleResult = new SimpleValidationRuleResult(
				columns, ValidationRuleManagerTest.DESCRIPTOR_NOT_NULL, props);

		validationRuleResult.addErrorRow(new Row(
				new SelectItem[] { new SelectItem("foobar", "f") },
				new Object[] { 3 }));

		StringWriter sw = new StringWriter();
		PrintWriter printWriter = new PrintWriter(sw);
		
		XmlResultExporter exporter = new XmlResultExporter();
		exporter.writeValidationRuleResultHeader(printWriter);
		exporter.writeValidationRuleResult(new Table("hehe"),
				validationRuleResult, printWriter);
		exporter.writeValidationRuleResultFooter(printWriter);

		final String xmlOutput = sw.toString();
		String expectedResult = "<?xml version=_1.0_ encoding=_UTF-8_?>n<datacleanerResults xmlns=_http://datacleaner.eobjects.org/1.5/results_ xmlns:xsi=_http://www.w3.org/2001/XMLSchema-instance_>n<validationRuleResult>n <jobConfiguration>n  <validationRule displayName=_Not-null checker_ className=_class dk.eobjects.datacleaner.validator.trivial.NotNullValidationRule_ />n  <table name=_hehe_ />n  <evaluatedColumns>n   <column name=_col1_ />n   <column name=_col2_ />n  </evaluatedColumns>n  <properties>n   <property name=_eobj_ value=_ects_ />n   <property name=_foo_ value=_bar_ />n  </properties>n </jobConfiguration>n <result validated=_false_>n  <rowHeader><column>f</column>  </rowHeader>n  <invalidRow><value>3</value>  </invalidRow>n </result>n</validationRuleResult>n</datacleanerResults>";
		assertEquals(expectedResult, xmlOutput.replace('\"', '_').replace('\n',
				'n'));

		// Make sure that the result is parseable
		getDocumentBuilder().parse(new InputSource() {

			@Override
			public Reader getCharacterStream() {
				return new StringReader(xmlOutput);
			}
		});
	}

	public void testWriteRowBasedProfileResult() throws Exception {
		Column[] columns = new Column[] { new Column("col1"),
				new Column("col2") };

		HashMap<String, String> props = new HashMap<String, String>();
		props.put("foo", "bar");
		props.put("eobj", "ects");
		ProfileResult profileResult = new ProfileResult(
				ProfileManagerTest.DESCRIPTOR_STRING_ANALYSIS, props, columns);

		MatrixBuilder mb = new MatrixBuilder();
		mb.addColumn("measure1");
		mb.addColumn("measure2");
		mb.addRow("foo", 1, 2);
		mb.addRow("bar", 3, 4);
		profileResult.addMatrix(mb.getMatrix());

		StringWriter sw = new StringWriter();
		PrintWriter printWriter = new PrintWriter(sw);

		XmlResultExporter exporter = new XmlResultExporter();
		exporter.setXmlType(XmlType.ROW);
		exporter.writeProfileResultHeader(printWriter);
		exporter.writeProfileResult(new Table("myTable"), profileResult,
				printWriter);
		exporter.writeProfileResultFooter(printWriter);

		final String xmlOutput = sw.toString();
		String expectedResult = "<?xml version=_1.0_ encoding=_UTF-8_?>n<datacleanerResults xmlns=_http://datacleaner.eobjects.org/1.5/results_ xmlns:xsi=_http://www.w3.org/2001/XMLSchema-instance_>n<profileResult>n <jobConfiguration>n  <profile displayName=_String analysis_ className=_class dk.eobjects.datacleaner.profiler.trivial.StringAnalysisProfile_ />n  <table name=_myTable_ />n  <profiledColumns>n   <column name=_col1_ />n   <column name=_col2_ />n  </profiledColumns>n  <properties>n   <property name=_eobj_ value=_ects_ />n   <property name=_foo_ value=_bar_ />n  </properties>n </jobConfiguration>n <result>n  <matrix>n    <Row name=_foo_>n      <measure measureName=_measure1_ measeureIndex=_0_ rowIndex=_0_ value=_1_ />n      <measure measureName=_measure2_ measeureIndex=_1_ rowIndex=_0_ value=_2_ />n    </Row>n    <Row name=_bar_>n      <measure measureName=_measure1_ measeureIndex=_0_ rowIndex=_1_ value=_3_ />n      <measure measureName=_measure2_ measeureIndex=_1_ rowIndex=_1_ value=_4_ />n    </Row>n  </matrix>n </result>n</profileResult>n</datacleanerResults>";
		assertEquals(expectedResult, xmlOutput.replace('\"', '_').replace('\n',
				'n'));

		// Make sure that the result is parseable

		//FIXME: update XSD
		/*getDocumentBuilder().parse(new InputSource() {

			@Override
			public Reader getCharacterStream() {
				return new StringReader(xmlOutput);
			}
		});*/
	}
	
	
	
	/**
	 * Creates a schema aware, validating document builder
	 */
	private DocumentBuilder getDocumentBuilder()
			throws ParserConfigurationException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		dbf.setValidating(true);
		dbf.setAttribute(
				"http://java.sun.com/xml/jaxp/properties/schemaLanguage",
				"http://www.w3.org/2001/XMLSchema");
		File schemaFile = new File(
				"src/main/resources/datacleaner-export-schema.xsd");
		assertTrue(schemaFile.exists());
		dbf.setAttribute(
				"http://java.sun.com/xml/jaxp/properties/schemaSource",
				"src/main/resources/datacleaner-export-schema.xsd");
		DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
		documentBuilder.setErrorHandler(new ErrorHandler() {
			public void error(SAXParseException e) throws SAXException {
				throw e;
			}

			public void fatalError(SAXParseException e) throws SAXException {
				throw e;
			}

			public void warning(SAXParseException e) throws SAXException {
				throw e;
			}
		});
		return documentBuilder;
	}
}
