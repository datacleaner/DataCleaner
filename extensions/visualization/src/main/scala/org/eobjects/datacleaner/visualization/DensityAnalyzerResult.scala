package org.eobjects.datacleaner.visualization

import org.eobjects.analyzer.result.AnalyzerResult
import org.eobjects.analyzer.data.InputColumn
import org.eobjects.analyzer.storage.RowAnnotation

class DensityAnalyzerResult(annotations: Map[(Int, Int), RowAnnotation], variable1: InputColumn[Number], variable2: InputColumn[Number]) extends AnalyzerResult {

  def getVariable1 = variable1;
  
  def getVariable2 = variable2;
  
  def getAnnotations = annotations
}