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

import java.util.ArrayList;
import java.util.List;

import org.eobjects.analyzer.beans.api.Alias;
import org.eobjects.analyzer.beans.api.Analyzer;
import org.eobjects.analyzer.beans.api.AnalyzerBean;
import org.eobjects.analyzer.beans.api.Categorized;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.Initialize;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.Validate;
import org.eobjects.analyzer.beans.categories.ValidationCategory;
import org.eobjects.analyzer.beans.convert.ConvertToStringTransformer;
import org.eobjects.analyzer.beans.transform.DictionaryMatcherTransformer;
import org.eobjects.analyzer.beans.transform.StringPatternMatcherTransformer;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MockInputRow;
import org.eobjects.analyzer.reference.Dictionary;
import org.eobjects.analyzer.reference.StringPattern;
import org.eobjects.analyzer.reference.SynonymCatalog;

@AnalyzerBean("Reference data matcher")
@Alias("Matching analyzer")
@Description("Check your data values against multiple forms of reference data in one simple analyzer step.\n"
		+ "This analyzer provides a handy shortcut for doing matching with dictionaries, synonym lookups or string patterns matching, retrieving matching matrices for all matches.")
@Categorized(ValidationCategory.class)
public class ReferenceDataMatcherAnalyzer implements Analyzer<BooleanAnalyzerResult> {

	@Configured(order = 1)
	InputColumn<?>[] columns;

	@Configured(order = 2, required = false)
	Dictionary[] dictionaries;

	@Configured(order = 3, required = false)
	SynonymCatalog[] synonymCatalogs;

	@Configured(order = 4, required = false)
	StringPattern[] stringPatterns;

	private BooleanAnalyzer _booleanAnalyzer;
	private DictionaryMatcherTransformer[] _dictionaryMatchers;
	private StringPatternMatcherTransformer[] _stringPatternMatchers;
	private List<InputColumn<Boolean>> _matchColumns;

	public ReferenceDataMatcherAnalyzer(InputColumn<?>[] columns,
			Dictionary[] dictionaries, SynonymCatalog[] synonymCatalogs,
			StringPattern[] stringPatterns) {
		this();
		this.columns = columns;
		this.dictionaries = dictionaries;
		this.stringPatterns = stringPatterns;
		this.synonymCatalogs = synonymCatalogs;
	}

	public ReferenceDataMatcherAnalyzer() {
	}

	@Validate
	public void validate() {
		if (!isDictionaryMatchingEnabled() && !isSynonymCatalogLookupEnabled()
				&& !isStringPatternMatchingEnabled()) {
			throw new IllegalStateException(
					"No dictionaries, synonym catalogs or string patterns selected");
		}
	}

	@Initialize
	public void init() {
		_dictionaryMatchers = new DictionaryMatcherTransformer[columns.length];
		_stringPatternMatchers = new StringPatternMatcherTransformer[columns.length];

		_matchColumns = new ArrayList<InputColumn<Boolean>>();

		OutputColumns outputColumns;
		for (int i = 0; i < columns.length; i++) {
			if (isDictionaryMatchingEnabled()) {
				// create matcher for dictionaries
				DictionaryMatcherTransformer dictionaryMatcher = new DictionaryMatcherTransformer(
						columns[i], dictionaries);
				outputColumns = dictionaryMatcher.getOutputColumns();
				addMatchColumns(outputColumns);
				_dictionaryMatchers[i] = dictionaryMatcher;
			}

			if (isSynonymCatalogLookupEnabled()) {
				outputColumns = new OutputColumns(synonymCatalogs.length);
				for (int j = 0; j < synonymCatalogs.length; j++) {
					SynonymCatalog synonymCatalog = synonymCatalogs[j];
					outputColumns.setColumnType(j, Boolean.class);
					outputColumns.setColumnName(j, columns[i].getName()
							+ " in " + synonymCatalog.getName());
				}
				addMatchColumns(outputColumns);
			}

			if (isStringPatternMatchingEnabled()) {
				// create matcher for string patterns
				StringPatternMatcherTransformer stringPatternMatcher = new StringPatternMatcherTransformer(
						columns[i], stringPatterns);
				outputColumns = stringPatternMatcher.getOutputColumns();
				addMatchColumns(outputColumns);
				_stringPatternMatchers[i] = stringPatternMatcher;
			}
		}

		@SuppressWarnings("unchecked")
		InputColumn<Boolean>[] columnArray = _matchColumns
				.toArray(new InputColumn[_matchColumns.size()]);
		_booleanAnalyzer = new BooleanAnalyzer(columnArray);
		_booleanAnalyzer.init();
	}

	private boolean isStringPatternMatchingEnabled() {
		return stringPatterns != null && stringPatterns.length > 0;
	}

	private boolean isSynonymCatalogLookupEnabled() {
		return synonymCatalogs != null && synonymCatalogs.length > 0;
	}

	private boolean isDictionaryMatchingEnabled() {
		return dictionaries != null && dictionaries.length > 0;
	}

	private void addMatchColumns(OutputColumns outputColumns) {
		int count = outputColumns.getColumnCount();
		for (int i = 0; i < count; i++) {
			String columnName = outputColumns.getColumnName(i);
			InputColumn<Boolean> col = new MockInputColumn<Boolean>(columnName,
					Boolean.class);
			_matchColumns.add(col);
		}
	}

	@Override
	public void run(InputRow row, int distinctCount) {
		MockInputRow mockInputRow = new MockInputRow();

		int matchColumnIndex = 0;
		for (int i = 0; i < columns.length; i++) {
			final Object value = row.getValue(columns[i]);
			final String stringValue = ConvertToStringTransformer
					.transformValue(value);
			mockInputRow.put(columns[i], value);

			if (isDictionaryMatchingEnabled()) {
				Object[] matches = _dictionaryMatchers[i].transform(row);
				for (Object match : matches) {
					assert match instanceof Boolean;

					InputColumn<Boolean> matchColumn = _matchColumns
							.get(matchColumnIndex);
					matchColumnIndex++;
					mockInputRow.put(matchColumn, match);
				}
			}

			if (isSynonymCatalogLookupEnabled()) {
				for (SynonymCatalog synonymCatalog : synonymCatalogs) {
					final InputColumn<Boolean> matchColumn = _matchColumns
							.get(matchColumnIndex);
					matchColumnIndex++;
					final String masterTerm = synonymCatalog
							.getMasterTerm(stringValue);
					if (masterTerm == null) {
						// no match
						mockInputRow.put(matchColumn, Boolean.FALSE);
					} else {
						mockInputRow.put(matchColumn, Boolean.TRUE);
					}
				}
			}

			if (isStringPatternMatchingEnabled()) {
				Object[] matches = _stringPatternMatchers[i].transform(row);
				for (Object match : matches) {
					assert match instanceof Boolean;
					InputColumn<Boolean> matchColumn = _matchColumns
							.get(matchColumnIndex);
					matchColumnIndex++;
					mockInputRow.put(matchColumn, match);
				}
			}
		}

		_booleanAnalyzer.run(mockInputRow, distinctCount);
	}

	@Override
	public BooleanAnalyzerResult getResult() {
		return _booleanAnalyzer.getResult();
	}
}
