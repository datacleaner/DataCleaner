package org.datacleaner.visualization

import javax.inject.{Inject, Named}

import org.datacleaner.api._
import org.datacleaner.storage.RowAnnotationFactory
import org.datacleaner.util.LabelUtils

import scala.collection.JavaConverters._
import scala.collection.mutable

object ScatterAnalyzer {
  final val PROPERTY_VARIABLE1 = "Variable1"
  final val PROPERTY_VARIABLE2 = "Variable2"
  final val PROPERTY_GROUP_COLUMN = "Group column"
}

@Named("Scatter plot")
@Description("Plots the occurences of two number variables in a scatter plot chart. A useful visualization for identifying outliers in numeric data relationships.")
@Categorized(Array(classOf[VisualizationCategory]))
class ScatterAnalyzer extends Analyzer[IScatterAnalyzerResult] {

  @Inject
  @Configured(value = ScatterAnalyzer.PROPERTY_VARIABLE1)
  @Description("The field with the first variable. Will be plotted on the horizontal X-axis.")
  var variable1: InputColumn[Number] = null

  @Inject
  @Configured(value = ScatterAnalyzer.PROPERTY_VARIABLE2)
  @Description("The field with the second variable. Will be plotted on the vertical Y-axis.")
  var variable2: InputColumn[Number] = null

  @Inject
  @Configured(value = ScatterAnalyzer.PROPERTY_GROUP_COLUMN, required = false)
  var groupColumn: InputColumn[_] = null

  @Inject
  @Provided
  var rowAnnotationFactory: RowAnnotationFactory = null

  val groups: mutable.Map[String, IScatterGroup] = mutable.Map[String, IScatterGroup]().withDefault(
    groupName => {
      val group = new JavaScatterGroup(groupName, rowAnnotationFactory)
      groups.put(groupName, group)
      group
    })

  override def run(row: InputRow, distinctCount: Int) = {
    val value1 = row.getValue(variable1)
    val value2 = row.getValue(variable2)

    if (value1 != null && value2 != null) {
      val groupNameValue = if (groupColumn == null) "Observations" else row.getValue(groupColumn)
      val groupName = LabelUtils.getValueLabel(groupNameValue)

      val point = (value1, value2)
      val group = groups(groupName)
      group.register(point._1, point._2, row, distinctCount)
    }
  }

  override def getResult: IScatterAnalyzerResult = {
    val groupList = groups.values.toList
    new JavaScatterAnalyzerResult(groupList.asJava, variable1, variable2, groupColumn)
  }
}