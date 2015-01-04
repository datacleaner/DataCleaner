package org.eobjects.analyzer.beans.transform

import org.eobjects.analyzer.beans.api.Transformer
import org.eobjects.analyzer.beans.api.TransformerBean
import org.eobjects.analyzer.beans.api.Description
import org.eobjects.analyzer.beans.api.Categorized
import org.eobjects.analyzer.beans.categories.StringManipulationCategory
import org.eobjects.analyzer.data.InputColumn
import org.eobjects.analyzer.beans.api.Configured
import org.eobjects.analyzer.beans.api.OutputColumns
import org.eobjects.analyzer.data.InputRow

@TransformerBean("String length")
@Description("Counts the length of Strings and creates a separate column with this metric.")
@Categorized(Array(classOf[StringManipulationCategory]))
class StringLengthTransformer(col: InputColumn[String]) extends Transformer[Number] {

  @Configured
  @Description("Column to compute string lengths from")
  var column: InputColumn[String] = col;

  def this() = this(null)

  def getOutputColumns() = new OutputColumns(column.getName() + " length");

  def transform(row: InputRow): Array[Number] = {
    val value = row.getValue(column);
    return transform(value);
  }

  def transform(value: String): Array[Number] = {
    if (value == null) {
      return Array(null);
    } 
    return Array(value.length());
  }
}