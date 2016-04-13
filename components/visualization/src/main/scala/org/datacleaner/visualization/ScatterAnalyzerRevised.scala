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
import collection.JavaConversions._

object ScatterAnalyzerRevised {
  final val PROPERTY_VARIABLE1 = "Variable1"
  final val PROPERTY_VARIABLE2 = "Variable2"
  final val PROPERTY_GROUP_COLUMN = "Group column"
}

@Named("Scatter plot")
@Description("Plots the occurences of two number variables in a scatter plot chart. A useful visualization for identifying outliers in numeric data relationships.")
@Categorized(Array(classOf[VisualizationCategory]))
class ScatterAnalyzerRevised extends Analyzer[ScatterAnalyzerResultRevised] {

  @Inject
  @Configured(value = ScatterAnalyzerRevised.PROPERTY_VARIABLE1)
  @Description("The field with the first variable. Will be plotted on the horizontal X-axis.")
  var variable1: InputColumn[Number] = null;

  @Inject
  @Configured(value = ScatterAnalyzerRevised.PROPERTY_VARIABLE2)
  @Description("The field with the second variable. Will be plotted on the vertical Y-axis.")
  var variable2: InputColumn[Number] = null;

  @Inject
  @Configured(value = ScatterAnalyzerRevised.PROPERTY_GROUP_COLUMN, required = false)
  var groupColumn: InputColumn[_] = null;

  @Inject
  @Provided
  var rowAnnotationFactory: RowAnnotationFactory = null;

  val groups: Map[String, ScatterGroupRevised] = Map[String, ScatterGroupRevised]().withDefault(
    groupName => {
      val group = new ScatterGroupRevised(groupName, rowAnnotationFactory)
      groups.put(groupName, group)
      group
    });

  override def run(row: InputRow, distinctCount: Int) = {
    val value1 = row.getValue(variable1);
    val value2 = row.getValue(variable2);
    
    if (value1 != null && value2 != null) {
        val groupNameValue = if (groupColumn == null) "Observations" else row.getValue(groupColumn)
                val groupName = LabelUtils.getValueLabel(groupNameValue)
                
        val group = groups(groupName);
        group.register(value1, value2, row, distinctCount);
    }
  }

  override def getResult: ScatterAnalyzerResultRevised = {
    val groupList = groups.values.toList;
    new ScatterAnalyzerResultRevised(groupList, variable1, variable2, groupColumn);
  }
}