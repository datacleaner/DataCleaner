package org.eobjects.datacleaner.visualization

import java.util.List
import org.jfree.data.category.CategoryDataset
import org.jfree.data.general.DatasetChangeListener
import org.jfree.data.general.DatasetGroup
import scala.collection.JavaConversions._
import org.jfree.data.xy.TableXYDataset
import org.jfree.data.DomainOrder

/**
 * CategoryDataset implementation of the StackedAreaAnalyzerResult for JFreeCharts
 */
class StackedAreaAnalyzerResultCategoryDataset(result: StackedAreaAnalyzerResult) extends CategoryDataset {

  private val columnNames = result.getMeasureColumns.map(col => col.getName())
  private val categories = result.getCategories
  
  private var group: DatasetGroup = new DatasetGroup("StackedAreaAnalyzerResult" + result.hashCode());

  override def addChangeListener(listener: DatasetChangeListener): Unit = {
    // change listeners not implemented
  }

  override def removeChangeListener(listener: DatasetChangeListener): Unit = {
    // change listeners not implemented
  }

  override def getGroup(): DatasetGroup = group

  override def setGroup(group: DatasetGroup) { this.group = group }

  override def getColumnKey(row: Int): Comparable[_] = categories(row).asInstanceOf[Comparable[_]]

  override def getColumnIndex(key: Comparable[_]): Int = categories.indexOf(key)

  override def getColumnKeys(): List[_] = categories

  override def getColumnCount(): Int = { categories.size }

  override def getRowKey(column: Int): Comparable[_] = columnNames(column)

  override def getRowIndex(key: Comparable[_]): Int = columnNames.indexOf(key)

  override def getRowKeys(): List[_] = columnNames.toList

  override def getRowCount(): Int = { columnNames.size }

  override def getValue(rowKey: Comparable[_], columnKey: Comparable[_]): Number = {
    val measures = result.getMeasures(columnKey)
    val index = getColumnIndex(rowKey);
    return measures(index);
  }

  override def getValue(row: Int, column: Int): Number = {
    val columnKey = getColumnKey(column)
    val measures = result.getMeasures(columnKey)
    return measures(row);
  }  
  
}