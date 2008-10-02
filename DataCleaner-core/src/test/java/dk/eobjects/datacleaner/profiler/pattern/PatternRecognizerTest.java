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
package dk.eobjects.datacleaner.profiler.pattern;

import java.util.Map;
import java.util.regex.Pattern;

import junit.framework.TestCase;

public class PatternRecognizerTest extends TestCase {

	public void testIdentifyAddressPatterns() throws Exception {
		PatternRecognizer patternRecognizer = new PatternRecognizer();
		patternRecognizer
				.addInstance("Osterbrogade 3, DK2100 Koebenhavn O", 1l);
		patternRecognizer.addInstance("Noerrebrogade 214, DK2200 Koebenhavn N",
				1l);
		patternRecognizer.addInstance("Osterbrogade 4, 2100 Koebenhavn O", 1l);
		patternRecognizer.addInstance("Noerrebrogade 215, 2200 Koebenhavn N",
				1l);
		patternRecognizer.addInstance("Byvej 2, 2. th, 2200 Koebenhavn N", 1l);

		Map<String, Long> patternMap = patternRecognizer.identifyPatterns();

		assertEquals(3, patternMap.size());

		Long patternCount = patternMap
				.get("aaaaaaaaaaaaa 999, 9999 aaaaaaaaaa a");
		assertEquals(2, patternCount.intValue());

		patternCount = patternMap.get("aaaaaaaaaaaaa 999, ?????? aaaaaaaaaa a");
		assertEquals(2, patternCount.intValue());

		patternCount = patternMap.get("aaaaa 9, 9. aa, 9999 aaaaaaaaaa a");
		assertEquals(1, patternCount.intValue());
	}

	public void testIdentifyNamePatterns() throws Exception {
		PatternRecognizer patternRecognizer = new PatternRecognizer();
		patternRecognizer.addInstance("Kasper Soerensen", 1l);
		patternRecognizer.addInstance("Mr. Kasper Soerensen", 1l);
		patternRecognizer.addInstance("Soerensen, Kasper", 1l);
		patternRecognizer.addInstance("Mr Kasper Soerensen", 1l);
		patternRecognizer.addInstance("Jesper Lind", 1l);
		Map<String, Long> patternMap = patternRecognizer.identifyPatterns();
		assertEquals(4, patternMap.size());

		Long patternCount = patternMap.get("aa. aaaaaa aaaaaaaaa");
		assertEquals(1, patternCount.intValue());

		patternCount = patternMap.get("aa aaaaaa aaaaaaaaa");
		assertEquals(1, patternCount.intValue());

		patternCount = patternMap.get("aaaaaa aaaaaaaaa");
		assertEquals(2, patternCount.intValue());

		patternCount = patternMap.get("aaaaaaaaa, aaaaaa");
		assertEquals(1, patternCount.intValue());
	}

	public void testSingleCharacter() throws Exception {
		PatternRecognizer patternRecognizer = new PatternRecognizer();
		patternRecognizer.addInstance("a", 1l);
		patternRecognizer.addInstance("b", 1l);
		Map<String, Long> patterns = patternRecognizer.identifyPatterns();
		assertEquals(1, patterns.size());
		assertEquals(2, patterns.get("a").intValue());
	}

	public void testNumberInput() throws Exception {
		PatternRecognizer patternRecognizer = new PatternRecognizer();
		patternRecognizer.addInstance("124", 1l);
		patternRecognizer.addInstance("4", 1l);
		patternRecognizer.addInstance("4324", 1l);
		patternRecognizer.addInstance("543", 1l);
		patternRecognizer.addInstance("2", 1l);
		patternRecognizer.addInstance("31", 1l);
		patternRecognizer.addInstance("943242872", 1l);

		Map<String, Long> patternMap = patternRecognizer.identifyPatterns();
		assertEquals(1, patternMap.size());
		assertEquals(7, patternMap.get("999999999").intValue());
	}

	public void testToStringCompliance() throws Exception {
		PatternRecognizer patternRecognizer = new PatternRecognizer();
		patternRecognizer.addInstance("Kasper Soerensen", 1l);
		patternRecognizer.addInstance("Mr. Kasper Soerensen", 1l);
		patternRecognizer.addInstance("Soerensen, Kasper", 1l);
		patternRecognizer.addInstance("Mr Kasper Soerensen", 1l);
		patternRecognizer.addInstance("Jesper Lind", 1l);

		Map<String, Long> patternMap = patternRecognizer.identifyPatterns();
		assertEquals(
				"{aaaaaa aaaaaaaaa=2, aa aaaaaa aaaaaaaaa=1, aaaaaaaaa, aaaaaa=1, aa. aaaaaa aaaaaaaaa=1}",
				patternMap.toString());
	}

