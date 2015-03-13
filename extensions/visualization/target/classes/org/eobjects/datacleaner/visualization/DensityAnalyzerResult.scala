package org.eobjects.datacleaner.visualization

import org.eobjects.analyzer.result.AnalyzerResult
import org.eobjects.analyzer.data.InputColumn
import org.eobjects.analyzer.storage.RowAnnotation
import org.eobjects.analyzer.storage.RowAnnotationFactory

class DensityAnalyzerResult(annotations: Map[(Int, Int), RowAnnotation], variable1: InputColumn[Number], variable2: InputColumn[Number], annotationFactory: RowAnnotationFactory) extends AnalyzerResult {

  def getVariable1 = variable1;
  
  def getVariable2 = variable2;
  
  def getRowAnnotations = annotations
  
  def getRowAnnotationFactory = annotationFactory
  
  def getRowAnnotation(x:Int, y:Int): RowAnnotation = {
    val annotation = annotations.get((x,y))
    annotation match {
      case Some(a) => a
      case None => null
    }
  }
}