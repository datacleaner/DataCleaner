package org.datacleaner.visualization

import java.util

import org.apache.commons.lang3.tuple.{ImmutablePair, Pair}
import org.datacleaner.api.InputColumn
import org.datacleaner.storage.{RowAnnotation, RowAnnotationFactory}

class ScalaDensityAnalyzerResult(annotations: Map[(Int, Int), RowAnnotation], variable1: InputColumn[Number], variable2: InputColumn[Number], annotationFactory: RowAnnotationFactory) extends IDensityAnalyzerResult {

  def getVariable1 = variable1
  
  def getVariable2 = variable2

  @transient
  var javaAnnotations : util.Map[Pair[Integer, Integer], RowAnnotation] = null

  def getRowAnnotations : util.Map[Pair[Integer, Integer], RowAnnotation] = {
    this.synchronized {
      if (javaAnnotations == null) {
        javaAnnotations = new util.HashMap[Pair[Integer, Integer], RowAnnotation]()

        annotations.foreach(entry => {
          javaAnnotations.put(new ImmutablePair[Integer, Integer](entry._1._1, entry._1._2), entry._2)
        })
      }
    }
    javaAnnotations
  }
  
  def getRowAnnotationFactory = annotationFactory
  
  def getRowAnnotation(x:Int, y:Int): RowAnnotation = {
    val annotation = annotations.get((x,y))
    annotation match {
      case Some(a) => a
      case None => null
    }
  }
}
