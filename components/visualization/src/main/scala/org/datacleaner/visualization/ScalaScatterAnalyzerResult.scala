package org.datacleaner.visualization

import java.util

import org.datacleaner.api.InputColumn

import scala.collection.JavaConverters._

class ScalaScatterAnalyzerResult(groups: List[IScatterGroup], variable1: InputColumn[_], variable2: InputColumn[_], groupColumn: InputColumn[_]) extends IScatterAnalyzerResult {

  def getVariable1: InputColumn[_] = variable1

  def getVariable2: InputColumn[_] = variable2

  def getGroupColumn: InputColumn[_] = groupColumn

  def hasGroups: Boolean = groupColumn != null

  def getGroups: util.List[IScatterGroup] = {
    groups.asJava
  }
}
