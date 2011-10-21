package org.eobjects.datacleaner.testtools;

import org.eobjects.analyzer.beans.api.Analyzer;
import org.eobjects.analyzer.beans.api.AnalyzerBean;
import org.eobjects.analyzer.beans.api.Categorized;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Provided;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.result.AnnotatedRowsResult;
import org.eobjects.analyzer.storage.RowAnnotation;
import org.eobjects.analyzer.storage.RowAnnotationFactory;
import org.eobjects.metamodel.util.EqualsBuilder;

@AnalyzerBean("Assert equals")
@Categorized(TestToolsCategory.class)
public class AssertEqualsAnalyzer implements Analyzer<AnnotatedRowsResult> {
	
	@Configured
	InputColumn<?> column1;
	
	@Configured
	InputColumn<?> column2;
	
	@Provided
	RowAnnotation annotation;
	
	@Provided
	RowAnnotationFactory annotationFactory;
	
	@Override
	public void run(InputRow row, int distinctCount) {
		Object value1 = row.getValue(column1);
		Object value2 = row.getValue(column2);
		if (!EqualsBuilder.equals(value1, value2)) {
			annotationFactory.annotate(row, distinctCount, annotation);
		}
	}

	@Override
	public AnnotatedRowsResult getResult() {
		return new AnnotatedRowsResult(annotation, annotationFactory, column1, column2);
	}

}
