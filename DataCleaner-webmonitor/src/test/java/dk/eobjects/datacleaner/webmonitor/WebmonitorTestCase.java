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
package dk.eobjects.datacleaner.webmonitor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import junit.framework.TestCase;

/**
 * Abstract helper class for webmonitor specific tests
 */
public abstract class WebmonitorTestCase extends TestCase {

	public ApplicationContext initApplicationContext() {
		FileSystemXmlApplicationContext appCtx = new FileSystemXmlApplicationContext(
				"src/main/webapp/WEB-INF/datacleaner-config.xml");
		WebmonitorBootstrap bootstrap = new WebmonitorBootstrap();
		bootstrap.setApplicationContext(appCtx);
		bootstrap.initDataCleanerManagers();
		return appCtx;
	}

	public void validateXml(Source source) throws SAXException, IOException {
		SchemaFactory schemaFactory = SchemaFactory
				.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		File file = new File("src/main/resources/schemas/datacleaner.xsd");
		assertTrue(file.exists());
		Schema schema = schemaFactory.newSchema(file);
		Validator validator = schema.newValidator();
		validator.setErrorHandler(new ErrorHandler() {

			public void error(SAXParseException exception) throws SAXException {
				throw exception;
			}

			public void fatalError(SAXParseException exception)
					throws SAXException {
				throw exception;
			}

			public void warning(SAXParseException exception)
					throws SAXException {
				throw exception;
			}

		});
		validator.validate(source);
	}

	public void validateXml(String xml) throws SAXException, IOException {
		validateXml(new StreamSource(new StringReader(xml)));
	}

	public void validateXml(InputStream inputStream) throws SAXException,
			IOException {
		validateXml(new StreamSource(inputStream));
	}
}