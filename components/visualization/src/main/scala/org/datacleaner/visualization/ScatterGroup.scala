package org.datacleaner.visualization

import org.datacleaner.api.InputRow
import org.datacleaner.storage.RowAnnotation
import org.datacleaner.storage.RowAnnotationFactory
import org.datacleaner.util.LabelUtils
import scala.collection.mutable.Map
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
