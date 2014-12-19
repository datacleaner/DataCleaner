package org.eobjects.datacleaner.visualization

import org.eobjects.analyzer.storage.RowAnnotation
import org.eobjects.analyzer.storage.RowAnnotationFactory
import scala.collection.mutable.Map
import org.eobjects.analyzer.data.InputRow
import org.eobjects.analyzer.util.LabelUtils
import scala.collection.mutable.LinkedHashMap

/**
 * Represents a group of scattered points
 */
class ScatterGroup(name: String, rowAnnotationFactory: RowAnnotationFactory) {
  
  def name(): String = name

  val annotations: Map[(Number, Number), RowAnnotation] = LinkedHashMap[(Number, Number), RowAnnotation]().withDefault(
    key => {
      val annotation = rowAnnotationFactory.createAnnotation();
      annotations.put(key, annotation);
      annotation;
    });

  def register(point: (Number, Number), row: InputRow, distinctCount: Int) = {
    val annotation = annotations(point);
    rowAnnotationFactory.annotate(row, distinctCount, annotation);
  }
  
  def getRowAnnotationFactory() = rowAnnotationFactory;
  
  def getRowAnnotation(point: (Number, Number)) = annotations.getOrElse(point, null)
  
  def getCoordinates(): Iterable[(Number, Number)] = annotations.keys
}