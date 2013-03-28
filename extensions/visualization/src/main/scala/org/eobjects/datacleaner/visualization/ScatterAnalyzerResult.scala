package org.eobjects.datacleaner.visualization

import org.eobjects.analyzer.result.AnalyzerResult
import org.eobjects.analyzer.storage.RowAnnotation
import org.eobjects.analyzer.storage.RowAnnotationFactory
import org.eobjects.analyzer.data.InputRow

class ScatterAnalyzerResult(groups : Iterable[ScatterGroup]) extends AnalyzerResult {

  def groups() : Iterable[ScatterGroup] = groups
}