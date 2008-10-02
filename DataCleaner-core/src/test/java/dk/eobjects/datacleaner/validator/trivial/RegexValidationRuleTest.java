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
package dk.eobjects.datacleaner.validator.trivial;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import junit.framework.TestCase;
import dk.eobjects.datacleaner.validator.AbstractValidationRule;
import dk.eobjects.datacleaner.validator.IValidationRuleResult;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.query.SelectItem;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.ColumnType;

public class RegexValidationRuleTest extends TestCase {

	public void testSimple() throws Exception {
		AbstractValidationRule vr = new RegexValidationRule();
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(RegexValidationRule.PROPERTY_REGEX, "a*b");
		vr.setProperties(properties);

		Column column = new Column("foo", ColumnType.VARCHAR);
		Column[] columns = new Column[] { column };
		vr.initialize(columns);

		SelectItem[] items = new SelectItem[] { new SelectItem(column) };
		vr.process(new Row(items, new Object[] { "ab" }), 1);
		vr.process(new Row(items, new Object[] { "aab" }), 1);
		vr.process(new Row(items, new Object[] { "aba" }), 1);
		vr.process(new Row(items, new Object[] { "acb" }), 1);
		vr.process(new Row(items, new Object[] { "foobar" }), 1);

		IValidationRuleResult result = vr.getResult();
		assertFalse(result.isValidated());

		List<Row> rows = result.getUnvalidatedRows();
		assertEquals(3, rows.size());
		assertEquals(
				"[Row[values={aba}], Row[values={acb}], Row[values={foobar}]]",
				rows.toString());
	}

	public void testError() throws Exception {
		AbstractValidationRule vr = new RegexValidationRule();
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(RegexValidationRule.PROPERTY_REGEX, "[a-Z]*");
		vr.setProperties(properties);

		Column column = new Column("foo", ColumnType.VARCHAR);
		Column[] columns = new Column[] { column };
		vr.initialize(columns);

		IValidationRuleResult result = vr.getResult();
		assertFalse(result.isValidated());
		Exception error = result.getError();
		assertNotNull(error);
		String message = error.getMessage();
		assertTrue(message.indexOf("Illegal character range near index 3") != -1);
		assertTrue(message.indexOf("[a-Z]*") != -1);
		assertTrue(message.indexOf("^") != -1);
	}

	/**
	 * This is not really a test of the validation rule. Instead we test some of
	 * the expressions that are shipped in DataCleaner-resources
	 */
	public void testCommonExpressions() throws Exception {
		Pattern emailPattern = Pattern
				.compile("[a-zA-Z0-9._%+-]*@[a-zA-Z0-9._%+-]*\\.[a-z]{2,4}");
		assertTrue(emailPattern.matcher("kasper@eobjects.dk").matches());
		assertTrue(emailPattern.matcher("kasper.sorensen@mail.eobjects.dk")
				.matches());
		assertFalse(emailPattern.matcher("kasper@eobjectsdk").matches());

		Pattern usernamePattern = Pattern.compile("[a-zA-Z0-9_]{3,16}");
		assertTrue(usernamePattern.matcher("kasper").matches());
		assertTrue(usernamePattern.matcher("ks123").matches());
		assertTrue(usernamePattern.matcher("ks_123").matches());
		assertTrue(usernamePattern.matcher("ks_123_ks").matches());
		assertFalse(usernamePattern.matcher(" ks_123_ks").matches());
		assertFalse(usernamePattern.matcher("ks_123_ks ").matches());
		assertFalse(usernamePattern.matcher(" ks_123_ks").matches());
		assertFalse(usernamePattern.matcher("kasper sorensen").matches());
		assertFalse(usernamePattern.matcher("ks").matches());
		assertFalse(usernamePattern.matcher(
				"kaspersorensenwithaverylongnickname").matches());

		Pattern ipPattern = Pattern
				.compile("\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b");
		assertTrue(ipPattern.matcher("255.255.255.0").matches());
		assertTrue(ipPattern.matcher("127.0.0.1").matches());
		assertTrue(ipPattern.matcher("10.42.51.41").matches());

		Pattern websiteUrlPattern = Pattern
				.compile("^https?://[a-z0-9_-][\\.[a-z0-9_-]]*\\.(com|edu|org|net|int|info|eu|biz|mil|gov|aero|travel|pro|name|museum|coop|asia|[a-z][a-z])+(:[0-9]+)?[/[a-zA-Z0-9\\._#-]]*/?$");
		assertTrue(websiteUrlPattern.matcher(
				"http://www.eobjects01.com/w00pah.php/yay").matches());
		assertTrue(websiteUrlPattern.matcher("http://www.eobjects.dk")
				.matches());
		assertTrue(websiteUrlPattern.matcher("https://www.eobjects.dk")
				.matches());

		assertTrue(websiteUrlPattern.matcher("https://www.eobjects.dk:312")
				.matches());

		assertTrue(websiteUrlPattern.matcher(
				"http://www.eobjects.dk/trac/wiki/DataCleaner#Relatedlinks")
				.matches());
	}
}