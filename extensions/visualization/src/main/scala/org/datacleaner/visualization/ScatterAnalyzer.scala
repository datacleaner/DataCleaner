package org.datacleaner.visualization

import org.datacleaner.beans.api.Analyzer
import org.datacleaner.beans.api.Configured
import org.datacleaner.beans.api.Provided
import org.datacleaner.data.InputColumn
import org.datacleaner.data.InputRow
import javax.inject.Inject
import javax.inject.Named
import org.datacleaner.beans.api.Configured
import org.datacleaner.beans.api.Provided
import org.datacleaner.storage.RowAnnotationFactory
import scala.collection.mutable.Map
import org.datacleaner.util.LabelUtils
import org.datacleaner.beans.api.Description
import org.datacleaner.beans.api.Categorized

@Named("Scatter plot")
@Description("Plots the occurences of two number variables in a scatter plot chart. A useful visualization for identifying outliers in numeric data relationships.")
@Categorized(Array(classOf[VisualizationCategory]))
class ScatterAnalyzer extends Analyzer[ScatterAnalyzerResult] {

  @Inject
  @Configured
  @Description("The field with the first variable. Will be plotted on the horizontal X-axis.")
  var variable1: InputColumn[Number] = null;

  @Inject
  @Configured
  @Description("The field with the second variable. Will be plotted on the vertical Y-axis.")
  var variable2: InputColumn[Number] = null;

  @Inject
  @Configured(required = false)
  var groupColumn: InputColumn[_] = null;

  @Inject
  @Provided
  var rowAnnotationFactory: RowAnnotationFactory = null;

  val groups: Map[String, ScatterGroup] = Map[String, ScatterGroup]().withDefault(
    groupName => {
      val group = new ScatterGroup(groupName, rowAnnotationFactory)
      groups.put(groupName, group)
      group
    });

  override def run(row: InputRow, distinctCount: Int) = {
    val value1 = row.getValue(variable1);
    val value2 = row.getValue(variable2);
    
    if (value1 != null && value2 != null) {
        val groupNameValue = if (groupColumn == null) "Observations" else row.getValue(groupColumn)
                val groupName = LabelUtils.getValueLabel(groupNameValue)
                
                val point = (value1, value2);
        val group = groups(groupName);
        group.register(point, row, distinctCount);
    }
  }

  override def getResult: ScatterAnalyzerResult = {
    val groupList = groups.values.toList;
    new ScatterAnalyzerResult(groupList, variable1, variable2, groupColumn);
  }
}
