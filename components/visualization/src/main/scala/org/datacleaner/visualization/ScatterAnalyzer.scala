package org.datacleaner.visualization

import javax.inject.Inject
import javax.inject.Named
import org.datacleaner.api.Analyzer
import org.datacleaner.api.Configured
import org.datacleaner.api.Provided
import org.datacleaner.api.InputColumn
import org.datacleaner.api.InputRow
import org.datacleaner.api.Configured
import org.datacleaner.api.Provided
import org.datacleaner.api.Description
import org.datacleaner.api.Categorized
import org.datacleaner.storage.RowAnnotationFactory
import scala.collection.mutable.Map
import org.datacleaner.util.LabelUtils

object ScatterAnalyzer {
  final val PROPERTY_VARIABLE1 = "Variable1"
  final val PROPERTY_VARIABLE2 = "Variable2"
  final val PROPERTY_GROUP_COLUMN = "Group column"
}

@Named("Scatter plot")
@Description("Plots the occurences of two number variables in a scatter plot chart. A useful visualization for identifying outliers in numeric data relationships.")
@Categorized(Array(classOf[VisualizationCategory]))
class ScatterAnalyzer extends Analyzer[ScatterAnalyzerResult] {

  @Inject
  @Configured(value = ScatterAnalyzer.PROPERTY_VARIABLE1)
  @Description("The field with the first variable. Will be plotted on the horizontal X-axis.")
  var variable1: InputColumn[Number] = null;

  @Inject
  @Configured(value = ScatterAnalyzer.PROPERTY_VARIABLE2)
  @Description("The field with the second variable. Will be plotted on the vertical Y-axis.")
  var variable2: InputColumn[Number] = null;

  @Inject
  @Configured(value = ScatterAnalyzer.PROPERTY_GROUP_COLUMN, required = false)
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