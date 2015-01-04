/**
 * AnalyzerBeans
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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.inject.Inject;

import org.eobjects.analyzer.beans.api.Analyzer;
import org.eobjects.analyzer.beans.api.AnalyzerBean;
import org.eobjects.analyzer.beans.api.Concurrent;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.Initialize;
import org.eobjects.analyzer.beans.api.Provided;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.result.AnnotatedRowsResult;
import org.eobjects.analyzer.result.CharacterSetDistributionResult;
import org.eobjects.analyzer.result.Crosstab;
import org.eobjects.analyzer.result.CrosstabDimension;
import org.eobjects.analyzer.result.CrosstabNavigator;
import org.eobjects.analyzer.storage.RowAnnotation;
import org.eobjects.analyzer.storage.RowAnnotationFactory;

import com.ibm.icu.text.UnicodeSet;

@AnalyzerBean("Character set distribution")
@Description("Inspects and maps text characters according to character set affinity, such as Latin, Hebrew, Cyrillic, Chinese and more.")
@Concurrent(true)
public class CharacterSetDistributionAnalyzer implements
		Analyzer<CharacterSetDistributionResult> {

	private static final Map<String, UnicodeSet> UNICODE_SETS = createUnicodeSets();

	@Inject
	@Configured
	InputColumn<String>[] _columns;

	@Inject
	@Provided
	RowAnnotationFactory _annotationFactory;

	private final Map<InputColumn<String>, CharacterSetDistributionAnalyzerColumnDelegate> _columnDelegates = new HashMap<InputColumn<String>, CharacterSetDistributionAnalyzerColumnDelegate>();

	@Initialize
	public void init() {
		for (InputColumn<String> column : _columns) {
			CharacterSetDistributionAnalyzerColumnDelegate delegate = new CharacterSetDistributionAnalyzerColumnDelegate(
					_annotationFactory, UNICODE_SETS);
			_columnDelegates.put(column, delegate);
		}
	}

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
		Map<String, UnicodeSet> unicodeSets = new TreeMap<String, UnicodeSet>();
		unicodeSets.put("Latin, ASCII", new UnicodeSet("[:ASCII:]"));
		unicodeSets.put("Latin, non-ASCII",
				subUnicodeSet("[:Latin:]", "[:ASCII:]"));
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

	private static UnicodeSet subUnicodeSet(String pattern1, String pattern2) {
		UnicodeSet unicodeSet = new UnicodeSet();
		unicodeSet.addAll(new UnicodeSet(pattern1));
		unicodeSet.removeAll(new UnicodeSet(pattern2));
		return unicodeSet;
	}

	@Override
	public void run(InputRow row, int distinctCount) {
		for (InputColumn<String> column : _columns) {
			String value = row.getValue(column);
			CharacterSetDistributionAnalyzerColumnDelegate delegate = _columnDelegates
					.get(column);
			delegate.run(value, row, distinctCount);
		}
	}

	@Override
	public CharacterSetDistributionResult getResult() {
		CrosstabDimension measureDimension = new CrosstabDimension("Measures");
		Set<String> unicodeSetNames = UNICODE_SETS.keySet();
		for (String name : unicodeSetNames) {
			measureDimension.addCategory(name);
		}

		CrosstabDimension columnDimension = new CrosstabDimension("Column");

		Crosstab<Number> crosstab = new Crosstab<Number>(Number.class,
				columnDimension, measureDimension);

		for (InputColumn<String> column : _columns) {
			String columnName = column.getName();
			CharacterSetDistributionAnalyzerColumnDelegate delegate = _columnDelegates
					.get(column);
			columnDimension.addCategory(columnName);

			CrosstabNavigator<Number> nav = crosstab.navigate().where(
					columnDimension, columnName);

			for (String name : unicodeSetNames) {
				RowAnnotation annotation = delegate.getAnnotation(name);
				int rowCount = annotation.getRowCount();
				nav.where(measureDimension, name).put(rowCount);
				if (rowCount > 0) {
					nav.attach(new AnnotatedRowsResult(annotation,
							_annotationFactory, column));
				}
			}
		}
		return new CharacterSetDistributionResult(_columns, unicodeSetNames,
				crosstab);
	}
}