package org.datacleaner.visualization

import java.util

import org.datacleaner.api.InputColumn

import scala.collection.JavaConverters._

class ScalaScatterAnalyzerResult(groups: List[CommonScatterGroup], variable1: InputColumn[_], variable2: InputColumn[_], groupColumn: InputColumn[_]) extends ScatterAnalyzerResult {

  def getVariable1: InputColumn[_] = variable1

  def getVariable2: InputColumn[_] = variable2

  def getGroupColumn: InputColumn[_] = groupColumn

  def hasGroups: Boolean = groupColumn != null

  def getGroups: util.List[CommonScatterGroup] = {
    groups.asJava
  }
}
