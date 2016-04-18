package org.datacleaner.visualization

import java.lang.Iterable
import java.util

import org.apache.commons.lang3.tuple.{ImmutablePair, Pair}
import org.datacleaner.api.InputRow
import org.datacleaner.storage.{RowAnnotation, RowAnnotationFactory}

import scala.collection.JavaConverters._
import scala.collection.mutable

/**
 * Represents a group of scattered points
 */
class ScalaScatterGroup(name: String, rowAnnotationFactory: RowAnnotationFactory) extends ScatterGroup {
  
  def getName: String = name

  val annotations: mutable.Map[(Number, Number), RowAnnotation] = mutable.LinkedHashMap[(Number, Number), RowAnnotation]().withDefault(
    key => {
      val annotation = rowAnnotationFactory.createAnnotation();
      annotations.put(key, annotation);
      annotation;
    });

  def register(x: Number, y: Number, row: InputRow, distinctCount: Int) = {
    val annotation = annotations((x,y))
    rowAnnotationFactory.annotate(row, distinctCount, annotation)
  }
  
  def getRowAnnotationFactory = rowAnnotationFactory;
  
  override def getRowAnnotation(x: Number, y: Number) = annotations.getOrElse((x,y), null)

  @transient
  var javaAnnotations : util.Map[Pair[Number, Number], RowAnnotation] = null

  def getRowAnnotations : util.Map[Pair[Number, Number], RowAnnotation] = {
    this.synchronized {
      if (javaAnnotations == null) {
        javaAnnotations = new util.HashMap[Pair[Number, Number], RowAnnotation]()

        annotations.foreach(entry => {
          javaAnnotations.put(new ImmutablePair[Number, Number](entry._1._1, entry._1._2), entry._2)
        })
      }
    }
    javaAnnotations
  }

  def getCoordinates: Iterable[Pair[Number, Number]] = {
    val annotations = getRowAnnotations
    val javaCoordinates = new util.HashSet[Pair[Number, Number]]();

    annotations.asScala.foreach(entry => {
          javaCoordinates.add(new ImmutablePair[Number, Number](entry._1.getLeft, entry._1.getRight))
        }
    )
    javaCoordinates
  }
}
