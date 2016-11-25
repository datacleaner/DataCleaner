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

import java.io.Serializable;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.inject.Named;

import org.datacleaner.api.Analyzer;
import org.datacleaner.api.ColumnProperty;
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
import org.datacleaner.result.Crosstab;
import org.datacleaner.result.CrosstabDimension;
import org.datacleaner.result.CrosstabNavigator;
import org.datacleaner.storage.RowAnnotation;
import org.datacleaner.storage.RowAnnotationFactory;
import org.datacleaner.util.NullTolerableComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named("Pattern finder")
@Description( "The Pattern Finder will inspect your String values and generate and match string patterns that suit "
        + "your data.\nIt can be used for a lot of purposes but is excellent for verifying or getting ideas about "
        + "the format of the string-values in a column.")
@ExternalDocumentation(value = { @DocumentationLink(title = "Kasper's Source: Pattern Finder 2.0",
        url = "http://kasper.eobjects.org/2010/09/pattern-finder-20-latest-feature-in.html",
        type = DocumentationType.TECH, version = "2.0") })
@Concurrent(true)
public class PatternFinderAnalyzer implements Analyzer<PatternFinderResult> {

    public static final String PROPERTY_COLUMN = "Column";
    public static final String PROPERTY_GROUP_COLUMN = "Group column";
    public static final String PROPERTY_DISCRIMINATE_TEXT_CASE = "Discriminate text case";
    public static final String PROPERTY_DISCRIMINATE_NEGATIVE_NUMBERS = "Discriminate negative numbers";
    public static final String PROPERTY_DISCRIMINATE_DECIMALS = "Discriminate decimals";
    public static final String PROPERTY_ENABLE_MIXED_TOKENS = "Enable mixed tokens";
    public static final String PROPERTY_IGNORE_REPEATED_SPACES = "Ignore repeated spaces";
    public static final String MEASURE_SAMPLE = "Sample";
    public static final String MEASURE_MATCH_COUNT = "Match count";
    public static final String DIMENSION_NAME_MEASURES = "Measures";
    public static final String DIMENSION_NAME_PATTERN = "Pattern";
    private static final Logger logger = LoggerFactory.getLogger(PatternFinderAnalyzer.class);
    @Configured(order = 1, value = PROPERTY_COLUMN)
    @ColumnProperty(escalateToMultipleJobs = true)
    InputColumn<String> column;

    @Configured(required = false, order = 2, value = PROPERTY_GROUP_COLUMN)
    @Description("Optional column to group patterns by")
    InputColumn<String> groupColumn;

    @Configured(required = false, order = 3, value = PROPERTY_DISCRIMINATE_TEXT_CASE)
    @Description("Separate text tokens based on case")
    boolean discriminateTextCase = true;

    @Configured(required = false, order = 4, value = PROPERTY_DISCRIMINATE_NEGATIVE_NUMBERS)
    @Description("Separate number tokens based on negativity")
    boolean discriminateNegativeNumbers = false;

    @Configured(required = false, order = 5, value = PROPERTY_DISCRIMINATE_DECIMALS)
    @Description("Separate number tokens for decimals")
    boolean discriminateDecimals = true;

    @Configured(required = false, order = 6, value = PROPERTY_ENABLE_MIXED_TOKENS)
    @Description("Use '?'-tokens for mixed text and numbers")
    boolean enableMixedTokens = true;

    @Configured(required = false, order = 7, value = PROPERTY_IGNORE_REPEATED_SPACES)
    @Description("Ignore whitespace differences")
    boolean ignoreRepeatedSpaces = false;

    @Configured(required = false, value = "Upper case patterns expand in size", order = 8)
    @Description("Auto-adjust/expand uppercase text tokens")
    boolean upperCaseExpandable = false;

    @Configured(required = false, value = "Lower case patterns expand in size", order = 9)
    @Description("Auto-adjust/expand lowercase text tokens")
    boolean lowerCaseExpandable = true;

    @Configured(required = false, value = "Predefined token name", order = 10)
    String predefinedTokenName;

    @Configured(required = false, value = "Predefined token regexes", order = 11)
    String[] predefinedTokenPatterns;

