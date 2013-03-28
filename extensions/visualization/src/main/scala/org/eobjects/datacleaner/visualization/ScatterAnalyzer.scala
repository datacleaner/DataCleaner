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
import scala.collection.mutable.Map;

@AnalyzerBean("Scatter analyzer")
class ScatterAnalyzer extends Analyzer[ScatterAnalyzerResult] {

  @Inject
  @Configured
  var variable1: InputColumn[Number] = null;

  @Inject
  @Configured
  var variable2: InputColumn[Number] = null;

  @Inject
  @Configured(required = false)
  var groupColumn: InputColumn[_] = null;

  @Inject
  @Provided
  var rowAnnotationFactory: RowAnnotationFactory = null;

  val groups : Map[String, ScatterGroup] = Map[String, ScatterGroup]().withDefault(
    groupName => {
      val group = new ScatterGroup(groupName, rowAnnotationFactory)
      groups.put(groupName, group)
      group
    });

  override def run(row: InputRow, distinctCount: Int) = {
    val value1 = row.getValue(variable1);
    val value2 = row.getValue(variable2);
    
    val groupNameValue = if (groupColumn == null) null else row.getValue(groupColumn)
    val groupName = if (groupNameValue == null) "" else groupNameValue.toString

    val point = (value1, value2);
    val group = groups(groupName);
    group.register(point, row, distinctCount);
    
  }

  override def getResult: ScatterAnalyzerResult = {
    val groupSeq = groups.values;
    new ScatterAnalyzerResult(groupSeq);
  }
}