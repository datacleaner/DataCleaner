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
package org.datacleaner.beans.transform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.datacleaner.api.InputColumn;
import org.datacleaner.beans.transform.TextCaseTransformer.TransformationMode;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;
import org.datacleaner.reference.Dictionary;
import org.datacleaner.reference.SimpleDictionary;
import org.junit.Before;
import org.junit.Test;

public class TextCaseTransformerTest {
    private TextCaseTransformer _textCaseTransformer;
    private InputColumn<String> _nameColumn;
    private MockInputRow _inputRow;

    @Before
    public void before() {
        _nameColumn = new MockInputColumn<>("Given Name", String.class);
        _inputRow = new MockInputRow();
        _textCaseTransformer = new TextCaseTransformer();
        _textCaseTransformer.valueColumn = _nameColumn;

    }

    @Test
    public void testCapitalizeEveryWord() {
        _textCaseTransformer.mode = TransformationMode.CAPITALIZE_WORDS;
        putData("Klaus-Dieter");
        final String[] result = _textCaseTransformer.transform(_inputRow);
        assertNotNull(result);
        assertEquals("Klaus-Dieter", result[0]);

        putData("Ulla Bonde");
        final String[] result1 = _textCaseTransformer.transform(_inputRow);
        assertNotNull(result1);
        assertEquals("Ulla Bonde", result1[0]);

        putData("Douwe johannes");
        final String[] result2 = _textCaseTransformer.transform(_inputRow);
        assertNotNull(result2);
        assertEquals("Douwe Johannes", result2[0]);

        putData("STEVEN");
        final String[] result3 = _textCaseTransformer.transform(_inputRow);
        assertNotNull(result3);
        assertEquals("Steven", result3[0]);

        putData("poul Krebs");
        final String[] result4 = _textCaseTransformer.transform(_inputRow);
        assertNotNull(result4);
        assertEquals("Poul Krebs", result4[0]);

        putData("The poul   krebs");
        final String[] result5 = _textCaseTransformer.transform(_inputRow);
        assertNotNull(result5);
        assertEquals("The Poul   Krebs", result5[0]);

    }

    @Test
    public void testUppercase() {
        _textCaseTransformer.mode = TransformationMode.UPPER_CASE;
        putData("The poul   krebs");
        final String[] result = _textCaseTransformer.transform(_inputRow);
        assertNotNull(result);
        assertEquals("THE POUL   KREBS", result[0]);
    }

    @Test
    public void testLowercase() {
        _textCaseTransformer.mode = TransformationMode.LOWER_CASE;
        putData("The PAUL   Krebs");
        final String[] result = _textCaseTransformer.transform(_inputRow);
        assertNotNull(result);
        assertEquals("the paul   krebs", result[0]);
    }

    @Test
    public void testCapitalizeSentances() {
        _textCaseTransformer.mode = TransformationMode.CAPITALIZE_SENTENCES;
        final String result = _textCaseTransformer.transform("i have a good day.");
        assertEquals("I have a good day.", result);
    }

    @Test
    public void testCapitalizeSentences() {
        final TextCaseTransformer transformer = new TextCaseTransformer();
        transformer.mode = TextCaseTransformer.TransformationMode.CAPITALIZE_SENTENCES;

        assertEquals("Hello world. Foo bar! Foo", transformer.transform("hello World. FOO baR! foo"));
        assertEquals("Hello world. Foo bar:\n Foo", transformer.transform("hello World. FOO baR:\n foo"));
    }

    @Test
    public void testCapitalizeWords() {
        final TextCaseTransformer transformer = new TextCaseTransformer();
        transformer.mode = TextCaseTransformer.TransformationMode.CAPITALIZE_WORDS;

        assertEquals("Hello World. Foo Bar! Foo", transformer.transform("hello World. FOO baR! foo"));
        assertEquals("Hello World. Foo Bar:\n Foo", transformer.transform("hello World. FOO baR:\n foo"));
    }

