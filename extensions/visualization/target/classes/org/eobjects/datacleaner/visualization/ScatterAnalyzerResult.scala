package org.eobjects.datacleaner.visualization

import org.eobjects.analyzer.result.AnalyzerResult
import org.eobjects.analyzer.storage.RowAnnotation
import org.eobjects.analyzer.storage.RowAnnotationFactory
import org.eobjects.analyzer.data.InputRow
import org.eobjects.analyzer.data.InputColumn

class ScatterAnalyzerResult(groups: List[ScatterGroup], variable1: InputColumn[_], variable2: InputColumn[_], groupColumn: InputColumn[_]) extends AnalyzerResult {

  def variable1(): InputColumn[_] = variable1

  def variable2(): InputColumn[_] = variable2

  def groupColumn(): InputColumn[_] = groupColumn

  def hasGroups(): Boolean = groupColumn != null;

  def groups(): List[ScatterGroup] = groups

}