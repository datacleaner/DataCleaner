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
package dk.eobjects.datacleaner.data;

import java.io.File;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.stream.StreamResult;

import junit.framework.TestCase;

import org.w3c.dom.Document;

import dk.eobjects.datacleaner.util.DomHelper;

public class DataContextSelectionTest extends TestCase {

	private DocumentBuilder _documentBuilder = DomHelper.getDocumentBuilder();
	private Document _document;
	private DataContextSelection _dataContextSelection;

	public void testCsv() throws Exception {
		_document = _documentBuilder.parse(new File(
				"src/test/resources/serialized_datacontextselection_csv.xml"));

		_dataContextSelection = DataContextSelection.deserialize(_document
				.getDocumentElement());
		assertNotNull(_dataContextSelection);
		assertEquals("customers_semicolon.csv", _dataContextSelection
				.getDataContext().getSchemaNames()[0]);

		StringWriter sw = new StringWriter();
		DomHelper.transform(_dataContextSelection.serialize(_document),
				new StreamResult(sw));
		assertEquals(
				"<?xml version=_1.0_ encoding=_UTF-8_?><dataContext><property name=_filename_>"
						+ new File("src/test/resources/customers_semicolon.csv")
								.getAbsolutePath()
						+ "</property><property name=_quoteChar_>_</property><property name=_separator_>;</property></dataContext>",
				sw.toString().replace('\"', '_'));
	}

	public void testExcel() throws Exception {
		_document = _documentBuilder.parse(new File(
				"src/test/resources/serialized_datacontextselection_xls.xml"));

		_dataContextSelection = DataContextSelection.deserialize(_document
				.getDocumentElement());
		assertNotNull(_dataContextSelection);
		assertEquals("customers.xls", _dataContextSelection.getDataContext()
				.getSchemaNames()[0]);

		StringWriter sw = new StringWriter();
		DomHelper.transform(_dataContextSelection.serialize(_document),
				new StreamResult(sw));
		assertEquals(
				"<?xml version=_1.0_ encoding=_UTF-8_?><dataContext><property name=_filename_>"
						+ new File("src/test/resources/customers.xls")
								.getAbsolutePath()
						+ "</property></dataContext>", sw.toString().replace(
						'\"', '_'));
	}

	public void testDatabase() throws Exception {
		Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
		_document = _documentBuilder.parse(new File(
				"src/test/resources/serialized_datacontextselection_db.xml"));

		_dataContextSelection = DataContextSelection.deserialize(_document
				.getDocumentElement());
		assertNotNull(_dataContextSelection);
		assertEquals("APP", _dataContextSelection.getDataContext()
				.getDefaultSchema().getName());

		StringWriter sw = new StringWriter();
		DomHelper.transform(_dataContextSelection.serialize(_document),
				new StreamResult(sw));
		assertEquals(
				"<?xml version=_1.0_ encoding=_UTF-8_?><dataContext><property name=_catalog_/>"
						+ "<property name=_connectionString_>jdbc:derby:src/test/resources/datacleaner_testdb;territory=en</property>"
						+ "<property name=_password_/><property name=_tables_>true</property><property name=_username_/>"
						+ "<property name=_views_>false</property></dataContext>",
				sw.toString().replace('\"', '_'));
		_dataContextSelection.selectNothing();
	}
}