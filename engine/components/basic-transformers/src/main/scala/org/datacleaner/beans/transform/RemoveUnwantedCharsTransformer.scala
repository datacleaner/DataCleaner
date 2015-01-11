package org.datacleaner.beans.transform

import javax.inject.Named
import org.datacleaner.api.Transformer
import org.datacleaner.api.Description
import org.datacleaner.api.Categorized
import org.datacleaner.beans.categories.StringManipulationCategory
import org.datacleaner.api.InputColumn
import org.datacleaner.api.Configured
import org.datacleaner.api.OutputColumns
import org.datacleaner.api.InputRow
import org.datacleaner.util.CharIterator

@Named("Remove unwanted characters")
@Description("Removes characters from strings that are not wanted. Use it to cleanse codes and identifiers that may have additional dashes, punctuations, unwanted letters etc.")
@Categorized(Array(classOf[StringManipulationCategory]))
class RemoveUnwantedCharsTransformer(col: InputColumn[String]) extends Transformer {

  @Configured
  var column: InputColumn[String] = col;

  @Configured(order = 1)
  var removeWhitespaces = true;

  @Configured(order = 2)
  var removeLetters = true;

  @Configured(order = 3)
  var removeDigits = false;

  @Configured(order = 4)
  @Description("Remove additional signs, such as dashes, punctiations, slashes and more?")
  var removeSigns = true;

  def this() = this(null)

  def getOutputColumns() = new OutputColumns(classOf[String], column.getName() + " (cleansed)");

  def transform(row: InputRow): Array[Object] = {
    val value = row.getValue(column);
    return transform(value);
  }

  def transform(value: String): Array[Object] = {
    if (value == null) {
      return Array(null);
    }
    val it = new CharIterator(value)
    while (it.hasNext()) {
      val c = it.next();
      if (it.isWhitespace()) {
        if (removeWhitespaces) {
          it.remove();
        }
      } else if (it.isLetter()) {
        if (removeLetters) {
          it.remove();
        }
      } else if (it.isDigit()) {
        if (removeDigits) {
          it.remove();
        }
      } else if (removeSigns) {
        it.remove();
      }
    }
    return Array(it.toString());
  }
}
