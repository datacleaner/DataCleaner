package org.eobjects.datacleaner.visualization

import org.eobjects.analyzer.data.InputColumn
import org.eobjects.analyzer.beans.api.AnalyzerBean
import org.eobjects.analyzer.beans.api.Analyzer
import org.eobjects.analyzer.beans.api.Configured
import org.eobjects.analyzer.beans.api.Categorized
import org.eobjects.analyzer.beans.api.Description
import javax.inject.Inject
import scala.collection.mutable.Map
import org.eobjects.analyzer.storage.RowAnnotationFactory
import org.eobjects.analyzer.storage.RowAnnotation
import org.eobjects.analyzer.data.InputRow
import org.eobjects.analyzer.util.LabelUtils
import org.eobjects.analyzer.beans.api.Provided

@AnalyzerBean("Density plot")
@Description("Plots the occurences of two number variables in a density plot chart. A useful visualization for identifying freqencies of combinations in numeric data relationships.")
@Categorized(Array(classOf[VisualizationCategory]))
class DensityAnalyzer extends Analyzer[DensityAnalyzerResult] {

  @Inject
  @Configured
  @Description("The field with the first variable. Will be plotted on the horizontal X-axis.")
  var variable1: InputColumn[Number] = null;

  @Inject
  @Configured
  @Description("The field with the second variable. Will be plotted on the vertical Y-axis.")
  var variable2: InputColumn[Number] = null;

  @Inject
  @Provided
  var rowAnnotationFactory: RowAnnotationFactory = null;
  
  val annotations: Map[(Int, Int), RowAnnotation] = Map[(Int, Int), RowAnnotation]().withDefault(
    point => {
      val annotation = rowAnnotationFactory.createAnnotation()
      annotations.put(point, annotation)
      annotation
    });

  override def run(row: InputRow, distinctCount: Int) = {
    val value1 = row.getValue(variable1);
    val value2 = row.getValue(variable2);

    if (value1 != null && value2 != null) {
      val point = (value1.intValue(), value2.intValue());
      val annotation = annotations(point)
      rowAnnotationFactory.annotate(row, distinctCount, annotation);
    }
  }

  override def getResult: DensityAnalyzerResult = {
    new DensityAnalyzerResult(annotations.toMap, variable1, variable2, rowAnnotationFactory);
  }
}