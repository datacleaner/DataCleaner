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
package org.eobjects.analyzer.beans;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MockInputRow;
import org.eobjects.analyzer.result.AnnotatedRowsResult;
import org.eobjects.analyzer.result.CharacterSetDistributionResult;
import org.eobjects.analyzer.result.Crosstab;
import org.eobjects.analyzer.result.CrosstabNavigator;
import org.eobjects.analyzer.result.renderer.CrosstabTextRenderer;
import org.eobjects.analyzer.storage.InMemoryRowAnnotationFactory;
import org.apache.metamodel.util.EqualsBuilder;

import com.ibm.icu.text.UnicodeSet;

public class CharacterSetDistributionAnalyzerTest extends TestCase {

    private static final String CHARSET_NAMES = "[Arabic, Armenian, Bengali, Cyrillic, Devanagari, Georgian, Greek, Gujarati, Gurmukhi, Han, Hangul, Hebrew, Hiragana, Kannada, Katakana, Latin, ASCII, Latin, non-ASCII, Malayalam, Oriya, Syriac, Tamil, Telugu, Thaana, Thai]";

    public void testCreateFilters() throws Exception {
        Map<String, UnicodeSet> unicodeSets = CharacterSetDistributionAnalyzer.createUnicodeSets();
        Set<String> keys = unicodeSets.keySet();
        assertEquals(CHARSET_NAMES, keys.toString());

        UnicodeSet set = unicodeSets.get("Arabic");
        assertFalse(set.contains('a'));
        assertTrue(set.containsAll("البيانات"));

        set = unicodeSets.get("Latin, ASCII");
        assertTrue(set.contains('a'));
        assertTrue(set.contains('z'));
        assertFalse(set.contains('ä'));
        assertFalse(set.contains('æ'));

        set = unicodeSets.get("Latin, non-ASCII");
        assertFalse(set.contains('a'));
        assertFalse(set.contains('z'));
        assertTrue(set.contains('ä'));
        assertTrue(set.contains('æ'));
    }

    public void testSimpleScenario() throws Exception {
        CharacterSetDistributionAnalyzer analyzer = new CharacterSetDistributionAnalyzer();
        InputColumn<String> col1 = new MockInputColumn<String>("foo", String.class);
        InputColumn<String> col2 = new MockInputColumn<String>("bar", String.class);

        @SuppressWarnings("unchecked")
        InputColumn<String>[] cols = new InputColumn[] { col1, col2 };
        analyzer._columns = cols;
        analyzer._annotationFactory = new InMemoryRowAnnotationFactory();
        analyzer.init();

        analyzer.run(new MockInputRow().put(col1, "foobar").put(col2, "foobar"), 10);
        analyzer.run(new MockInputRow().put(col1, "DåtåClænør"), 1);
        analyzer.run(new MockInputRow().put(col1, "Данныечистого"), 1);
        analyzer.run(new MockInputRow().put(col1, "數據清潔"), 1);
        analyzer.run(new MockInputRow().put(col1, "بيانات الأنظف"), 1);
        analyzer.run(new MockInputRow().put(col1, "dữ liệu sạch hơn"), 1);

        CharacterSetDistributionResult result = analyzer.getResult();
        assertTrue(EqualsBuilder.equals(analyzer._columns, result.getColumns()));
        assertEquals(CHARSET_NAMES, Arrays.toString(result.getUnicodeSetNames()));

        Crosstab<?> crosstab = result.getCrosstab();
        assertEquals("[Column, Measures]", Arrays.toString(crosstab.getDimensionNames()));

        assertEquals(CHARSET_NAMES, crosstab.getDimension("Measures").getCategories().toString());

        CrosstabNavigator<?> cyrillicNavigation = crosstab.navigate().where("Column", "foo").where("Measures", "Cyrillic");
        assertEquals("1", cyrillicNavigation.get().toString());
        AnnotatedRowsResult cyrillicAnnotatedRowsResult = (AnnotatedRowsResult) cyrillicNavigation.explore().getResult();
        InputRow[] annotatedRows = cyrillicAnnotatedRowsResult.getRows();
        assertEquals(1, annotatedRows.length);
        assertEquals("Данныечистого", annotatedRows[0].getValue(col1));
        assertEquals("12", crosstab.navigate().where("Column", "foo").where("Measures", "Latin, ASCII").get().toString());
        assertEquals("2", crosstab.navigate().where("Column", "foo").where("Measures", "Latin, non-ASCII").get().toString());

        String resultString = new CrosstabTextRenderer().render(result);
        String[] resultLines = resultString.split("\n");
        assertEquals(25, resultLines.length);
        assertEquals("                    foo    bar ", resultLines[0]);
        assertEquals("Arabic                1      0 ", resultLines[1]);
        assertEquals("Armenian              0      0 ", resultLines[2]);
        assertEquals("Bengali               0      0 ", resultLines[3]);
        assertEquals("Cyrillic              1      0 ", resultLines[4]);
        assertEquals("Devanagari            0      0 ", resultLines[5]);
        assertEquals("Georgian              0      0 ", resultLines[6]);
        assertEquals("Greek                 0      0 ", resultLines[7]);
        assertEquals("Gujarati              0      0 ", resultLines[8]);
        assertEquals("Gurmukhi              0      0 ", resultLines[9]);
        assertEquals("Han                   1      0 ", resultLines[10]);
        assertEquals("Hangul                0      0 ", resultLines[11]);
        assertEquals("Hebrew                0      0 ", resultLines[12]);
        assertEquals("Hiragana              0      0 ", resultLines[13]);
        assertEquals("Kannada               0      0 ", resultLines[14]);
        assertEquals("Katakana              0      0 ", resultLines[15]);
        assertEquals("Latin, ASCII         12     10 ", resultLines[16]);
        assertEquals("Latin, non-ASCII      2      0 ", resultLines[17]);
        assertEquals("Malayalam             0      0 ", resultLines[18]);
        assertEquals("Oriya                 0      0 ", resultLines[19]);
        assertEquals("Syriac                0      0 ", resultLines[20]);
        assertEquals("Tamil                 0      0 ", resultLines[21]);
        assertEquals("Telugu                0      0 ", resultLines[22]);
        assertEquals("Thaana                0      0 ", resultLines[23]);
        assertEquals("Thai                  0      0 ", resultLines[24]);
    }
}