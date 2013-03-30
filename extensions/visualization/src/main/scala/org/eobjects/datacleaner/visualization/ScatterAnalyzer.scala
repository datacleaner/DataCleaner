package org.eobjects.datacleaner.visualization

import org.eobjects.analyzer.beans.api.Analyzer
import org.eobjects.analyzer.beans.api.AnalyzerBean
import org.eobjects.analyzer.beans.api.Configured
import org.eobjects.analyzer.beans.api.Provided
import org.eobjects.analyzer.data.InputColumn
import org.eobjects.analyzer.data.InputRow
import javax.inject.Inject
import org.eobjects.analyzer.beans.api.AnalyzerBean
import org.eobjects.analyzer.beans.api.Configured
import org.eobjects.analyzer.beans.api.Provided
import org.eobjects.analyzer.storage.RowAnnotationFactory
import scala.collection.mutable.Map
import org.eobjects.analyzer.util.LabelUtils
import org.eobjects.analyzer.beans.api.Description
import org.eobjects.analyzer.beans.api.Categorized

@AnalyzerBean("Scatter plot")
@Description("Plots the occurences of two number variables in a scatter plot chart. A useful visualization for identifying outliers in numeric data relationships.")
@Categorized(Array(classOf[VisualizationCategory]))
class ScatterAnalyzer extends Analyzer[ScatterAnalyzerResult] {

  @Inject
  @Configured
  @Description("The field with the first variable. Will be plotted on the vertical Y-axis.")
  var variable1: InputColumn[Number] = null;

  @Inject
  @Configured
  @Description("The field with the second variable. Will be plotted on the horizontal X-axis.")
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