    @Configured(required = false, order = 12)
    Character decimalSeparator = DecimalFormatSymbols.getInstance().getDecimalSeparator();

    @Configured(required = false, order = 13)
    Character thousandsSeparator = DecimalFormatSymbols.getInstance().getGroupingSeparator();

    @Configured(required = false, order = 14)
    Character minusSign = DecimalFormatSymbols.getInstance().getMinusSign();
    @Provided
    RowAnnotationFactory _rowAnnotationFactory;
    private Map<String, DefaultPatternFinder> _patternFinders;
    private TokenizerConfiguration _configuration;

    public static Crosstab<Serializable> createCrosstab() {
        final CrosstabDimension measuresDimension = new CrosstabDimension(DIMENSION_NAME_MEASURES);
        measuresDimension.addCategory(MEASURE_MATCH_COUNT);
        measuresDimension.addCategory(MEASURE_SAMPLE);
        final CrosstabDimension patternDimension = new CrosstabDimension(DIMENSION_NAME_PATTERN);
        return new Crosstab<>(Serializable.class, measuresDimension, patternDimension);
    }

    @Initialize
    public void init() {
        _configuration = new TokenizerConfiguration(enableMixedTokens);

        _configuration.setUpperCaseExpandable(upperCaseExpandable);
        _configuration.setLowerCaseExpandable(lowerCaseExpandable);

        _configuration.setDiscriminateNegativeNumbers(discriminateNegativeNumbers);
        _configuration.setDiscriminateDecimalNumbers(discriminateDecimals);
        _configuration.setDiscriminateTextCase(discriminateTextCase);
        _configuration.setDistriminateTokenLength(TokenType.WHITESPACE, !ignoreRepeatedSpaces);

        if (decimalSeparator != null) {
            _configuration.setDecimalSeparator(decimalSeparator);
        }

        if (thousandsSeparator != null) {
            _configuration.setThousandsSeparator(thousandsSeparator);
        }

        if (minusSign != null) {
            _configuration.setMinusSign(minusSign);
        }

        if (predefinedTokenName != null && predefinedTokenPatterns != null) {
            final Set<String> tokenRegexes = new HashSet<>();
            for (final String predefinedTokenPattern : predefinedTokenPatterns) {
                tokenRegexes.add(predefinedTokenPattern);
            }
            _configuration.getPredefinedTokens().add(new PredefinedTokenDefinition(predefinedTokenName, tokenRegexes));
        }

        _patternFinders = new HashMap<>();
    }

    @Override
    public void run(final InputRow row, final int distinctCount) {
        final String group;
        if (groupColumn == null) {
            group = null;
        } else {
            group = row.getValue(groupColumn);
        }
        final String value = row.getValue(column);

        run(group, value, row, distinctCount);
    }

    private void run(final String group, final String value, final InputRow row, final int distinctCount) {
        final DefaultPatternFinder patternFinder = getPatternFinderForGroup(group);
        patternFinder.run(row, value, distinctCount);
    }

    private DefaultPatternFinder getPatternFinderForGroup(final String group) {
        DefaultPatternFinder patternFinder = _patternFinders.get(group);
        if (patternFinder == null) {
            synchronized (this) {
                patternFinder = _patternFinders.get(group);
                if (patternFinder == null) {
                    patternFinder = new DefaultPatternFinder(_configuration, _rowAnnotationFactory);
                    _patternFinders.put(group, patternFinder);
                }
            }
        }
        return patternFinder;
    }

    @Override
    public PatternFinderResult getResult() {
        if (groupColumn == null) {
            final Crosstab<?> crosstab = createCrosstab(getPatternFinderForGroup(null));
            return new PatternFinderResult(column, crosstab, _configuration);
        } else {
            final Map<String, Crosstab<?>> crosstabs = new TreeMap<>(NullTolerableComparator.get(String.class));
            final Set<Entry<String, DefaultPatternFinder>> patternFinderEntries = _patternFinders.entrySet();
            for (final Entry<String, DefaultPatternFinder> entry : patternFinderEntries) {
                final DefaultPatternFinder patternFinder = entry.getValue();
                final Crosstab<Serializable> crosstab = createCrosstab(patternFinder);
                crosstabs.put(entry.getKey(), crosstab);
            }
            if (logger.isInfoEnabled()) {
                logger.info("Grouped result contains {} groups", crosstabs.size());
            }
            return new PatternFinderResult(column, groupColumn, crosstabs, _configuration);
        }
    }

