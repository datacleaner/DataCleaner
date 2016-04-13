package org.datacleaner.visualization

import javax.inject.Named
import org.datacleaner.api.Analyzer
import org.datacleaner.api.Categorized
import org.datacleaner.api.Configured
import org.datacleaner.api.Description
import org.datacleaner.api.InputColumn
import org.datacleaner.api.InputRow
import org.datacleaner.api.Initialize
import org.datacleaner.api.Validate
import collection.JavaConversions._

object StackedAreaAnalyzerRevised {
  final val PROPERTY_MEASURE_COLUMNS = "Measure columns"
  final val PROPERTY_CATEGORY_COLUMN = "Category column"
}

@Named("Stacked area plot")
@Description("Plots a number of related measures in a stacked area chart. Useful visualization for showing the relative influence of each measure compared to the sum of measures.")
@Categorized(Array(classOf[VisualizationCategory]))
class StackedAreaAnalyzerRevised extends Analyzer[StackedAreaAnalyzerResultRevised] {

  @Configured(value = StackedAreaAnalyzerRevised.PROPERTY_MEASURE_COLUMNS, order = 1)
  var measureColumns: Array[InputColumn[Number]] = null

  @Configured(value = StackedAreaAnalyzerRevised.PROPERTY_CATEGORY_COLUMN, order = 2)
  var categoryColumn: InputColumn[_] = null

  var result: StackedAreaAnalyzerResultRevised = null;

  @Validate
  def validate() {
    result = new StackedAreaAnalyzerResultRevised(categoryColumn, measureColumns);
    if (!result.isNumberCategory && !result.isTimeCategory) {
      throw new IllegalStateException("Category column must be either a number or time based")
    }
  }

  @Initialize
  def initialize() {
    result = new StackedAreaAnalyzerResultRevised(categoryColumn, measureColumns);
  }

  override def run(row: InputRow, distinctCount: Int) = {
    val category = row.getValue(categoryColumn);
    if (category != null) {
      val measures = measureColumns.map(col => row.getValue(col))

      for (i <- 1 to distinctCount) {
        result.addMeasures(category, measures);
      }
    }
  }

  override def getResult(): StackedAreaAnalyzerResultRevised = result
}
