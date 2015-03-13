package org.eobjects.datacleaner.visualization

import org.eobjects.analyzer.result.AnalyzerResult
import org.eobjects.analyzer.data.InputColumn
import org.eobjects.analyzer.util.ReflectionUtils
import scala.collection.mutable.Map
import org.apache.metamodel.util.TimeComparator
import org.apache.metamodel.util.ObjectComparator

class StackedAreaAnalyzerResult(CategoryColumn: InputColumn[_], measureColumns: Array[InputColumn[Number]]) extends AnalyzerResult {

  val measureMap: Map[Any, Array[Number]] = Map();

  def isTimeCategory(): Boolean = ReflectionUtils.isDate(CategoryColumn.getDataType());

  def isNumberCategory(): Boolean = ReflectionUtils.isNumber(CategoryColumn.getDataType());

  def getCategoryColumn() = CategoryColumn
  
  def getCategoryCount() = measureMap.size

  def getCategories(): List[_] = {
    val list = measureMap.keys.toList
    return list.sortWith({ (x, y) => ObjectComparator.getComparator().compare(x.asInstanceOf[AnyRef], y.asInstanceOf[AnyRef]) < 0 });
  }

  def getMeasureColumns = measureColumns;

  def getMeasures(category: Any): Array[Number] = measureMap.getOrElse(category, null)

  def addMeasures(category: Any, measures: Array[Number]) = {
    val measuresOption = measureMap.get(category)
    measuresOption match {
      case Some(m) => for (i <- 0 to m.length - 1) {
        val previousValue = m(i)
        val additionValue = measures(i)
        val newValue = sum(previousValue, additionValue);
        m.update(i, newValue);
      }
      case None => measureMap.put(category, measures)
    }
  }

  def sum(x: Number, y: Number): Number = {
    if (x == null) {
      return y;
    }
    if (y == null) {
      return x;
    }
    return x.doubleValue + y.doubleValue
  }
}