    private Crosstab<Serializable> createCrosstab(final DefaultPatternFinder patternFinder) {
        final Crosstab<Serializable> crosstab = createCrosstab();

        final Set<Entry<TokenPattern, RowAnnotation>> entrySet = patternFinder.getAnnotations().entrySet();

        // sort the entries so that the ones with the highest amount of
        // matches are at the top
        final Set<Entry<TokenPattern, RowAnnotation>> sortedEntrySet = new TreeSet<>((o1, o2) -> {
            int result = o2.getValue().getRowCount() - o1.getValue().getRowCount();
            if (result == 0) {
                result = o1.getKey().toSymbolicString().compareTo(o2.getKey().toSymbolicString());
            }
            return result;
        });
        sortedEntrySet.addAll(entrySet);

        for (final Entry<TokenPattern, RowAnnotation> entry : sortedEntrySet) {
            final TokenPattern pattern = entry.getKey();
            final CrosstabNavigator<Serializable> nav = crosstab.navigate();
            nav.where(DIMENSION_NAME_PATTERN, pattern.toSymbolicString());

            nav.where(DIMENSION_NAME_MEASURES, MEASURE_MATCH_COUNT);
            final RowAnnotation annotation = entry.getValue();
            final int size = annotation.getRowCount();
            nav.put(size, true);
            nav.attach(AnnotatedRowsResult.createIfSampleRowsAvailable(annotation, _rowAnnotationFactory, column));

            nav.where(DIMENSION_NAME_MEASURES, MEASURE_SAMPLE);
            nav.put(pattern.getSampleString(), true);
        }
        return crosstab;
    }

    // setter methods for unittesting purposes
    public void setRowAnnotationFactory(final RowAnnotationFactory rowAnnotationFactory) {
        _rowAnnotationFactory = rowAnnotationFactory;
    }

    public void setColumn(final InputColumn<String> column) {
        this.column = column;
    }

    public void setPredefinedTokenName(final String predefinedTokenName) {
        this.predefinedTokenName = predefinedTokenName;
    }

    public void setPredefinedTokenPatterns(final String[] predefinedTokenPatterns) {
        this.predefinedTokenPatterns = predefinedTokenPatterns;
    }

    public void setDiscriminateTextCase(final boolean discriminateTextCase) {
        this.discriminateTextCase = discriminateTextCase;
    }

    public void setDiscriminateNegativeNumbers(final boolean discriminateNegativeNumbers) {
        this.discriminateNegativeNumbers = discriminateNegativeNumbers;
    }

    public void setDiscriminateDecimals(final boolean discriminateDecimals) {
        this.discriminateDecimals = discriminateDecimals;
    }

    public void setEnableMixedTokens(final boolean enableMixedTokens) {
        this.enableMixedTokens = enableMixedTokens;
    }

    public void setUpperCaseExpandable(final boolean upperCaseExpandable) {
        this.upperCaseExpandable = upperCaseExpandable;
    }

    public void setLowerCaseExpandable(final boolean lowerCaseExpandable) {
        this.lowerCaseExpandable = lowerCaseExpandable;
    }

    public void setDecimalSeparator(final Character decimalSeparator) {
        this.decimalSeparator = decimalSeparator;
    }

    public void setIgnoreRepeatedSpaces(final boolean ignoreRepeatedSpaces) {
        this.ignoreRepeatedSpaces = ignoreRepeatedSpaces;
    }

    public void setMinusSign(final Character minusSign) {
        this.minusSign = minusSign;
    }

    public void setThousandsSeparator(final Character thousandsSeparator) {
        this.thousandsSeparator = thousandsSeparator;
    }

    public void setGroupColumn(final InputColumn<String> groupColumn) {
        this.groupColumn = groupColumn;
    }
}
