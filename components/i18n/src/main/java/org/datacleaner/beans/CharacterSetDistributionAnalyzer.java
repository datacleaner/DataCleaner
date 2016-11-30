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
package org.datacleaner.beans;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.inject.Inject;
import javax.inject.Named;

import org.datacleaner.api.Analyzer;
import org.datacleaner.api.Concurrent;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.ExternalDocumentation;
import org.datacleaner.api.ExternalDocumentation.DocumentationLink;
import org.datacleaner.api.ExternalDocumentation.DocumentationType;
import org.datacleaner.api.Initialize;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.Provided;
import org.datacleaner.result.AnnotatedRowsResult;
import org.datacleaner.result.CharacterSetDistributionResult;
import org.datacleaner.result.Crosstab;
import org.datacleaner.result.CrosstabDimension;
import org.datacleaner.result.CrosstabNavigator;
import org.datacleaner.storage.RowAnnotation;
import org.datacleaner.storage.RowAnnotationFactory;

import com.ibm.icu.text.UnicodeSet;

@Named("Character set distribution")
@Description("Inspects and maps text characters according to character set affinity, "
        + "such as Latin, Hebrew, Cyrillic, Chinese and more.")
@ExternalDocumentation({ @DocumentationLink(title = "Internationalization in DataCleaner",
        url = "https://www.youtube.com/watch?v=ApA-nhtLbhI", type = DocumentationType.VIDEO, version = "3.0") })
@Concurrent(true)
public class CharacterSetDistributionAnalyzer implements Analyzer<CharacterSetDistributionResult> {

    private static final Map<String, UnicodeSet> UNICODE_SETS = createUnicodeSets();
    private final Map<InputColumn<String>, CharacterSetDistributionAnalyzerColumnDelegate> _columnDelegates =
            new HashMap<>();
    @Inject
    @Configured
    InputColumn<String>[] _columns;
    @Inject
    @Provided
    RowAnnotationFactory _annotationFactory;

    /**
     * Creates a map of unicode sets, with their names as keys.
     *
     * There's a usable list of Unicode scripts on this page:
     * http://unicode.org/cldr/utility/properties.jsp?a=Script#Script
     *
     * Additionally, this page has some explanations on some of the more exotic
     * sources, like japanese:
     * http://userguide.icu-project.org/transforms/general#TOC-Japanese
     *
     * @return
     */
    protected static Map<String, UnicodeSet> createUnicodeSets() {
        final Map<String, UnicodeSet> unicodeSets = new TreeMap<>();
        unicodeSets.put("Latin, ASCII", new UnicodeSet("[:ASCII:]"));
        unicodeSets.put("Latin, non-ASCII", subUnicodeSet("[:Latin:]", "[:ASCII:]"));
        unicodeSets.put("Arabic", new UnicodeSet("[:Script=Arabic:]"));
        unicodeSets.put("Armenian", new UnicodeSet("[:Script=Armenian:]"));
        unicodeSets.put("Bengali", new UnicodeSet("[:Script=Bengali:]"));
        unicodeSets.put("Cyrillic", new UnicodeSet("[:Script=Cyrillic:]"));
        unicodeSets.put("Devanagari", new UnicodeSet("[:Script=Devanagari:]"));
        unicodeSets.put("Greek", new UnicodeSet("[:Script=Greek:]"));
        unicodeSets.put("Han", new UnicodeSet("[:Script=Han:]"));
        unicodeSets.put("Gujarati", new UnicodeSet("[:Script=Gujarati:]"));
        unicodeSets.put("Georgian", new UnicodeSet("[:Script=Georgian:]"));
        unicodeSets.put("Gurmukhi", new UnicodeSet("[:Script=Gurmukhi:]"));
        unicodeSets.put("Hangul", new UnicodeSet("[:Script=Hangul:]"));
        unicodeSets.put("Hebrew", new UnicodeSet("[:Script=Hebrew:]"));
        unicodeSets.put("Hiragana", new UnicodeSet("[:Script=Hiragana:]"));
        // unicodeSets.put("Kanji", new UnicodeSet("[:Script=Kanji:]"));
        unicodeSets.put("Kannada", new UnicodeSet("[:Script=Kannada:]"));
        unicodeSets.put("Katakana", new UnicodeSet("[:Script=Katakana:]"));
        unicodeSets.put("Malayalam", new UnicodeSet("[:Script=Malayalam:]"));
        // unicodeSets.put("Mandarin", new UnicodeSet("[:Script=Mandarin:]"));
        unicodeSets.put("Oriya", new UnicodeSet("[:Script=Oriya:]"));
        unicodeSets.put("Syriac", new UnicodeSet("[:Script=Syriac:]"));
        unicodeSets.put("Tamil", new UnicodeSet("[:Script=Tamil:]"));
        unicodeSets.put("Telugu", new UnicodeSet("[:Script=Telugu:]"));
        unicodeSets.put("Thaana", new UnicodeSet("[:Script=Thaana:]"));
        unicodeSets.put("Thai", new UnicodeSet("[:Script=Thai:]"));
        return unicodeSets;
    }

    private static UnicodeSet subUnicodeSet(final String pattern1, final String pattern2) {
        final UnicodeSet unicodeSet = new UnicodeSet();
        unicodeSet.addAll(new UnicodeSet(pattern1));
        unicodeSet.removeAll(new UnicodeSet(pattern2));
        return unicodeSet;
    }

    @Initialize
    public void init() {
        for (final InputColumn<String> column : _columns) {
            final CharacterSetDistributionAnalyzerColumnDelegate delegate =
                    new CharacterSetDistributionAnalyzerColumnDelegate(_annotationFactory, UNICODE_SETS);
            _columnDelegates.put(column, delegate);
        }
    }

    @Override
    public void run(final InputRow row, final int distinctCount) {
        for (final InputColumn<String> column : _columns) {
            final String value = row.getValue(column);
            final CharacterSetDistributionAnalyzerColumnDelegate delegate = _columnDelegates.get(column);
            delegate.run(value, row, distinctCount);
        }
    }

    @Override
    public CharacterSetDistributionResult getResult() {
        final CrosstabDimension measureDimension = new CrosstabDimension("Measures");
        final Set<String> unicodeSetNames = UNICODE_SETS.keySet();
        for (final String name : unicodeSetNames) {
            measureDimension.addCategory(name);
        }

        final CrosstabDimension columnDimension = new CrosstabDimension("Column");

        final Crosstab<Number> crosstab = new Crosstab<>(Number.class, columnDimension, measureDimension);

        for (final InputColumn<String> column : _columns) {
            final String columnName = column.getName();
            final CharacterSetDistributionAnalyzerColumnDelegate delegate = _columnDelegates.get(column);
            columnDimension.addCategory(columnName);

            final CrosstabNavigator<Number> nav = crosstab.navigate().where(columnDimension, columnName);

            for (final String name : unicodeSetNames) {
                final RowAnnotation annotation = delegate.getAnnotation(name);
                final int rowCount = annotation.getRowCount();
                nav.where(measureDimension, name).put(rowCount);
                if (rowCount > 0) {
                    nav.attach(new AnnotatedRowsResult(annotation, _annotationFactory, column));
                }
            }
        }

        return new CharacterSetDistributionResult(_columns, unicodeSetNames, crosstab);
    }
}
