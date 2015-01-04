package org.datacleaner.beans.transform

import org.datacleaner.beans.api.Transformer
import org.datacleaner.beans.api.TransformerBean
import org.datacleaner.beans.api.Description
import org.datacleaner.beans.api.Categorized
import org.datacleaner.beans.categories.StringManipulationCategory
import org.datacleaner.data.InputColumn
import org.datacleaner.beans.api.Configured
import org.datacleaner.beans.api.OutputColumns
import org.datacleaner.data.InputRow

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
