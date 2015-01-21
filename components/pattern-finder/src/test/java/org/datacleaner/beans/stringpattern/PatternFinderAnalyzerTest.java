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
package org.datacleaner.beans.stringpattern;

import junit.framework.TestCase;

import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;
import org.datacleaner.descriptors.AnalyzerDescriptor;
import org.datacleaner.descriptors.Descriptors;
import org.datacleaner.storage.InMemoryRowAnnotationFactory;

public class PatternFinderAnalyzerTest extends TestCase {

	public void testDescriptor() throws Exception {
		// simply test that the analyzer is valid
	    AnalyzerDescriptor<PatternFinderAnalyzer> descriptor = Descriptors.ofAnalyzer(PatternFinderAnalyzer.class);
		assertEquals("Pattern finder", descriptor.getDisplayName());
	}

	public void testSingleToken() throws Exception {
		PatternFinderAnalyzer pf = new PatternFinderAnalyzer();
		MockInputColumn<String> column = new MockInputColumn<String>("title", String.class);

		pf.setRowAnnotationFactory(new InMemoryRowAnnotationFactory());
		pf.setColumn(column);

		pf.init();

		pf.run(new MockInputRow().put(column, "blabla"), 1);

		assertEquals("Crosstab:\nMatch count,aaaaaa: 1\nSample,aaaaaa: blabla", pf.getResult().getSingleCrosstab()
				.toString());
	}

	public void testEmployeeTitles() throws Exception {
		PatternFinderAnalyzer pf = new PatternFinderAnalyzer();
		MockInputColumn<String> column = new MockInputColumn<String>("title", String.class);

		pf.setRowAnnotationFactory(new InMemoryRowAnnotationFactory());
		pf.setColumn(column);
		pf.setDiscriminateTextCase(true);

		pf.init();

		pf.run(new MockInputRow().put(column, "Sales director"), 1);

		String[] resultLines;
		resultLines = new PatternFinderResultTextRenderer().render(pf.getResult()).split("\n");
		assertEquals(2, resultLines.length);
		assertEquals("               Match count Sample      ", resultLines[0]);
		assertEquals("Aaaaa aaaaaaaa           1 Sales director ", resultLines[1]);

		pf.run(new MockInputRow().put(column, "Key account manager"), 1);
		pf.run(new MockInputRow().put(column, "Account manager"), 1);
		pf.run(new MockInputRow().put(column, "Sales manager (EMEA)"), 1);

		resultLines = new PatternFinderResultTextRenderer().render(pf.getResult()).split("\n");
		assertEquals(4, resultLines.length);
		assertEquals("                     Match count Sample      ", resultLines[0]);
		assertEquals("Aaaaaaa aaaaaaaa               2 Sales director ", resultLines[1]);
		assertEquals("Aaa aaaaaaa aaaaaaa            1 Key account manager ", resultLines[2]);
		assertEquals("Aaaaa aaaaaaa (AAAA)           1 Sales manager (EMEA) ", resultLines[3]);

		pf.run(new MockInputRow().put(column, "Sales Manager, USA"), 1);
		pf.run(new MockInputRow().put(column, "Account Manager (USA)"), 1);
		pf.run(new MockInputRow().put(column, "1st on the phone"), 1);

		resultLines = new PatternFinderResultTextRenderer().render(pf.getResult()).split("\n");
		assertEquals(7, resultLines.length);
		assertEquals("                      Match count Sample      ", resultLines[0]);
		assertEquals("Aaaaaaa aaaaaaaa                2 Sales director ", resultLines[1]);
		assertEquals("??? aa aaa aaaaa                1 1st on the phone ", resultLines[2]);
		assertEquals("Aaa aaaaaaa aaaaaaa             1 Key account manager ", resultLines[3]);
		assertEquals("Aaaaa Aaaaaaa, AAA              1 Sales Manager, USA ", resultLines[4]);
		assertEquals("Aaaaa aaaaaaa (AAAA)            1 Sales manager (EMEA) ", resultLines[5]);
		assertEquals("Aaaaaaa Aaaaaaa (AAA)           1 Account Manager (USA) ", resultLines[6]);
	}

