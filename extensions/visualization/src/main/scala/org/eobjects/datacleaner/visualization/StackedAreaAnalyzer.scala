package org.eobjects.datacleaner.visualization

import org.eobjects.analyzer.beans.api.Analyzer
import org.eobjects.analyzer.beans.api.AnalyzerBean
import org.eobjects.analyzer.beans.api.Categorized
import org.eobjects.analyzer.beans.api.Configured
import org.eobjects.analyzer.beans.api.Description
import org.eobjects.analyzer.data.InputColumn
import org.eobjects.analyzer.data.InputRow
import org.eobjects.analyzer.beans.api.Initialize
import org.eobjects.analyzer.beans.api.Validate

@AnalyzerBean("Stacked area plot")
@Description("Plots a number of related measures in a stacked area chart. Useful visualization for showing the relative influence of each measure compared to the sum of measures.")
@Categorized(Array(classOf[VisualizationCategory]))
class StackedAreaAnalyzer extends Analyzer[StackedAreaAnalyzerResult] {

  @Configured(order = 1)
  var measureColumns: Array[InputColumn[Number]] = null

  @Configured(order = 2)
  var categoryColumn: InputColumn[_] = null

  var result: StackedAreaAnalyzerResult = null;

  @Validate
  def validate() {
    result = new StackedAreaAnalyzerResult(categoryColumn, measureColumns);
    if (!result.isNumberCategory && !result.isTimeCategory) {
      throw new IllegalStateException("Category column must be either a number or time based")
    }
  }

  @Initialize
  def initialize() {
    result = new StackedAreaAnalyzerResult(categoryColumn, measureColumns);
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

  override def getResult(): StackedAreaAnalyzerResult = result
}