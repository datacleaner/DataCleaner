package org.datacleaner.visualization

import org.datacleaner.data.InputColumn
import org.datacleaner.beans.api.Analyzer
import org.datacleaner.beans.api.Configured
import org.datacleaner.beans.api.Categorized
import org.datacleaner.beans.api.Description
import javax.inject.Inject
import scala.collection.mutable.Map
import org.datacleaner.storage.RowAnnotationFactory
import org.datacleaner.storage.RowAnnotation
import org.datacleaner.data.InputRow
import org.datacleaner.util.LabelUtils
import org.datacleaner.beans.api.Provided

@Named("Density plot")
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
