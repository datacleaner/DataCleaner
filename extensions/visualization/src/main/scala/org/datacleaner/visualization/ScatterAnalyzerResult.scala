package org.datacleaner.visualization

import org.datacleaner.result.AnalyzerResult
import org.datacleaner.storage.RowAnnotation
import org.datacleaner.storage.RowAnnotationFactory
import org.datacleaner.data.InputRow
import org.datacleaner.data.InputColumn

class ScatterAnalyzerResult(groups: List[ScatterGroup], variable1: InputColumn[_], variable2: InputColumn[_], groupColumn: InputColumn[_]) extends AnalyzerResult {

  def variable1(): InputColumn[_] = variable1

  def variable2(): InputColumn[_] = variable2

  def groupColumn(): InputColumn[_] = groupColumn

  def hasGroups(): Boolean = groupColumn != null;

  def groups(): List[ScatterGroup] = groups

}
