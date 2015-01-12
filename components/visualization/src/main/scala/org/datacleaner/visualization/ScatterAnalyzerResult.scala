package org.datacleaner.visualization

import org.datacleaner.api.AnalyzerResult
import org.datacleaner.api.InputRow
import org.datacleaner.api.InputColumn
import org.datacleaner.storage.RowAnnotation
import org.datacleaner.storage.RowAnnotationFactory

class ScatterAnalyzerResult(groups: List[ScatterGroup], variable1: InputColumn[_], variable2: InputColumn[_], groupColumn: InputColumn[_]) extends AnalyzerResult {

  def variable1(): InputColumn[_] = variable1

  def variable2(): InputColumn[_] = variable2

  def groupColumn(): InputColumn[_] = groupColumn

  def hasGroups(): Boolean = groupColumn != null;

  def groups(): List[ScatterGroup] = groups

}