	public void testEmailAddresses() throws Exception {
		PatternFinderAnalyzer pf = new PatternFinderAnalyzer();
		MockInputColumn<String> column = new MockInputColumn<String>("title", String.class);

		pf.setRowAnnotationFactory(new InMemoryRowAnnotationFactory());
		pf.setColumn(column);
		pf.setDiscriminateTextCase(true);

		pf.init();

		pf.run(new MockInputRow().put(column, "kasper@eobjects.dk"), 1);
		pf.run(new MockInputRow().put(column, "kasper.sorensen@eobjects.dk"), 1);
		pf.run(new MockInputRow().put(column, "john@doe.com"), 1);
		pf.run(new MockInputRow().put(column, ""), 3);
		pf.run(new MockInputRow().put(column, "john.doe@company.com"), 1);
		pf.run(new MockInputRow().put(column, null), 1);
		pf.run(new MockInputRow().put(column, null), 1);

		String[] resultLines = new PatternFinderResultTextRenderer().render(pf.getResult()).split("\n");
		assertEquals(5, resultLines.length);
		assertEquals("                             Match count Sample      ", resultLines[0]);
		assertEquals("<blank>                                3 <blank>     ", resultLines[1]);
		assertEquals("<null>                                 2 <null>      ", resultLines[2]);
		assertEquals("aaaaaa.aaaaaaaa@aaaaaaaa.aaa           2 kasper.sorensen@eobjects.dk ", resultLines[3]);
		assertEquals("aaaaaa@aaaaaaaa.aaa                    2 kasper@eobjects.dk ", resultLines[4]);
	}

	public void testGroupEmailByDomain() throws Exception {
		PatternFinderAnalyzer pf = new PatternFinderAnalyzer();
		MockInputColumn<String> col1 = new MockInputColumn<String>("username", String.class);
		MockInputColumn<String> col2 = new MockInputColumn<String>("domain", String.class);

		pf.setRowAnnotationFactory(new InMemoryRowAnnotationFactory());
		pf.setColumn(col1);
		pf.setGroupColumn(col2);
		pf.setDiscriminateTextCase(true);

		pf.init();
		String[] resultLines;

		resultLines = new PatternFinderResultTextRenderer().render(pf.getResult()).split("\n");
		assertEquals(1, resultLines.length);
		assertEquals("No patterns found", resultLines[0]);

		pf.run(new MockInputRow().put(col1, "kasper").put(col2, null), 1);

		resultLines = new PatternFinderResultTextRenderer().render(pf.getResult()).split("\n");
		assertEquals("Patterns for group: null", resultLines[0]);
		assertEquals("       Match count Sample      ", resultLines[1]);
		assertEquals("aaaaaa           1 kasper      ", resultLines[2]);
		assertEquals(3, resultLines.length);

		pf.run(new MockInputRow().put(col1, "kasper").put(col2, "eobjects.dk"), 1);
		pf.run(new MockInputRow().put(col1, "kasper.sorensen").put(col2, "eobjects.dk"), 1);
		pf.run(new MockInputRow().put(col1, "kaspersorensen").put(col2, "eobjects.dk"), 1);
		pf.run(new MockInputRow().put(col1, "john").put(col2, "company.com"), 1);
		pf.run(new MockInputRow().put(col1, "doe").put(col2, "company.com"), 1);

		resultLines = new PatternFinderResultTextRenderer().render(pf.getResult()).split("\n");
		assertEquals("Patterns for group: null", resultLines[0]);
		assertEquals("       Match count Sample      ", resultLines[1]);
		assertEquals("aaaaaa           1 kasper      ", resultLines[2]);
		assertEquals("", resultLines[3]);
		assertEquals("Patterns for group: company.com", resultLines[4]);
		assertEquals("     Match count Sample      ", resultLines[5]);
		assertEquals("aaaa           2 john        ", resultLines[6]);
		assertEquals("", resultLines[7]);
		assertEquals("Patterns for group: eobjects.dk", resultLines[8]);
		assertEquals("                Match count Sample      ", resultLines[9]);
		assertEquals("aaaaaaaaaaaaaa            2 kasper      ", resultLines[10]);
		assertEquals("aaaaaa.aaaaaaaa           1 kasper.sorensen ", resultLines[11]);
		assertEquals(12, resultLines.length);
	}
}