    @Test
    public void testCapitalizeWordsAllWordsDictionary() {
        final TextCaseTransformer transformer = new TextCaseTransformer();
        transformer.mode = TextCaseTransformer.TransformationMode.CAPITALIZE_WORDS;
        transformer.allWordsDictionaries = new Dictionary[] { new SimpleDictionary("dict A", "What a day!"),
                new SimpleDictionary("dict B", "Day A-WHA?") };
        transformer.init();

        assertEquals("Hello World. Foo Bar! Foo", transformer.transform("hello World. FOO baR! foo"));
        assertEquals("Hello World. Foo Bar:\n Foo", transformer.transform("hello World. FOO baR:\n foo"));
        assertEquals("What a day!", transformer.transform("what A DAY!"));
        assertEquals("Day A-WHA?", transformer.transform("day a-wha?"));
    }

    @Test
    public void testCapitalizeWordsWordsDictionary() {
        final TextCaseTransformer transformer = new TextCaseTransformer();
        transformer.mode = TextCaseTransformer.TransformationMode.CAPITALIZE_WORDS;
        transformer.wordDictionaries = new Dictionary[] { new SimpleDictionary("dict A", "FOO", "foohoo"),
                new SimpleDictionary("dict B", "BAR", "barhar") };
        transformer.init();

        assertEquals("Hello World. FOO BAR! FOO", transformer.transform("hello World. FOO baR! foo"));
        assertEquals("Hello World. FOO BAR:\n FOO", transformer.transform("hello World. FOO baR:\n foo"));
        assertEquals("Hello World. foohoo barhar:\n FOO", transformer.transform("hello World. FOOhoo baRhar:\n foo"));

    }

    @Test
    public void testCapitalizeWordEndsDictionary() {
        final TextCaseTransformer transformer = new TextCaseTransformer();
        transformer.mode = TextCaseTransformer.TransformationMode.CAPITALIZE_WORDS;
        transformer.wordStartDictionaries = new Dictionary[] { new SimpleDictionary("dict start A", "O'S"),
                new SimpleDictionary("dict start B", "O'L") };
        transformer.wordEndDictionaries = new Dictionary[] { new SimpleDictionary("dict end A", "FOOO") };
        transformer.init();

        assertEquals("O'Sullivan's Cars", transformer.transform("o'sullivan'S cars"));
        assertEquals("O'Leary", transformer.transform("o'leary"));
        assertEquals("MartinFOOO", transformer.transform("martinfooo"));
    }

    @Test
    public void testCapitalizeDictionaryPriority() {
        final TextCaseTransformer transformer = new TextCaseTransformer();
        transformer.mode = TextCaseTransformer.TransformationMode.CAPITALIZE_WORDS;
        transformer.allWordsDictionaries = new Dictionary[] { new SimpleDictionary("dict A", "O'MustMATCHfooo Bar") };
        transformer.wordDictionaries = new Dictionary[] { new SimpleDictionary("dict A", "O'mustMatchFooo") };
        transformer.wordStartDictionaries = new Dictionary[] { new SimpleDictionary("dict start A", "O'S"), };
        transformer.wordEndDictionaries = new Dictionary[] { new SimpleDictionary("dict end A", "FOOO") };
        transformer.init();

        assertEquals("O'MustMATCHfooo Bar", transformer.transform("o'mustmatchfooo BAR"));
        assertEquals("O'mustMatchFooo", transformer.transform("o'mustmatchfooo"));
    }

    @Test
    public void testUpperCase() {
        final TextCaseTransformer transformer = new TextCaseTransformer();
        transformer.mode = TextCaseTransformer.TransformationMode.UPPER_CASE;

        assertEquals("HELLO WORLD. FOO BAR! FOO", transformer.transform("hello World. FOO baR! foo"));
    }

    @Test
    public void testLowerCase() {
        final TextCaseTransformer transformer = new TextCaseTransformer();
        transformer.mode = TextCaseTransformer.TransformationMode.LOWER_CASE;

        assertEquals("hello world. foo bar! foo", transformer.transform("hello World. FOO baR! foo"));
    }

    private void putData(final String name) {
        _inputRow.put(_nameColumn, name);
    }
}
