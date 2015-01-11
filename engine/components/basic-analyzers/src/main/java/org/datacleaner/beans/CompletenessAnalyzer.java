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

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.metamodel.util.HasName;
import org.datacleaner.api.Analyzer;
import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.Initialize;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.Provided;
import org.datacleaner.beans.categories.ValidationCategory;
import org.datacleaner.storage.RowAnnotation;
import org.datacleaner.storage.RowAnnotationFactory;
import org.datacleaner.util.StringUtils;

@Named("Completeness analyzer")
@Description("Asserts the completeness of your data by ensuring that all required fields are filled.")
@Categorized(ValidationCategory.class)
public class CompletenessAnalyzer implements Analyzer<CompletenessAnalyzerResult> {

    public static enum Condition implements HasName {
        NOT_BLANK_OR_NULL("Not <blank> or <null>"), NOT_NULL("Not <null>");

        private final String _name;

        private Condition(String name) {
            _name = name;
        }

        @Override
        public String getName() {
            return _name;
        }
    }
    
    public static enum EvaluationMode implements HasName {
        ALL_FIELDS("When all fields are incomplete, the record is incomplete"), ANY_FIELD("When any field is incomplete, the record is incomplete");

        private final String _name;
        
        private EvaluationMode(String name) {
            _name = name;
        }
        
        @Override
        public String getName() {
            return _name;
        }
        
    }

    @Inject
    @Configured("Values")
    @Description("Values to check for completeness")
    InputColumn<?>[] _valueColumns;

    @Inject
    @Configured("Conditions")
    @Description("The conditions of which a value is determined to be filled or not")
    Condition[] _conditions;
    
    @Inject
    @Configured("Evaluation mode")
    EvaluationMode _evaluationMode = EvaluationMode.ANY_FIELD;

    @Inject
    @Provided
    RowAnnotation _invalidRecords;

    @Inject
    @Provided
    RowAnnotationFactory _annotationFactory;

    private final AtomicInteger _rowCount;

    public CompletenessAnalyzer() {
        _rowCount = new AtomicInteger();
    }

    @Initialize
    public void init() {
        _rowCount.set(0);
    }

    @Override
    public void run(InputRow row, int distinctCount) {
        _rowCount.addAndGet(distinctCount);
        boolean allInvalid = true;
        for (int i = 0; i < _valueColumns.length; i++) {
            final Object value = row.getValue(_valueColumns[i]);
            final boolean valid;
            if (value instanceof String && _conditions[i] == Condition.NOT_BLANK_OR_NULL) {
                valid = !StringUtils.isNullOrEmpty((String) value);
            } else {
                valid = value != null;
            }
            if (_evaluationMode == EvaluationMode.ANY_FIELD && !valid) {
                _annotationFactory.annotate(row, distinctCount, _invalidRecords);
                return;
            }
            
            if (valid) {
                allInvalid = false;
            }
        }
        if (_evaluationMode == EvaluationMode.ALL_FIELDS && allInvalid) {
            _annotationFactory.annotate(row, distinctCount, _invalidRecords);
            return;
        }
    }

    @Override
    public CompletenessAnalyzerResult getResult() {
        return new CompletenessAnalyzerResult(_rowCount.get(), _invalidRecords, _annotationFactory, _valueColumns);
    }

    public void setConditions(Condition[] conditions) {
        _conditions = conditions;
    }

    public void setValueColumns(InputColumn<?>[] valueColumns) {
        _valueColumns = valueColumns;
    }

    /**
     * Shortcut method to fill all conditions (of existing columns) to a single
     * condition.
     * 
     * @param condition
     */
    public void fillAllConditions(Condition condition) {
        if (_valueColumns != null) {
            final Condition[] conditions = new Condition[_valueColumns.length];
            Arrays.fill(conditions, condition);
            _conditions = conditions;
        }
    }

}
