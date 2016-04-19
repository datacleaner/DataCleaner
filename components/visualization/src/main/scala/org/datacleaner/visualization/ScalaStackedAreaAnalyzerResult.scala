package org.datacleaner.visualization

import java.util

import org.apache.metamodel.util.ObjectComparator
import org.datacleaner.api.InputColumn
import org.datacleaner.util.ReflectionUtils

import scala.collection.JavaConverters._
import scala.collection.mutable
class ScalaStackedAreaAnalyzerResult(CategoryColumn: InputColumn[_], measureColumns: Array[InputColumn[Number]]) extends StackedAreaAnalyzerResult {

  val measureMap: mutable.Map[Any, Array[Number]] = mutable.Map()

  def isTimeCategory: Boolean = ReflectionUtils.isDate(CategoryColumn.getDataType)

  def isNumberCategory: Boolean = ReflectionUtils.isNumber(CategoryColumn.getDataType)

  def getCategoryColumn = CategoryColumn

  def getCategoryCount = measureMap.size

  def getCategories: util.List[_] = {
    val list = measureMap.keys.toList
    list.sortWith({ (x, y) => ObjectComparator.getComparator.compare(x.asInstanceOf[AnyRef], y.asInstanceOf[AnyRef]) < 0 }).asJava
  }

  def getMeasureColumns = measureColumns

  def getMeasures(category: Any): Array[Number] = measureMap.getOrElse(category, null)

  def addMeasures(category: Any, measures: Array[Number]) = {
    val measuresOption = measureMap.get(category)
    measuresOption match {
      case Some(m) => for (i <- m.indices) {
        val previousValue = m(i)
        val additionValue = measures(i)
        val newValue = sum(previousValue, additionValue)
        m.update(i, newValue)
      }
      case None => measureMap.put(category, measures)
    }
  }

  def sum(x: Number, y: Number): Number = {
    if (x == null) {
      return y
    }
    if (y == null) {
      return x
    }
    x.doubleValue + y.doubleValue
  }
}