	public void testPatternEquals() throws Exception {
		PatternRecognizer patternRecognizer = new PatternRecognizer();
		patternRecognizer.addInstance("Kasper Soerensen", 1l);
		patternRecognizer.addInstance("Asbjoern Leeth", 1l);
		Map<String, Long> patterns = patternRecognizer.identifyPatterns();
		assertEquals("{aaaaaaaa aaaaaaaaa=2}", patterns.toString());

		assertTrue(patternRecognizer.patternEquals("aaaaaaaa aaaaaaaaa",
				"Kasp Soeren"));
		assertFalse(patternRecognizer.patternEquals("aaaaaaaa aaaaaaaaa",
				"Kasp Something-with-mixed"));
		assertFalse(patternRecognizer.patternEquals("aaaaaaaa aaaaaaaaa",
				"Kasp er Soerensen"));
	}

	public void testGetRegexSimple() throws Exception {
		PatternRecognizer patternRecognizer = new PatternRecognizer();
		patternRecognizer.addInstance("Kasper Soerensen", 1l);
		patternRecognizer.addInstance("Asbjoern Leeth", 1l);
		Map<String, Long> patterns = patternRecognizer.identifyPatterns();
		assertEquals("{aaaaaaaa aaaaaaaaa=2}", patterns.toString());

		Pattern regex = patternRecognizer.getRegex("aaaaaaaa aaaaaaaaa");
		assertEquals("[a-zA-Z]{1,8}\\Q \\E[a-zA-Z]{1,9}", regex.pattern());
		assertTrue(regex.matcher("Kasper Soerensen").matches());
		assertTrue(regex.matcher("Asbjoern Leeth").matches());
	}

	public void testGetRegex() throws Exception {
		PatternRecognizer patternRecognizer = new PatternRecognizer();
		patternRecognizer.addInstance("Which OS do you like the most?", 1l);
		patternRecognizer.addInstance("I'd prefer not 2 use Windows95!", 1l);
		patternRecognizer.addInstance("Which OS is then the best one?", 1l);
		Map<String, Long> patterns = patternRecognizer.identifyPatterns();
		assertEquals(
				"{aaaaa aa aa aaaa aaaa aaaa aaaa?=2, a'a aaaaaa aaa 9 aaa ?????????!=1}",
				patterns.toString());
		String[] patternsStrings = patterns.keySet().toArray(new String[2]);
		assertEquals("aaaaa aa aa aaaa aaaa aaaa aaaa?", patternsStrings[0]);
		Pattern regex = patternRecognizer.getRegex(patternsStrings[0]);

		assertEquals(
				"[a-zA-Z]{1,5}\\Q \\E[a-zA-Z]{1,2}\\Q \\E[a-zA-Z]{1,2}\\Q \\E[a-zA-Z]{1,4}\\Q \\E[a-zA-Z]{1,4}\\Q \\E[a-zA-Z]{1,4}\\Q \\E[a-zA-Z]{1,4}\\Q?\\E",
				regex.pattern());
		assertTrue(regex.matcher("Which OS do you like the most?").matches());
		assertTrue(regex.matcher("Which OS is then the best one?").matches());
		assertFalse(regex.matcher(
				"Which Operating System is then the best one?").matches());

		assertEquals("a'a aaaaaa aaa 9 aaa ?????????!", patternsStrings[1]);
		regex = patternRecognizer.getRegex(patternsStrings[1]);
		assertEquals(
				"[a-zA-Z]{1,1}\\Q'\\E[a-zA-Z]{1,1}\\Q \\E[a-zA-Z]{1,6}\\Q \\E[a-zA-Z]{1,3}\\Q \\E[0-9]{1,1}\\Q \\E[a-zA-Z]{1,3}\\Q \\E[a-zA-Z0-9]{1,9}\\Q!\\E",
				regex.pattern());
		assertTrue(regex.matcher("I'd prefer not 2 use Windows95!").matches());
		assertFalse(regex.matcher("I'd prefer not 2 use Windows Vista!")
				.matches());
	}
}