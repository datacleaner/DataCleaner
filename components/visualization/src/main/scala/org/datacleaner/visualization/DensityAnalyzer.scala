package org.datacleaner.visualization

import javax.inject.{Inject, Named}

import org.apache.commons.lang3.tuple.{ImmutablePair, Pair}
import org.datacleaner.api._
import org.datacleaner.storage.{RowAnnotation, RowAnnotationFactory}

import scala.collection.JavaConverters._
import scala.collection.mutable

object DensityAnalyzer {
  final val PROPERTY_VARIABLE1 = "Variable1"
  final val PROPERTY_VARIABLE2 = "Variable2"
}

@Named("Density plot")
@Description("Plots the occurences of two number variables in a density plot chart. A useful visualization for identifying freqencies of combinations in numeric data relationships.")
@Categorized(Array(classOf[VisualizationCategory]))
class DensityAnalyzer extends Analyzer[IDensityAnalyzerResult] {

  @Inject
  @Configured(value = DensityAnalyzer.PROPERTY_VARIABLE1)
  @Description("The field with the first variable. Will be plotted on the horizontal X-axis.")
  var variable1: InputColumn[Number] = null

  @Inject
  @Configured(value = DensityAnalyzer.PROPERTY_VARIABLE2)
  @Description("The field with the second variable. Will be plotted on the vertical Y-axis.")
  var variable2: InputColumn[Number] = null

  @Inject
  @Provided
  var rowAnnotationFactory: RowAnnotationFactory = null

  val annotations: mutable.Map[Pair[Integer, Integer], RowAnnotation] = mutable.Map[Pair[Integer, Integer], RowAnnotation]().withDefault(
    point => {
      val annotation = rowAnnotationFactory.createAnnotation()
      annotations.put(point, annotation)
      annotation
    })

  override def run(row: InputRow, distinctCount: Int) = {
    val value1 = row.getValue(variable1)
    val value2 = row.getValue(variable2)

    if (value1 != null && value2 != null) {
      val point = new ImmutablePair[Integer, Integer](value1.intValue(), value2.intValue())
      val annotation = annotations(point)
      rowAnnotationFactory.annotate(row, distinctCount, annotation)
    }
  }

  override def getResult: IDensityAnalyzerResult = {
    new JavaDensityAnalyzerResult(annotations.asJava, variable1, variable2, rowAnnotationFactory)
  }
}
